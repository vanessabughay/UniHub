package com.example.unihub.ui.ListarGrupo

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.model.Grupo
import com.example.unihub.data.repository.ContatoRepository
import com.example.unihub.data.repository.ContatoResumo
import com.example.unihub.data.repository.GrupoRepository

import java.io.IOException
import retrofit2.HttpException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
class ListarGrupoViewModel(
    private val repository: GrupoRepository,
    private val contatoRepository: ContatoRepository
) : ViewModel() {

    private val _grupos = MutableStateFlow<List<Grupo>>(emptyList())
    val grupos: StateFlow<List<Grupo>> = _grupos.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _contatosDoUsuario = MutableStateFlow<List<ContatoResumo>>(emptyList())
    val contatosDoUsuario: StateFlow<List<ContatoResumo>> = _contatosDoUsuario.asStateFlow()

    init {
        loadGrupo()
        loadContatosDoUsuario()
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun loadGrupo() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.getGrupo()
                .map { gruposDoRepositorio ->
                    gruposDoRepositorio.map { grupo ->
                        Grupo(
                            id = grupo.id,
                            nome = grupo.nome,
                            membros = grupo.membros,
                            adminContatoId = grupo.adminContatoId,
                            ownerId = grupo.ownerId

                        )
                    }.sortedBy { it.nome.lowercase() }
                }
                .catch { exception ->
                    val detalheErro = if (exception.message.isNullOrBlank()) {
                        "Causa desconhecida. Verifique os logs." // Ou uma mensagem mais genérica
                    } else {
                        exception.message
                    }
                    _errorMessage.value = "Falha ao carregar Grupos: $detalheErro"
                    // Adicionar log para depuração
                    Log.e("ListarGrupoViewModel", "Erro em loadGrupo: ", exception)
                    _grupos.value = emptyList()
                    _isLoading.value = false
                }
                .collect { gruposOrdenadosParaUi ->
                    _grupos.value = gruposOrdenadosParaUi
                    _isLoading.value = false
                }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun loadContatosDoUsuario() {
        viewModelScope.launch {
            contatoRepository.getContatoResumo()
                .map { contatosDoRepositorio ->
                    contatosDoRepositorio.filterNot { it.pendente }
                }
                .catch {
                    _contatosDoUsuario.value = emptyList()
                }
                .collect { contatosFiltrados ->
                    _contatosDoUsuario.value = contatosFiltrados
                }
        }
    }

    /**
     * Exclui um Grupo pelo ID.
     * @param GrupoId O ID do Grupo a ser excluído.
     * @param onResult Callback que informa o resultado da operação (true para sucesso, false para falha).
     */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun deleteGrupo(grupoId: String, onResult: (sucesso: Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true // Mostrar indicador de carregamento durante a exclusão
            _errorMessage.value = null // Limpar mensagens de erro antigas

            try {
                val id = grupoId.toLongOrNull()
                if (id == null) {
                    _errorMessage.value = "ID de Grupo inválido."
                    _isLoading.value = false
                    onResult(false)
                    return@launch
                }

                var delegouCarregamento = false
                val deleteSuccess = repository.deleteGrupo(id.toString())

                if (deleteSuccess) {
                    // Após a exclusão bem-sucedida, recarregue a lista de Grupos.
                    // loadGrupo() já define _isLoading.value = false no seu final.
                    delegouCarregamento = true
                    loadGrupo()
                    onResult(true)
                } else {
                    _errorMessage.value = "Falha ao excluir o Grupo no servidor/banco de dados."
                    onResult(false)
                }
                if (!delegouCarregamento) {
                    _isLoading.value = false
                }
            } catch (e: IllegalStateException) {
                _errorMessage.value = e.message ?: "Não foi possível sair do grupo. Tente novamente mais tarde."
                _isLoading.value = false
                onResult(false)
            } catch (e: IllegalArgumentException) {
                _errorMessage.value = e.message ?: "ID de Grupo inválido."
                _isLoading.value = false
                onResult(false)
            } catch (e: IOException) {
                _errorMessage.value = "Erro de rede ao excluir Grupo: ${e.message}"
                _isLoading.value = false
                onResult(false)
            } catch (e: HttpException) {
                _errorMessage.value = "Erro do servidor (${e.code()}) ao excluir Grupo."
                _isLoading.value = false
                onResult(false)
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao excluir Grupo: ${e.message}"
                _isLoading.value = false
                onResult(false)
            }

        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun leaveGrupo(grupoId: String, onResult: (sucesso: Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val id = grupoId.toLongOrNull()
                if (id == null) {
                    _errorMessage.value = "ID de Grupo inválido."
                    _isLoading.value = false
                    onResult(false)
                    return@launch
                }

                var delegouCarregamento = false
                val leaveSuccess = repository.leaveGrupo(id.toString())

                if (leaveSuccess) {
                    delegouCarregamento = true
                    loadGrupo()
                    onResult(true)
                } else {
                    _errorMessage.value = "Falha ao sair do Grupo no servidor/banco de dados."
                    onResult(false)
                }

                if (!delegouCarregamento) {
                    _isLoading.value = false
                }
            } catch (e: IllegalArgumentException) {
                _errorMessage.value = e.message ?: "ID de Grupo inválido."
                _isLoading.value = false
                onResult(false)
            } catch (e: IOException) {
                _errorMessage.value = "Erro de rede ao sair do Grupo: ${e.message}"
                _isLoading.value = false
                onResult(false)
            } catch (e: HttpException) {
                _errorMessage.value = "Erro do servidor (${e.code()}) ao sair do Grupo."
                _isLoading.value = false
                onResult(false)
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao sair do Grupo: ${e.message}"
                _isLoading.value = false
                onResult(false)
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}