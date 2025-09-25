package com.example.unihub.ui.ManterAvaliacao

import android.os.Build
import androidx.annotation.RequiresExtension
// Removido: import androidx.core.graphics.values // Não parece estar sendo usado
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.dto.AvaliacaoRequest
import com.example.unihub.data.dto.ContatoRef
import com.example.unihub.data.dto.DisciplinaRef
import com.example.unihub.data.model.Avaliacao
import com.example.unihub.data.model.Contato
import com.example.unihub.data.model.Disciplina
import com.example.unihub.data.model.EstadoAvaliacao
import com.example.unihub.data.model.Modalidade // Certifique-se que este enum tem INDIVIDUAL e EM_GRUPO
import com.example.unihub.data.model.Prioridade // Certifique-se que este enum tem os 5 níveis e um displayName
import com.example.unihub.data.repository.AvaliacaoRepository
import com.example.unihub.data.repository.ContatoRepository
import com.example.unihub.data.repository.DisciplinaRepository
import com.example.unihub.ui.ListarContato.ContatoResumoUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.text.lowercase

// Removido: import kotlin.text.toList // Não parece estar sendo usado explicitamente aqui

data class DisciplinaUiItem(
    val id: Long,
    val nome: String
)

// ManterAvaliacaoUiState já está bem próximo.
// Apenas confirme se os enums Modalidade e Prioridade estão como esperado.
data class ManterAvaliacaoUiState(
    val avaliacaoIdAtual: String? = null,
    val descricao: String = "",

    val disciplinaIdSelecionada: Long? = null,
    val nomeDisciplinaSelecionada: String = "",

    val tipoAvaliacao: String = "", // Campo para digitar

    val modalidade: Modalidade = Modalidade.INDIVIDUAL, // Escolher entre GRUPO ou INDIVIDUAL
    val prioridade: Prioridade = Prioridade.MEDIA, // Escolher entre (Muito baixa, ..., Muito alta)
    val receberNotificacoes: Boolean = false, // Caixa de marcação checkbox

    // Campos a serem "desativados" (não obrigatórios na validação por enquanto)
    val dataEntrega: String = "",
    val horaEntrega: String = "",
    val peso: String = "",

    // Outros campos existentes
    val estado: EstadoAvaliacao = EstadoAvaliacao.A_REALIZAR, // Este campo não foi explicitamente pedido para a nova lista, mas já existe. Decida se mantém.
    val nota: String = "", // Se não for mais usado nesta tela, pode remover.
    val dificuldade: String = "", // Se não for mais usado nesta tela, pode remover.

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

    // Listas para os dropdowns
    val todasPrioridades: List<Prioridade> = Prioridade.values().toList(),
    val todasModalidades: List<Modalidade> = Modalidade.values().toList(),
    val todosEstados: List<EstadoAvaliacao> = EstadoAvaliacao.values().toList() // Se mantiver o campo estado
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

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    init {
        loadAllAvailableContatos()
        loadAllDisciplinasDisponiveis()

        viewModelScope.launch {
            _idIntegrantesSelecionados.collect { idsAtuais ->
                val integrantesAtualizados = _uiState.value.todosOsContatosDisponiveis
                    .filter { contato -> idsAtuais.contains(contato.id) }
                _uiState.update { it.copy(integrantesDaAvaliacao = integrantesAtualizados)
                }
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
                            val dataEntregaStringDoBackend = avaliacao.dataEntrega
                            _uiState.update { currentState ->
                                currentState.copy(
                                    descricao = avaliacao.descricao ?: "",
                                    disciplinaIdSelecionada = avaliacao.disciplina?.id,
                                    nomeDisciplinaSelecionada = avaliacao.disciplina?.nome ?: "",
                                    tipoAvaliacao = avaliacao.tipoAvaliacao ?: "",
                                    modalidade = avaliacao.modalidade ?: Modalidade.INDIVIDUAL,
                                    prioridade = avaliacao.prioridade ?: Prioridade.MEDIA,
                                    receberNotificacoes = avaliacao.receberNotificacoes ?: false,
                                    // Data e Peso podem vir nulos do backend se forem opcionais
                                    dataEntrega = dataEntregaStringDoBackend ?: "", // Se for null do backend, UI fica vazia
                                    horaEntrega = "",
                                    peso = avaliacao.peso?.toString() ?: "",
                                    estado = avaliacao.estado, // Se mantiver
                                    nota = avaliacao.nota?.toString() ?: "", // Se mantiver
                                    // dificuldade = avaliacao.dificuldade?.toString() ?: "", // Se mantiver
                                    isLoading = false
                                )
                            }
                            val idsDosIntegrantes = avaliacao.integrantes.mapNotNull { integrante -> integrante.id }.toSet()
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

    private fun getLocalDateTimeFromUi(): LocalDateTime? {
        val currentState = _uiState.value
        if (currentState.dataEntrega.isBlank() || currentState.horaEntrega.isBlank()) {
            return null // Se data ou hora estiverem em branco, não há LocalDateTime completo
        }
        return try {
            val datePart = LocalDate.parse(currentState.dataEntrega, dateFormatter)
            val timePart = java.time.LocalTime.parse(currentState.horaEntrega, timeFormatter)
            LocalDateTime.of(datePart, timePart)
        } catch (e: DateTimeParseException) {
            null // Formato inválido
        }
    }

    private fun validateInput(): Boolean {
        val currentState = _uiState.value
        if (currentState.descricao.isBlank()) {
            _uiState.update { it.copy(erro = "Descrição é obrigatória.") }
            return false
        }
        if (currentState.disciplinaIdSelecionada == null) {
            _uiState.update { it.copy(erro = "Disciplina é obrigatória.") }
            return false
        }

        // Validação de Data/Hora (apenas se preenchido, devido a "deixar desativado")
        if (currentState.dataEntrega.isNotBlank() || currentState.horaEntrega.isNotBlank()) {
            // Se um está preenchido, o outro também deveria estar para formar um LocalDateTime
            if (currentState.dataEntrega.isBlank() || currentState.horaEntrega.isBlank()) {
                _uiState.update { it.copy(erro = "Se fornecer Data ou Hora de entrega, ambos são necessários.") }
                return false
            }
            if (getLocalDateTimeFromUi() == null) {
                _uiState.update { it.copy(erro = "Formato de Data ou Hora de entrega inválido. Use AAAA-MM-DD e HH:MM.") }
                return false
            }
        }

        // Validação de Peso (apenas se preenchido)
        if (currentState.peso.isNotBlank() && currentState.peso.toDoubleOrNull() == null) {
            _uiState.update { st -> st.copy(erro = "Peso inválido. Deve ser um número.") }
            return false
        }
        // Adicione validações para 'tipo' se for obrigatório
        // if (currentState.tipoAvaliacao.isBlank()) {
        //     _uiState.update { it.copy(erro = "Tipo é obrigatório.") }
        //     return false
        // }
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
            _uiState.update { it.copy(erro = "ID inválido para atualização.") }; return
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

    // --- Funções Setter (a maioria já existe e está correta) ---
    fun setDescricao(descricao: String) {
        _uiState.update { it.copy(descricao = descricao, erro = null) }
    }

    fun onDisciplinaSelecionada(disciplinaItem: DisciplinaUiItem?) {
        _uiState.update {
            it.copy(
                disciplinaIdSelecionada = disciplinaItem?.id,
                nomeDisciplinaSelecionada = disciplinaItem?.nome ?: "",
                erro = null
            )
        }
    }

    fun setTipoAvaliacao(tipo: String) {
        _uiState.update { it.copy(tipoAvaliacao = tipo, erro = null) }
    }

    fun setModalidade(modalidade: Modalidade) {
        _uiState.update { it.copy(modalidade = modalidade, erro = null) }
    }

    fun setDataEntrega(data: String) {
        _uiState.update { it.copy(dataEntrega = data, erro = null) }
    }

    fun setHoraEntrega(hora: String) {
        _uiState.update { it.copy(horaEntrega = hora, erro = null) }
    }

    fun setPeso(peso: String) {
        _uiState.update { it.copy(peso = peso, erro = null) }
    }

    fun setPrioridade(prioridade: Prioridade) {
        _uiState.update { it.copy(prioridade = prioridade, erro = null) }
    }

    fun setReceberNotificacoes(receber: Boolean) {
        _uiState.update { it.copy(receberNotificacoes = receber, erro = null) }
    }

    // Se mantiver 'estado', 'nota', 'dificuldade', os setters existentes são:
    fun setEstado(estado: EstadoAvaliacao) {
        _uiState.update { it.copy(estado = estado, erro = null) }
    }
    fun setNota(nota: String) { // Se for usado
        _uiState.update { it.copy(nota = nota, erro = null) }
    }
    // fun setDificuldade(dificuldade: String) { // Se for usado
    //     _uiState.update { it.copy(dificuldade = dificuldade, erro = null) }
    // }


    // --- Funções de gerenciamento de integrantes e eventos (já existem) ---
    fun deleteAvaliacao(id: String) { // Seu código existente
        _uiState.update { it.copy(isLoading = true, erro = null, isExclusao = true) }
        viewModelScope.launch {
            try {
                val longId = id.toLongOrNull()
                if (longId == null) {
                    _uiState.update { it.copy(erro = "ID inválido para exclusão.", isLoading = false, sucesso = false) }
                    return@launch
                }
                val result = avaliacaoRepository.deleteAvaliacao(id) // Assumindo que deleteAvaliacao espera String
                _uiState.update { it.copy(sucesso = result, isLoading = false) }
                if (!result) {
                    _uiState.update { it.copy(erro = "Falha ao excluir a Avaliação.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = "Erro ao excluir avaliação: ${e.message}", isLoading = false, sucesso = false) }
            }
        }
    }

    fun loadAllAvailableContatos() { // Seu código existente
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAllContatos = true, errorLoadingAllContatos = null) }
            contatoRepository.getContatoResumo()
                .map { listaDeModelosContato ->
                    listaDeModelosContato.mapNotNull { modeloContato ->
                        modeloContato.id?.let { id -> // Garante que id não é nulo
                            ContatoResumoUi(
                                id = id,
                                nome = modeloContato.nome ?: "", // Lida com nome nulo
                                email = modeloContato.email ?: "" // Lida com email nulo
                            )
                        }
                    }.sortedBy { it.nome.lowercase() }
                }
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingAllContatos = false,
                            errorLoadingAllContatos = "Falha ao carregar contatos: ${e.message}"
                        )
                    }
                }
                .collect { contatosParaUi ->
                    _uiState.update {
                        it.copy(
                            todosOsContatosDisponiveis = contatosParaUi,
                            isLoadingAllContatos = false
                        )
                    }
                }
        }
    }

    private fun loadAllDisciplinasDisponiveis() { // Seu código existente
        disciplinaRepository?.let { repo ->
            viewModelScope.launch {
                _uiState.update { it.copy(isLoadingDisciplinas = true, errorLoadingDisciplinas = null) }
                try {
                    repo.getDisciplinasResumo() // Retorna Flow<List<DisciplinaResumo>>
                        .map { disciplinasResumo ->
                            disciplinasResumo.mapNotNull { dr ->
                                dr.id?.let { id -> // Usa o id do DisciplinaResumo
                                    DisciplinaUiItem(id = id, nome = dr.nome) // Usa o nome do DisciplinaResumo
                                }
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
                                // não sobrescreve se já houver disciplina selecionada (ex.: ao editar)
                                if (_uiState.value.disciplinaIdSelecionada == null) {
                                    disciplinasUi.firstOrNull { it.id == pend }
                                        ?.let { onDisciplinaSelecionada(it) }
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

    fun addIntegrantePeloId(contatoId: Long) { // Seu código existente
        _idIntegrantesSelecionados.update { currentIds -> currentIds + contatoId }
    }

    fun removeIntegrantePeloId(contatoId: Long) { // Seu código existente
        _idIntegrantesSelecionados.update { currentIds -> currentIds - contatoId }
    }

    fun onEventoConsumido() { // Seu código existente
        _uiState.update { it.copy(sucesso = false, erro = null, isExclusao = false, errorLoadingAllContatos = null, errorLoadingDisciplinas = null) }
    }

    private fun buildAvaliacaoRequest(idParaAtualizar: Long? = null): AvaliacaoRequest {
        val s = _uiState.value
        val integrantesIds = _idIntegrantesSelecionados.value
        return AvaliacaoRequest(
            id = idParaAtualizar,
            descricao = s.descricao.takeIf { it.isNotBlank() },
            disciplina = s.disciplinaIdSelecionada?.let { DisciplinaRef(it) }, // <- aqui é DisciplinaRef
            tipoAvaliacao = s.tipoAvaliacao.takeIf { it.isNotBlank() },
            modalidade = s.modalidade,
            dataEntrega = s.dataEntrega.takeIf { it.isNotBlank() },
            nota = s.nota.toDoubleOrNull(),
            peso = s.peso.toDoubleOrNull(),
            integrantes = integrantesIds.map { ContatoRef(it) },
            prioridade = s.prioridade,
            estado = s.estado,
            dificuldade = s.dificuldade.toIntOrNull(),
            receberNotificacoes = s.receberNotificacoes
        )
    }

    fun preselectDisciplinaId(disciplinaIdStr: String) {
        val discId = disciplinaIdStr.toLongOrNull() ?: return
        disciplinaIdParaPreselecionar = discId

        // se a lista já estiver carregada, seleciona agora
        val lista = _uiState.value.todasDisciplinasDisponiveis
        if (lista.isNotEmpty() && _uiState.value.disciplinaIdSelecionada == null) {
            lista.firstOrNull { it.id == discId }?.let { onDisciplinaSelecionada(it) }
        }
    }



}

