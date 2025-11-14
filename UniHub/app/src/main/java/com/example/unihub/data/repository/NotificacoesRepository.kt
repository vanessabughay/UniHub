package com.example.unihub.data.repository

import android.os.Build
import androidx.annotation.RequiresExtension
import com.example.unihub.data.api.NotificacoesApi
import com.example.unihub.data.config.TokenManager
import com.example.unihub.data.model.Antecedencia
import com.example.unihub.data.model.NotificacoesConfig
import com.example.unihub.data.model.normalized
import com.example.unihub.notifications.AttendanceNotificationScheduler
import com.example.unihub.notifications.EvaluationNotificationScheduler
import com.example.unihub.notifications.TaskNotificationScheduler
import com.example.unihub.data.model.Avaliacao as AvaliacaoModel
import com.example.unihub.data.dto.TarefaDto
import kotlinx.coroutines.flow.first
import java.time.Duration

class NotificacoesRepository(
    private val api: NotificacoesApi,
    private val attendanceScheduler: AttendanceNotificationScheduler,
    private val evaluationScheduler: EvaluationNotificationScheduler,
    private val taskScheduler: TaskNotificationScheduler,
    private val disciplinaRepository: DisciplinaRepository,
    private val avaliacaoRepository: AvaliacaoRepository,
    private val tarefaRepository: TarefaRepository
) {

    suspend fun carregarConfig(): NotificacoesConfig {
        val (authHeader, usuarioId) = getAuthData()
        return api.carregar(authHeader, usuarioId).normalized()
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun salvarConfig(config: NotificacoesConfig): NotificacoesConfig {
        val (authHeader, usuarioId) = getAuthData()

        // 1. Persiste a nova configuração no backend real
        val payload = config.normalized()
        val salvo = api.salvar(authHeader, usuarioId, payload).normalized()

        // 2. Re-agenda as notificações com base na nova configuração
        reagendarTodasNotificacoes(salvo)
        return salvo
    }

    private fun getAuthData(): Pair<String, Long> {
        val token = TokenManager.token ?: throw IllegalStateException("Token de autenticação não encontrado")
        val usuarioId = TokenManager.usuarioId ?: throw IllegalStateException("ID do usuário não encontrado")
        val authHeader = "Bearer $token"
        return Pair(authHeader, usuarioId)
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private suspend fun reagendarTodasNotificacoes(config: NotificacoesConfig) {

        // *** Lógica de Presença/Aula ***
        if (config.notificacaoDePresenca) {
            val disciplinasResumo = getDisciplinasResumoSuspend()
            val attendanceInfoList = disciplinasResumo
                .filter { it.receberNotificacoes }
                .map { discResumo ->
                    AttendanceNotificationScheduler.DisciplineScheduleInfo(
                        id = discResumo.id,
                        nome = discResumo.nome,
                        receberNotificacoes = discResumo.receberNotificacoes,
                        horariosAulas = discResumo.aulas,
                        totalAusencias = discResumo.totalAusencias,
                        ausenciasPermitidas = discResumo.ausenciasPermitidas
                    )
                }
            attendanceScheduler.scheduleNotifications(attendanceInfoList)
        } else {
            attendanceScheduler.cancelAllNotifications()
        }

        // *** Lógica de Avaliação ***
        if (config.avaliacoesAtivas) {
            val avaliacoesAtivas = getAvaliacoesAtivasSuspend()
            val evaluationInfoList = avaliacoesAtivas
                .filter { it.receberNotificacoes }
                .mapNotNull { avaliacao ->
                    val disciplinaNome = avaliacao.disciplina?.nome ?: return@mapNotNull null
                    val disciplinaId = (avaliacao.disciplina?.id as? String)?.toLongOrNull() ?: return@mapNotNull null
                    val avaliacaoId = avaliacao.id ?: return@mapNotNull null



                    val prioridade = avaliacao.prioridade

                    val antecedenciaEscolhida: Antecedencia? = config.avaliacoesConfig.periodicidade[prioridade]

                    val reminderDuration: Duration =
                        if (antecedenciaEscolhida == null || antecedenciaEscolhida == Antecedencia.padrao) {
                            EvaluationNotificationScheduler.defaultReminderDuration(prioridade)
                        } else {
                            Duration.ofDays(antecedenciaEscolhida.dias.toLong())
                        }

                    EvaluationNotificationScheduler.EvaluationInfo(
                        id = avaliacaoId,
                        descricao = avaliacao.descricao,
                        disciplinaId = disciplinaId,
                        disciplinaNome = disciplinaNome,
                        dataHoraIso = avaliacao.dataEntrega,
                        reminderDuration = reminderDuration,
                        receberNotificacoes = avaliacao.receberNotificacoes
                    )
                }
            evaluationScheduler.scheduleNotifications(evaluationInfoList)
        } else {
            evaluationScheduler.scheduleNotifications(emptyList())
        }

        // *** Lógica de Tarefas/Quadros ***
        if (config.prazoTarefa) {
            val tarefasAtivas = getTarefasAtivasSuspend()
            val taskInfoList = tarefasAtivas
                .filter { it.receberNotificacoes }
                .map { tarefaDto ->
                    TaskNotificationScheduler.TaskInfo(
                        titulo = tarefaDto.titulo,
                        quadroNome = tarefaDto.nomeQuadro,
                        prazoIso = tarefaDto.dataPrazo,
                        receberNotificacoes = tarefaDto.receberNotificacoes
                    )
                }
            taskScheduler.scheduleNotifications(taskInfoList)
        } else {
            taskScheduler.scheduleNotifications(emptyList())
        }
    }



    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private suspend fun getDisciplinasResumoSuspend(): List<DisciplinaResumo> {
        return disciplinaRepository.getDisciplinasResumo().first()
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private suspend fun getAvaliacoesAtivasSuspend(): List<AvaliacaoModel> {
        return avaliacaoRepository.getAvaliacao().first()
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private suspend fun getTarefasAtivasSuspend(): List<TarefaDto> {
        return tarefaRepository.getProximasTarefas()
    }
}