package com.example.unihub.ui.ManterGrupo

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.config.TokenManager
import com.example.unihub.data.model.Grupo
import com.example.unihub.data.repository.ContatoRepository
import com.example.unihub.data.repository.GrupoRepository
import com.example.unihub.ui.ListarContato.ContatoResumoUi
import java.io.IOException
import retrofit2.HttpException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

data class ManterGrupoUiState(
    val nome: String = "",
    val grupoIdAtual: String? = null,
    val membrosDoGrupo: List<ContatoResumoUi> = emptyList(),
    val adminContatoId: Long? = null,
    val ownerId: Long? = null,
    val isLoading: Boolean = false,
    val erro: String? = null,
    val sucesso: Boolean = false,
    val isExclusao: Boolean = false, // Para controlar o fluxo após exclusão
    val podeExcluirGrupo: Boolean = false,
    val todosOsContatosDisponiveis: List<ContatoResumoUi> = emptyList(),
    val membrosOriginaisDoGrupo: List<ContatoResumoUi> = emptyList(),
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
                val estadoAtual = _uiState.value
                val contatosDisponiveisPorId = estadoAtual.todosOsContatosDisponiveis.associateBy { it.id }
                val membrosOriginaisPorId = estadoAtual.membrosOriginaisDoGrupo.associateBy { it.id }
                val membrosAtualizados = idsAtuais.mapNotNull { id ->
                    contatosDisponiveisPorId[id] ?: membrosOriginaisPorId[id]
                }
                _uiState.update { it.copy(membrosDoGrupo = membrosAtualizados) }
            }
        }
    }
    // Para carregar os dados de um Grupo existente para edição
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun loadGrupo(id: String) {
        val longId = id.toLongOrNull()
        if (longId != null) {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                erro = null,
                grupoIdAtual = id,
                podeExcluirGrupo = false,
                ownerId = null
            )
            viewModelScope.launch {
                try {
                  // e que Grupo tem uma propriedade 'membros' (List<Contato> ou List<Long>).
                    grupoRepository.getGrupoById(longId).collect { grupo ->
                        if (grupo != null) {
                            val usuarioLogadoId = TokenManager.usuarioId
                            val podeExcluir = calcularPodeExcluirGrupo(grupo, usuarioLogadoId)
                            val membrosDoGrupoServidor = grupo.membros.mapNotNull { membro ->
                                val identificador = membro.idContato ?: membro.id
                                if (identificador != null) {
                                    val nome = membro.nome ?: membro.email ?: "Integrante $identificador"
                                    val email = membro.email ?: membro.nome ?: "Email não informado"
                                    ContatoResumoUi(
                                        id = identificador,
                                        nome = nome,
                                        email = email,
                                        pendente = membro.pendente
                                    )
                                } else {
                                    null
                                }
                            }
                            _uiState.update {
                                it.copy(
                                    nome = grupo.nome ?: "",
                                    adminContatoId = grupo.adminContatoId,
                                    ownerId = grupo.ownerId,
                                    isLoading = false,
                                    podeExcluirGrupo = podeExcluir,
                                    grupoIdAtual = id,
                                    membrosDoGrupo = membrosDoGrupoServidor,
                                    membrosOriginaisDoGrupo = membrosDoGrupoServidor
                                )
                            }
                            val idsDosMembrosDoGrupo = grupo.membros
                                .mapNotNull { membro -> membro.idContato ?: membro.id }
                                .toSet()

                            _idMembrosSelecionados.value = idsDosMembrosDoGrupo
                        } else {
                            _uiState.update {
                                it.copy(
                                    erro = "Grupo não encontrado",
                                    adminContatoId = null,
                                    isLoading = false,
                                    ownerId = null,
                                    podeExcluirGrupo = false,
                                    grupoIdAtual = null,
                                    membrosDoGrupo = emptyList(),
                                    membrosOriginaisDoGrupo = emptyList()
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            erro = "ID de Grupo inválido",
                            grupoIdAtual = null,
                            adminContatoId = null,
                            ownerId = null,
                            podeExcluirGrupo = false,
                            membrosDoGrupo = emptyList(),
                            membrosOriginaisDoGrupo = emptyList()
                        )
                    }
                }
            }
        } else {
            _uiState.update {
                it.copy(
                    erro = "ID de Grupo inválido",
                    grupoIdAtual = null,
                    ownerId = null,
                    podeExcluirGrupo = false,
                    membrosDoGrupo = emptyList(),
                    membrosOriginaisDoGrupo = emptyList()
                )
            }
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
                val usuarioLogadoId = TokenManager.usuarioId
                    ?: throw IllegalStateException("Não foi possível identificar o usuário autenticado. Faça login novamente.")

                val membrosValidados = idsMembrosParaSalvar.map { contatoId ->
                    val contatoCompleto = contatoRepository.fetchContatoById(contatoId)
                        ?: throw IllegalArgumentException("Não foi possível localizar o contato selecionado (ID $contatoId).")

                    val pertenceAoUsuario = contatoCompleto.ownerId == null || contatoCompleto.ownerId == usuarioLogadoId

                    if (!pertenceAoUsuario) {
                        val identificador = contatoCompleto.nome ?: contatoCompleto.email ?: "ID $contatoId"
                        throw IllegalArgumentException("O contato $identificador não pertence à sua agenda.")
                    }

                    if (contatoCompleto.pendente) {
                        val identificador = contatoCompleto.nome ?: contatoCompleto.email ?: "ID $contatoId"
                        throw IllegalArgumentException("O contato $identificador ainda está pendente e não pode ser adicionado ao grupo.")
                    }

                    contatoCompleto
                }

                val novoGrupo = Grupo(
                    nome = nome,
                    membros = membrosValidados,
                    ownerId = usuarioLogadoId
                )

                grupoRepository.addGrupo(novoGrupo)
                _uiState.update { it.copy(sucesso = true, isLoading = false) }
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
                val idsMembrosParaSalvar = _idMembrosSelecionados.value.toList()
                val usuarioLogadoId = TokenManager.usuarioId
                    ?: throw IllegalStateException("Não foi possível identificar o usuário autenticado. Faça login novamente.")
                val membrosValidados = idsMembrosParaSalvar.map { contatoId ->
                    val contatoCompleto = contatoRepository.fetchContatoById(contatoId)
                        ?: throw IllegalArgumentException("Não foi possível localizar o contato selecionado (ID $contatoId).")

                    val pertenceAoUsuario = contatoCompleto.ownerId == null || contatoCompleto.ownerId == usuarioLogadoId

                    if (!pertenceAoUsuario) {
                        val identificador = contatoCompleto.nome ?: contatoCompleto.email ?: "ID $contatoId"
                        throw IllegalArgumentException("O contato $identificador não pertence à sua agenda.")
                    }

                    if (contatoCompleto.pendente) {
                        val identificador = contatoCompleto.nome ?: contatoCompleto.email ?: "ID $contatoId"
                        throw IllegalArgumentException("O contato $identificador ainda está pendente e não pode ser adicionado ao grupo.")
                    }

                    contatoCompleto
                }

                val grupoAtualizado = Grupo(
                    id = longId,
                    nome = nome,
                    membros = membrosValidados,
                    ownerId = usuarioLogadoId
                )
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
                val result = grupoRepository.deleteGrupo(id)
                if (result) {
                    _uiState.value = _uiState.value.copy(sucesso = true, isLoading = false)
                } else {
                    _uiState.value = _uiState.value.copy(
                        sucesso = false,
                        isLoading = false,
                        erro = "Falha ao excluir o Grupo no servidor."
                    )
                }
            } catch (e: IllegalStateException) {
                _uiState.value = _uiState.value.copy(
                    erro = e.message
                        ?: "Não foi possível sair do grupo. Verifique se há outro membro com acesso.",
                    isLoading = false,
                    sucesso = false
                )
            } catch (e: IllegalArgumentException) {
                _uiState.value = _uiState.value.copy(
                    erro = e.message ?: "ID de Grupo inválido.",
                    isLoading = false,
                    sucesso = false
                )
            } catch (e: IOException) {
                _uiState.value = _uiState.value.copy(
                    erro = "Falha de rede ao excluir o Grupo: ${e.message}",
                    isLoading = false,
                    sucesso = false
                )
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(
                    erro = "Erro do servidor (${e.code()}) ao excluir o Grupo.",
                    isLoading = false,
                    sucesso = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    erro = e.message ?: "Erro inesperado ao excluir o Grupo.",
                    isLoading = false,
                    sucesso = false
                )
            }
        }
    }

    // Função para carregar todos os contatos disponíveis (mantida do seu código)
    // Nenhuma mudança necessária aqui se ContatoRepository está injetado
    fun loadAllAvailableContatos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAllContatos = true, errorLoadingAllContatos = null) }
            val usuarioLogadoId = TokenManager.usuarioId
            if (usuarioLogadoId == null) {
                _uiState.update {
                    it.copy(
                        isLoadingAllContatos = false,
                        errorLoadingAllContatos = "Não foi possível identificar o usuário autenticado. Faça login novamente."
                    )
                }
                return@launch
            }
            contatoRepository.getContatoResumo() // usa o ContatoRepository injetado
                .map { listaDeModelosContato -> // listaDeModelosContato é List<com.example.unihub.data.model.Contato>
                    listaDeModelosContato
                        .filter { !it.pendente }
                        .filter { modeloContato ->
                            modeloContato.ownerId == null || modeloContato.ownerId == usuarioLogadoId
                        }
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
                    val idsValidos = contatosParaUi.map { it.id }.toSet()
                    val idsOriginais = _uiState.value.membrosOriginaisDoGrupo.map { it.id }.toSet()
                    _idMembrosSelecionados.update { idsSelecionados ->
                        idsSelecionados.filter { idsValidos.contains(it) || idsOriginais.contains(it) }.toSet()
                    }
                    _uiState.update {
                        it.copy(
                            todosOsContatosDisponiveis = contatosParaUi,
                            isLoadingAllContatos = false
                        )
                    }
                }
        }
    }

    private fun calcularPodeExcluirGrupo(grupo: Grupo, usuarioLogadoId: Long?): Boolean {
        val ownerId = grupo.ownerId
        if (usuarioLogadoId == null || ownerId == null || usuarioLogadoId != ownerId) {
            return false
        }

        val identificadores = mutableSetOf<String>()
        grupo.membros.forEach { membro ->
            val idContato = membro.idContato
            val emailNormalizado = membro.email
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.lowercase()
            val id = membro.id

            when {
                idContato != null -> identificadores.add("usuario:$idContato")
                emailNormalizado != null -> identificadores.add("email:$emailNormalizado")
                id != null -> identificadores.add("contato:$id")
            }
        }

        identificadores.add("usuario:$ownerId")
        return identificadores.size <= 1
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
