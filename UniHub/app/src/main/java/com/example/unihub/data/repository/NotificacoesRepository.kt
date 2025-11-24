package com.example.unihub.data.repository

import android.os.Build
import androidx.annotation.RequiresExtension
import com.example.unihub.data.api.NotificacoesApi
import com.example.unihub.data.config.TokenManager
import com.example.unihub.data.model.Antecedencia
import com.example.unihub.data.model.NotificacoesConfig
import com.example.unihub.data.model.normalized
import com.example.unihub.notifications.FrequenciaNotificationScheduler
import com.example.unihub.notifications.CompartilhamentoNotificationSynchronizer
import com.example.unihub.notifications.ContatoNotificationSynchronizer
import com.example.unihub.notifications.AvaliacaoNotificationScheduler
import com.example.unihub.notifications.TarefaNotificationScheduler
import com.example.unihub.data.model.Avaliacao as AvaliacaoModel
import com.example.unihub.data.dto.TarefaDto
import kotlinx.coroutines.flow.first
import java.time.Duration

class NotificacoesRepository(
    private val api: NotificacoesApi,
    private val frequenciaScheduler: FrequenciaNotificationScheduler,
    private val avaliacaoScheduler: AvaliacaoNotificationScheduler,
    private val tarefaScheduler: TarefaNotificationScheduler,
    private val disciplinaRepository: DisciplinaRepository,
    private val avaliacaoRepository: AvaliacaoRepository,
    private val tarefaRepository: TarefaRepository,
    private val notificationHistoryRepository: NotificationHistoryRepository,
    private val compartilhamentoSynchronizer: CompartilhamentoNotificationSynchronizer,
    private val contatoSynchronizer: ContatoNotificationSynchronizer,
) {

    suspend fun carregarConfig(): NotificacoesConfig {
        TokenManager.requireUsuarioId()
        return api.carregar().normalized().also { config ->
            avaliacaoScheduler.persistAntecedencias(config.avaliacoesConfig.antecedencia)
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun salvarConfig(config: NotificacoesConfig): NotificacoesConfig {
        TokenManager.requireUsuarioId()

        // 1. Persiste a nova configuração no backend real
        val payload = config.normalized()
        val salvo = api.salvar(payload).normalized()
        avaliacaoScheduler.persistAntecedencias(salvo.avaliacoesConfig.antecedencia)


        // 2. Re-agenda as notificações com base na nova configuração
        reagendarTodasNotificacoes(salvo)
        return salvo
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private suspend fun reagendarTodasNotificacoes(config: NotificacoesConfig) {

        avaliacaoScheduler.persistAntecedencias(config.avaliacoesConfig.antecedencia)


        // *** Lógica de Presença/Aula ***
        if (config.notificacaoDePresenca) {
            val disciplinasResumo = getDisciplinasResumoSuspend()
            val frequenciaInfoList = disciplinasResumo
                .filter { it.receberNotificacoes }
                .map { discResumo ->
                    FrequenciaNotificationScheduler.DisciplineScheduleInfo(
                        id = discResumo.id,
                        nome = discResumo.nome,
                        receberNotificacoes = discResumo.receberNotificacoes,
                        horariosAulas = discResumo.aulas,
                        totalAusencias = discResumo.totalAusencias,
                        ausenciasPermitidas = discResumo.ausenciasPermitidas
                    )
                }
            frequenciaScheduler.scheduleNotifications(frequenciaInfoList)
        } else {
            frequenciaScheduler.cancelAllNotifications()
            notificationHistoryRepository.clearByCategoryOrType(
                category = NotificationHistoryRepository.FREQUENCIA_CATEGORY,
                types = setOf(
                    NotificationHistoryRepository.FREQUENCIA_TYPE,
                    NotificationHistoryRepository.FREQUENCIA_RESPOSTA_TYPE,
                )
            )
        }

        // *** Lógica de Avaliação ***
        if (config.avaliacoesAtivas) {
            val avaliacoesAtivas = getAvaliacoesAtivasSuspend()
            val avaliacaoInfoList = avaliacoesAtivas
                .filter { it.receberNotificacoes }
                .mapNotNull { avaliacao ->

                    val avaliacaoId = avaliacao.id ?: return@mapNotNull null

                    val prioridade = avaliacao.prioridade
                    val antecedenciaEscolhida: Antecedencia? = config.avaliacoesConfig.antecedencia[prioridade]
                    val reminderDuration: Duration =
                        antecedenciaEscolhida?.duration ?: Antecedencia.padrao.duration

                    AvaliacaoNotificationScheduler.AvaliacaoInfo(
                        id = avaliacaoId,
                        descricao = avaliacao.descricao,
                        disciplinaId = AvaliacaoNotificationScheduler.parseDisciplinaId(avaliacao.disciplina?.id),
                        disciplinaNome = avaliacao.disciplina?.nome,
                        dataHoraIso = avaliacao.dataEntrega,
                        prioridade = prioridade,
                        overrideReminderDuration = reminderDuration,
                        receberNotificacoes = avaliacao.receberNotificacoes
                    )
                }
            avaliacaoScheduler.scheduleNotifications(avaliacaoInfoList)
        } else {
            avaliacaoScheduler.scheduleNotifications(emptyList())
            notificationHistoryRepository.clearByCategoryOrType(
                category = NotificationHistoryRepository.AVALIACAO_CATEGORY,
                types = setOf(NotificationHistoryRepository.AVALIACAO_LEMBRETE_TYPE)
            )
        }

        // *** Lógica de Tarefas/Quadros ***
        if (config.prazoTarefa) {
            val tarefasAtivas = getTarefasAtivasSuspend()
            val tarefaInfoList = tarefasAtivas
                .filter { it.receberNotificacoes }
                .map { tarefaDto ->
                    TarefaNotificationScheduler.TarefaInfo(
                        titulo = tarefaDto.titulo,
                        quadroNome = tarefaDto.nomeQuadro,
                        prazoIso = tarefaDto.dataPrazo,
                        receberNotificacoes = tarefaDto.receberNotificacoes
                    )
                }
            tarefaScheduler.scheduleNotifications(tarefaInfoList)
        } else {
            tarefaScheduler.scheduleNotifications(emptyList())
            notificationHistoryRepository.clearByCategoryOrType(
                category = NotificationHistoryRepository.TAREFA_CATEGORY,
                types = setOf(NotificationHistoryRepository.TAREFA_PRAZO_TYPE)
            )
        }

        if (!config.compartilhamentoDisciplina) {
            compartilhamentoSynchronizer.silenceNotifications()
        }

        if (!config.incluirEmQuadro) {
            notificationHistoryRepository.clearByCategoryOrType(
                category = NotificationHistoryRepository.TAREFA_ATRIBUICAO_CATEGORY,
                types = setOf(NotificationHistoryRepository.TAREFA_ATRIBUICAO_CATEGORY)
            )
        }

        if (!config.comentarioTarefa) {
            notificationHistoryRepository.clearByCategoryOrType(
                category = NotificationHistoryRepository.TAREFA_COMENTARIO_CATEGORY,
                types = setOf(NotificationHistoryRepository.TAREFA_COMENTARIO_CATEGORY)
            )
        }

        if (!config.conviteContato) {
            contatoSynchronizer.silenceNotifications()
        }

        if (!config.inclusoEmGrupo) {
            notificationHistoryRepository.clearByCategoryOrType(
                category = NotificationHistoryRepository.GROUP_MEMBER_CATEGORY,
                types = setOf(NotificationHistoryRepository.GROUP_MEMBER_CATEGORY)
            )
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