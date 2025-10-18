package com.example.unihub.ui.ManterGrupo

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.ui.text.toLowerCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Contato
import com.example.unihub.data.model.Grupo
import com.example.unihub.data.repository.ContatoRepository
import com.example.unihub.data.repository.GrupoRepository
import com.example.unihub.ui.ListarContato.ContatoResumoUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import kotlin.text.lowercase
import kotlin.text.mapNotNull

data class ManterGrupoUiState(
    val nome: String = "",
    val grupoIdAtual: String? = null,
    val membrosDoGrupo: List<ContatoResumoUi> = emptyList(),
    val isLoading: Boolean = false,
    val erro: String? = null,
    val sucesso: Boolean = false,
    val isExclusao: Boolean = false, // Para controlar o fluxo após exclusão
    val todosOsContatosDisponiveis: List<ContatoResumoUi> = emptyList(),
    val isLoadingAllContatos: Boolean = false,
    val errorLoadingAllContatos: String? = null

)

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
class ManterGrupoViewModel(
    private val grupoRepository: GrupoRepository,
    private val contatoRepository: ContatoRepository
) : ViewModel() {


    private val _uiState = MutableStateFlow(ManterGrupoUiState())
    val uiState: StateFlow<ManterGrupoUiState> = _uiState.asStateFlow()
    private val _idMembrosSelecionados = MutableStateFlow<Set<Long>>(emptySet())

    init {
        loadAllAvailableContatos()
        viewModelScope.launch {
            _idMembrosSelecionados.collect { idsAtuais ->
                val membrosAtualizados = _uiState.value.todosOsContatosDisponiveis
                    .filter { contato -> idsAtuais.contains(contato.id) }
                _uiState.update { it.copy(membrosDoGrupo = membrosAtualizados) }
            }
        }
    }
    // Para carregar os dados de um Grupo existente para edição
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun loadGrupo(id: String) {
        val longId = id.toLongOrNull()
        if (longId != null) {
            _uiState.value = _uiState.value.copy(isLoading = true, erro = null)
            viewModelScope.launch {
                try {
                  // e que Grupo tem uma propriedade 'membros' (List<Contato> ou List<Long>).
                    grupoRepository.getGrupoById(longId).collect { grupo ->
                        if (grupo != null) {
                            _uiState.update {
                                it.copy(
                                    nome = grupo.nome ?: "",
                                    isLoading = false
                                )
                            }
                            val idsDosMembrosDoGrupo = grupo.membros.mapNotNull { membro -> membro.id }.toSet()
                            _idMembrosSelecionados.value = idsDosMembrosDoGrupo
                        } else {
                            _uiState.update { it.copy(erro = "Grupo não encontrado", isLoading = false) }
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(erro = e.message, isLoading = false) }
                }
            }
        } else {
            _uiState.update { it.copy(erro = "ID de Grupo inválido", grupoIdAtual = null) }
        }
    }


    fun createGrupo(nome: String) {
        if (nome.isBlank()) {
            _uiState.update { it.copy(erro = "Nome é obrigatório.") }
            return
        }
        _uiState.update { it.copy(isLoading = true, erro = null, isExclusao = false) }
        viewModelScope.launch {
            try {
                val idsMembrosParaSalvar = _idMembrosSelecionados.value.toList()

                // 1. Filtra 'todosOsContatosDisponiveis' para obter os ContatoResumoUi selecionados
                val resumosDosMembrosSelecionados = _uiState.value.todosOsContatosDisponiveis
                    .filter { contatoResumo -> idsMembrosParaSalvar.contains(contatoResumo.id) }

                // 2. Mapeia os ContatoResumoUi para objetos Contato completos
                val membrosCompletosParaSalvar = resumosDosMembrosSelecionados.map { resumo ->
                    Contato(
                        id = resumo.id,
                        nome = resumo.nome,
                        email = resumo.email
                      )
                }

                val novoGrupo = Grupo(nome = nome, membros = membrosCompletosParaSalvar)

                val grupoAdicionado = grupoRepository.addGrupo(novoGrupo) // Supondo que addGrupo retorne o grupo ou um booleano
                _uiState.update { it.copy(sucesso = true, isLoading = false) } // Ajuste 'sucesso' baseado no retorno de addGrupo
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.message, isLoading = false, sucesso = false) }
            }
        }
    }

    fun updateGrupo(id: String, nome: String) {
        val longId = id.toLongOrNull()
        if (longId == null) {
            _uiState.update { it.copy(erro = "ID de Grupo inválido para atualização.") }
            return
        }
        if (nome.isBlank()) {
            _uiState.update { it.copy(erro = "Nome é obrigatório.") }
            return
        }
        _uiState.update { it.copy(isLoading = true, erro = null, isExclusao = false) }
        viewModelScope.launch {
            try {
                //  Obter a lista atual de IDs de membros selecionados.
                val idsMembrosParaSalvar = _idMembrosSelecionados.value.toList()
                val membrosParaSalvar = _uiState.value.todosOsContatosDisponiveis
                    .filter { idsMembrosParaSalvar.contains(it.id) }
                    .map { Contato(id=it.id, nome=it.nome, email=it.email /* ajuste outros campos */) } // Ajuste a conversão

                val grupoAtualizado = Grupo(id = longId, nome = nome, membros = membrosParaSalvar)
                val result = grupoRepository.updateGrupo(grupoAtualizado)
                _uiState.update { it.copy(sucesso = result, isLoading = false) }
                if (!result) {
                    _uiState.update { it.copy(erro = "Falha ao atualizar o Grupo.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.message, isLoading = false, sucesso = false) }
            }
        }
    }

    fun deleteGrupo(id: String) {

            _uiState.value = _uiState.value.copy(isLoading = true, erro = null, isExclusao = true)
            viewModelScope.launch {
                try {
                    val result = grupoRepository.deleteGrupo(id) // deleteGrupo retorna Boolean
                    _uiState.value = _uiState.value.copy(sucesso = result, isLoading = false)
                    if (!result) {
                        _uiState.value = _uiState.value.copy(erro = "Falha ao excluir o Grupo.")
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(erro = e.message, isLoading = false, sucesso = false)
                }
            }
    }

    // Função para carregar todos os contatos disponíveis (mantida do seu código)
    // Nenhuma mudança necessária aqui se ContatoRepository está injetado
    fun loadAllAvailableContatos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAllContatos = true, errorLoadingAllContatos = null) }
            contatoRepository.getContatoResumo() // usa o ContatoRepository injetado
                .map { listaDeModelosContato -> // listaDeModelosContato é List<com.example.unihub.data.model.Contato>
                    listaDeModelosContato
                        .filter { !it.pendente }
                        .mapNotNull { modeloContato -> // modeloContato é com.example.unihub.data.model.Contato
                            // Se id, nome, ou email forem essenciais e puderem ser nulos, decida como tratar.
                            // Aqui, estamos usando o operador elvis (?:) para fornecer padrões ou pular o item.
                            val id = modeloContato.id
                            val nome = modeloContato.nome
                            val email = modeloContato.email

                            if (id != null && nome != null && email != null) { // Garante que campos essenciais não são nulos
                                ContatoResumoUi(
                                    id = id,
                                    nome = nome,
                                    email = email,
                                    pendente = modeloContato.pendente
                                )
                            } else {
                                null // Se algum campo essencial for nulo, não inclua este contato no resultado do mapNotNull
                            }
                        }
                        .sortedBy { it.nome.lowercase() } // Agora 'it' é ContatoResumoUi (não nulo)
                }
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingAllContatos = false,
                            errorLoadingAllContatos = "Falha ao carregar contatos disponíveis: ${e.message}"
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

    //  para manipular a seleção de membros
    fun addMembroAoGrupoPeloId(contatoId: Long) {
        _idMembrosSelecionados.update { currentIds -> currentIds + contatoId }
    }

    fun removeMembroDoGrupoPeloId(contatoId: Long) {
        _idMembrosSelecionados.update { currentIds -> currentIds - contatoId }
    }

    // para definir o nome do grupo a partir da UI
    fun setNomeGrupo(nome: String) {
        _uiState.update { it.copy(nome = nome) }
    }


    fun onEventoConsumido() {
        _uiState.update { it.copy(sucesso = false, erro = null, isExclusao = false, errorLoadingAllContatos = null) }
    }
}
