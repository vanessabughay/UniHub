package com.example.unihub.ui.ManterAvaliacao

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.dto.AvaliacaoRequestDto
import com.example.unihub.data.dto.ContatoIdDto
import com.example.unihub.data.dto.DisciplinaIdDto
import com.example.unihub.data.model.EstadoAvaliacao
import com.example.unihub.data.model.Modalidade
import com.example.unihub.data.model.Prioridade
import com.example.unihub.data.repository.AvaliacaoRepository
import com.example.unihub.data.repository.ContatoRepository
import com.example.unihub.data.repository.DisciplinaRepository
import com.example.unihub.ui.ListarContato.ContatoResumoUi
import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.text.lowercase
import com.example.unihub.ui.Shared.NotaCampo
import com.example.unihub.ui.Shared.PesoCampo

data class DisciplinaUiItem(
    val id: Long,
    val nome: String
)

data class ManterAvaliacaoUiState(
    val avaliacaoIdAtual: String? = null,
    val descricao: String = "",

    val disciplinaIdSelecionada: Long? = null,
    val nomeDisciplinaSelecionada: String = "",

    val tipoAvaliacao: String = "",

    val modalidade: Modalidade = Modalidade.INDIVIDUAL,
    val prioridade: Prioridade = Prioridade.MEDIA,
    val receberNotificacoes: Boolean = true,

    // agora usamos data + hora
    val dataEntrega: String = "", // "yyyy-MM-dd"
    val horaEntrega: String = "", // "HH:mm"

    val peso: String = "",
    val estado: EstadoAvaliacao = EstadoAvaliacao.A_REALIZAR,
    val nota: String = "",
    val dificuldade: String = "",

    val integrantesDaAvaliacao: List<ContatoResumoUi> = emptyList(),
    val todosOsContatosDisponiveis: List<ContatoResumoUi> = emptyList(),

    val isLoading: Boolean = false,
    val erro: String? = null,
    val sucesso: Boolean = false,
    val isExclusao: Boolean = false,
    val isLoadingAllContatos: Boolean = false,
    val errorLoadingAllContatos: String? = null,

    val todasDisciplinasDisponiveis: List<DisciplinaUiItem> = emptyList(),
    val isLoadingDisciplinas: Boolean = false,
    val errorLoadingDisciplinas: String? = null,

    val todasPrioridades: List<Prioridade> = Prioridade.values().toList(),
    val todasModalidades: List<Modalidade> = Modalidade.values().toList(),
    val todosEstados: List<EstadoAvaliacao> = EstadoAvaliacao.values().toList()
)

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
class ManterAvaliacaoViewModel(
    private val avaliacaoRepository: AvaliacaoRepository,
    private val contatoRepository: ContatoRepository,
    private val disciplinaRepository: DisciplinaRepository? = null,
    private var disciplinaIdParaPreselecionar: Long? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManterAvaliacaoUiState())
    val uiState: StateFlow<ManterAvaliacaoUiState> = _uiState.asStateFlow()
    private val _idIntegrantesSelecionados = MutableStateFlow<Set<Long>>(emptySet())

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE // yyyy-MM-dd
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm") // 24h

    init {
        loadAllAvailableContatos()
        loadAllDisciplinasDisponiveis()

        viewModelScope.launch {
            _idIntegrantesSelecionados.collect { idsAtuais ->
                val integrantesAtualizados = _uiState.value.todosOsContatosDisponiveis
                    .filter { contato ->
                        val registroId = contato.registroId
                        registroId != null && idsAtuais.contains(registroId)
                    }
                _uiState.update { it.copy(integrantesDaAvaliacao = integrantesAtualizados) }
            }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun loadAvaliacao(id: String) {
        val longId = id.toLongOrNull()
        if (longId != null) {
            _uiState.update { it.copy(isLoading = true, erro = null, avaliacaoIdAtual = id) }
            viewModelScope.launch {
                try {
                    avaliacaoRepository.getAvaliacaoById(longId).collect { avaliacao ->
                        if (avaliacao != null) {
                            val (dataUi, horaUi) = splitBackendDateTime(avaliacao.dataEntrega)

                            _uiState.update { currentState ->
                                currentState.copy(
                                    descricao = avaliacao.descricao ?: "",
                                    disciplinaIdSelecionada = avaliacao.disciplina?.id,
                                    nomeDisciplinaSelecionada = avaliacao.disciplina?.nome ?: "",
                                    tipoAvaliacao = avaliacao.tipoAvaliacao ?: "",
                                    modalidade = avaliacao.modalidade ?: Modalidade.INDIVIDUAL,
                                    prioridade = avaliacao.prioridade ?: Prioridade.MEDIA,
                                    receberNotificacoes = avaliacao.receberNotificacoes != false,

                                    dataEntrega = dataUi,
                                    horaEntrega = horaUi,

                                    peso = PesoCampo.fromDouble(avaliacao.peso),
                                    estado = avaliacao.estado,
                                    nota = NotaCampo.fromDouble(avaliacao.nota),
                                    // dificuldade = avaliacao.dificuldade?.toString() ?: "",
                                    isLoading = false
                                )
                            }
                            val idsDosIntegrantes = avaliacao.integrantes.mapNotNull { it.id }.toSet()
                            _idIntegrantesSelecionados.value = idsDosIntegrantes
                        } else {
                            _uiState.update { it.copy(erro = "Avaliação não encontrada", isLoading = false) }
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(erro = "Erro ao carregar avaliação: ${e.message}", isLoading = false) }
                }
            }
        } else {
            _uiState.update { it.copy(erro = "ID de Avaliação inválido", avaliacaoIdAtual = null) }
        }
    }

    private fun splitBackendDateTime(raw: String?): Pair<String, String> {
        if (raw.isNullOrBlank()) return "" to ""
        // tenta LocalDateTime com vários formatos
        val dtPatterns = listOf(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,           // 2025-10-01T14:30:00
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"), // 2025-10-01 14:30
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")// 2025-10-01T14:30
        )
        for (fmt in dtPatterns) {
            try {
                val ldt = LocalDateTime.parse(raw, fmt)
                return ldt.toLocalDate().format(dateFormatter) to ldt.toLocalTime().format(timeFormatter)
            } catch (_: Exception) { /* tenta próximo */ }
        }
        // tenta só data
        return try {
            val ld = LocalDate.parse(raw, dateFormatter)
            ld.format(dateFormatter) to ""
        } catch (_: Exception) {
            // fallback bruto (não quebra a tela)
            raw to ""
        }
    }

    private fun getLocalDateTimeFromUi(): LocalDateTime? {
        val st = _uiState.value
        if (st.dataEntrega.isBlank() || st.horaEntrega.isBlank()) return null
        return try {
            val datePart = LocalDate.parse(st.dataEntrega, dateFormatter)
            val timePart = LocalTime.parse(st.horaEntrega, timeFormatter)
            LocalDateTime.of(datePart, timePart)
        } catch (_: DateTimeParseException) {
            null
        }
    }

    private fun validateInput(): Boolean {
        val st = _uiState.value
        if (st.descricao.isBlank()) {
            _uiState.update { it.copy(erro = "Descrição é obrigatória.") }
            return false
        }
        if (st.disciplinaIdSelecionada == null) {
            _uiState.update { it.copy(erro = "Disciplina é obrigatória.") }
            return false
        }
        // se preencher data OU hora, precisa dos dois e formato válido
        if (st.dataEntrega.isNotBlank() || st.horaEntrega.isNotBlank()) {
            if (st.dataEntrega.isBlank() || st.horaEntrega.isBlank()) {
                _uiState.update { it.copy(erro = "Se fornecer Data ou Hora de entrega, ambos são necessários.") }
                return false
            }
            if (getLocalDateTimeFromUi() == null) {
                _uiState.update { it.copy(erro = "Formato inválido. Use Data AAAA-MM-DD e Hora HH:MM.") }
                return false
            }
        }
        if (st.peso.isNotEmpty() && PesoCampo.toDouble(st.peso) == null) {
            _uiState.update { it.copy(erro = "Peso inválido. Use números inteiros de 0 a 100.") }
            return false
        }
        if (st.nota.isNotEmpty() && NotaCampo.toDouble(st.nota) == null) {
            _uiState.update { it.copy(erro = "Nota inválida. Use formato como 8,5.") }
            return false
        }
        return true
    }

    fun createAvaliacao() {
        if (!validateInput()) return
        _uiState.update { it.copy(isLoading = true, erro = null, isExclusao = false) }
        viewModelScope.launch {
            try {
                val req = buildAvaliacaoRequest()
                avaliacaoRepository.addAvaliacao(req)
                _uiState.update { it.copy(sucesso = true, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = "Erro ao criar avaliação: ${e.message}", isLoading = false) }
            }
        }
    }

    fun updateAvaliacao() {
        val id = _uiState.value.avaliacaoIdAtual?.toLongOrNull() ?: run {
            _uiState.update { it.copy(erro = "ID inválido para atualização.") }
            return
        }
        if (!validateInput()) return
        _uiState.update { it.copy(isLoading = true, erro = null, isExclusao = false) }
        viewModelScope.launch {
            try {
                val req = buildAvaliacaoRequest(id)
                val ok = avaliacaoRepository.updateAvaliacao(id, req)
                _uiState.update { it.copy(sucesso = ok, isLoading = false, erro = if (ok) null else "Falha ao atualizar") }
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = "Erro ao atualizar avaliação: ${e.message}", isLoading = false) }
            }
        }
    }

    // setters
    fun setDescricao(descricao: String) { _uiState.update { it.copy(descricao = descricao, erro = null) } }
    fun onDisciplinaSelecionada(disciplinaItem: DisciplinaUiItem?) {
        _uiState.update {
            it.copy(
                disciplinaIdSelecionada = disciplinaItem?.id,
                nomeDisciplinaSelecionada = disciplinaItem?.nome ?: "",
                erro = null
            )
        }
    }
    fun setTipoAvaliacao(tipo: String) { _uiState.update { it.copy(tipoAvaliacao = tipo, erro = null) } }
    fun setModalidade(modalidade: Modalidade) { _uiState.update { it.copy(modalidade = modalidade, erro = null) } }
    fun setDataEntrega(data: String) { _uiState.update { it.copy(dataEntrega = data, erro = null) } }
    fun setHoraEntrega(hora: String) { _uiState.update { it.copy(horaEntrega = hora, erro = null) } }
    fun setPeso(peso: String) {
        val sanitized = PesoCampo.sanitize(peso)
        _uiState.update { it.copy(peso = sanitized, erro = null) }
    }
    fun setPrioridade(prioridade: Prioridade) { _uiState.update { it.copy(prioridade = prioridade, erro = null) } }
    fun setReceberNotificacoes(receber: Boolean) { _uiState.update { it.copy(receberNotificacoes = receber, erro = null) } }
    fun setEstado(estado: EstadoAvaliacao) { _uiState.update { it.copy(estado = estado, erro = null) } }
    fun setNota(nota: String) {
        val sanitized = NotaCampo.sanitize(nota)
        _uiState.update { it.copy(nota = sanitized, erro = null) }
    }

    fun deleteAvaliacao(id: String) {
        _uiState.update { it.copy(isLoading = true, erro = null, isExclusao = true) }
        viewModelScope.launch {
            try {
                val longId = id.toLongOrNull()
                if (longId == null) {
                    _uiState.update { it.copy(erro = "ID inválido para exclusão.", isLoading = false, sucesso = false) }
                    return@launch
                }
                val result = avaliacaoRepository.deleteAvaliacao(id)
                _uiState.update { it.copy(sucesso = result, isLoading = false) }
                if (!result) _uiState.update { it.copy(erro = "Falha ao excluir a Avaliação.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = "Erro ao excluir avaliação: ${e.message}", isLoading = false, sucesso = false) }
            }
        }
    }

    fun loadAllAvailableContatos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAllContatos = true, errorLoadingAllContatos = null) }
            contatoRepository.getContatoResumo()
                .map { lista ->
                    lista
                        .filter { !it.pendente }
                        .mapNotNull { m ->
                            val registroId = m.registroId ?: return@mapNotNull null
                            ContatoResumoUi(
                                id = m.id,
                                nome = m.nome,
                                email = m.email,
                                pendente = m.pendente,
                                registroId = registroId,
                                ownerId = m.ownerId
                            )
                        }
                        .sortedBy { it.nome.lowercase() }
                }
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingAllContatos = false,
                            errorLoadingAllContatos = "Falha ao carregar contatos: ${e.message}"
                        )
                    }
                }
                .collect { contatos ->
                    _uiState.update {
                        it.copy(
                            todosOsContatosDisponiveis = contatos,
                            isLoadingAllContatos = false
                        )
                    }
                }
        }
    }

    private fun loadAllDisciplinasDisponiveis() {
        disciplinaRepository?.let { repo ->
            viewModelScope.launch {
                _uiState.update { it.copy(isLoadingDisciplinas = true, errorLoadingDisciplinas = null) }
                try {
                    repo.getDisciplinasResumo()
                        .map { list ->
                            list.mapNotNull { dr ->
                                dr.id?.let { id -> DisciplinaUiItem(id = id, nome = dr.nome) }
                            }.sortedBy { it.nome.lowercase() }
                        }
                        .catch { e ->
                            _uiState.update {
                                it.copy(
                                    isLoadingDisciplinas = false,
                                    errorLoadingDisciplinas = "Falha ao carregar disciplinas: ${e.message}"
                                )
                            }
                        }
                        .collect { disciplinasUi ->
                            _uiState.update {
                                it.copy(
                                    todasDisciplinasDisponiveis = disciplinasUi,
                                    isLoadingDisciplinas = false
                                )
                            }
                            disciplinaIdParaPreselecionar?.let { pend ->
                                if (_uiState.value.disciplinaIdSelecionada == null) {
                                    disciplinasUi.firstOrNull { it.id == pend }?.let { onDisciplinaSelecionada(it) }
                                }
                                disciplinaIdParaPreselecionar = null
                            }
                        }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoadingDisciplinas = false,
                            errorLoadingDisciplinas = "Exceção ao carregar disciplinas: ${e.message}"
                        )
                    }
                }
            }
        } ?: run {
            _uiState.update { it.copy(errorLoadingDisciplinas = "Repositório de disciplinas não configurado.") }
        }
    }

    fun addIntegrantePeloId(contatoId: Long) { _idIntegrantesSelecionados.update { it + contatoId } }
    fun removeIntegrantePeloId(contatoId: Long) { _idIntegrantesSelecionados.update { it - contatoId } }

    fun onEventoConsumido() {
        _uiState.update {
            it.copy(
                sucesso = false,
                erro = null,
                isExclusao = false,
                errorLoadingAllContatos = null,
                errorLoadingDisciplinas = null
            )
        }
    }

    private fun buildAvaliacaoRequest(idParaAtualizar: Long? = null): AvaliacaoRequestDto {
        val s = _uiState.value
        val integrantesIds = _idIntegrantesSelecionados.value

        // monta dataEntrega final:
        val dataFinal: String? = when {
            s.dataEntrega.isBlank() && s.horaEntrega.isBlank() -> null
            s.dataEntrega.isNotBlank() && s.horaEntrega.isNotBlank() ->
                "${s.dataEntrega}T${s.horaEntrega}:00" // <-- garante HH:mm:ss
            else -> s.dataEntrega
        }

        val notaDouble = NotaCampo.toDouble(s.nota)
        val pesoDouble = PesoCampo.toDouble(s.peso)

        return AvaliacaoRequestDto(
            id = idParaAtualizar,
            descricao = s.descricao.takeIf { it.isNotBlank() },
            disciplina = s.disciplinaIdSelecionada?.let { DisciplinaIdDto(it) },
            tipoAvaliacao = s.tipoAvaliacao.takeIf { it.isNotBlank() },
            modalidade = s.modalidade,
            dataEntrega = dataFinal,
            nota = notaDouble,
            peso = pesoDouble,
            integrantes = integrantesIds.map { ContatoIdDto(it) },
            prioridade = s.prioridade,
            estado = s.estado,
            dificuldade = s.dificuldade.toIntOrNull(),
            receberNotificacoes = s.receberNotificacoes
        )
    }

    fun preselectDisciplinaId(disciplinaIdStr: String) {
        val discId = disciplinaIdStr.toLongOrNull() ?: return
        disciplinaIdParaPreselecionar = discId
        val lista = _uiState.value.todasDisciplinasDisponiveis
        if (lista.isNotEmpty() && _uiState.value.disciplinaIdSelecionada == null) {
            lista.firstOrNull { it.id == discId }?.let { onDisciplinaSelecionada(it) }
        }
    }

}
