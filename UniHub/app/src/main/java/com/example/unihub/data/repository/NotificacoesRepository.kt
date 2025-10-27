package com.example.unihub.data.repository

import android.os.Build
import androidx.annotation.RequiresExtension
import com.example.unihub.data.api.NotificacoesApi
import com.example.unihub.data.model.Antecedencia
import com.example.unihub.data.model.NotificacoesConfig
import com.example.unihub.data.model.Prioridade
import com.example.unihub.notifications.AttendanceNotificationScheduler
import com.example.unihub.notifications.EvaluationNotificationScheduler
import com.example.unihub.data.model.Avaliacao as AvaliacaoModel
import kotlinx.coroutines.flow.first

class NotificacoesRepository(
    private val api: NotificacoesApi,
    private val attendanceScheduler: AttendanceNotificationScheduler,
    private val evaluationScheduler: EvaluationNotificationScheduler,
    private val disciplinaRepository: DisciplinaRepository,
    private val avaliacaoRepository: AvaliacaoRepository
) {

    suspend fun carregarConfig(): NotificacoesConfig {
        return api.carregar()
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun salvarConfig(config: NotificacoesConfig) {
        // 1. Persiste a nova configuração
        api.salvar(config)
        // 2. Re-agenda as notificações com base na nova configuração
        reagendarTodasNotificacoes(config)
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

                    val antecedenciaDias = getAntecedenciaDias(avaliacao.prioridade, config)

                    EvaluationNotificationScheduler.EvaluationInfo(
                        id = avaliacaoId,
                        descricao = avaliacao.descricao,
                        disciplinaId = disciplinaId,
                        disciplinaNome = disciplinaNome,
                        dataHoraIso = avaliacao.dataEntrega,
                        prioridade = avaliacao.prioridade,
                        receberNotificacoes = avaliacao.receberNotificacoes,
                        antecedenciaDias = antecedenciaDias
                    )
                }
            evaluationScheduler.scheduleNotifications(evaluationInfoList)
        } else {
            evaluationScheduler.cancelAllNotifications()
        }
    }

    private fun getAntecedenciaDias(prioridade: Prioridade, config: NotificacoesConfig): Int {
        val antecedencia: Antecedencia = config.avaliacoesConfig.periodicidade[prioridade]
            ?: Antecedencia.padrao
        return antecedencia.dias
    }


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private suspend fun getDisciplinasResumoSuspend(): List<DisciplinaResumo> {
        return disciplinaRepository.getDisciplinasResumo().first()
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private suspend fun getAvaliacoesAtivasSuspend(): List<AvaliacaoModel> {
        return avaliacaoRepository.getAvaliacao().first()
    }
}