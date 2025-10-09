package com.example.unihub.ui.PesoNotas

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihub.data.dto.AvaliacaoRequestDto
import com.example.unihub.data.dto.ContatoIdDto
import com.example.unihub.data.dto.DisciplinaIdDto
import com.example.unihub.data.model.Avaliacao
import com.example.unihub.data.model.EstadoAvaliacao
import com.example.unihub.data.model.Modalidade
import com.example.unihub.data.model.Prioridade
import com.example.unihub.data.repository.AvaliacaoRepository
import com.example.unihub.data.repository.InstituicaoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

data class PesoNotasUiState(
    val disciplinaId: Long? = null,
    val itens: List<Avaliacao> = emptyList(),
    val isLoading: Boolean = false,
    val erro: String? = null,

    val somaPesosTotal: Double = 0.0,
    val somaPesosComNota: Double = 0.0,
    val notaGeral: Double = 0.0,
    val faltandoParaAprovacao: Double = 0.0,
    val mediaAprovacao: Double = 0.0
)

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
class ManterPesoNotasViewModel(
    private val repository: AvaliacaoRepository,
    private val instituicaoRepository: InstituicaoRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(PesoNotasUiState())
    val ui: StateFlow<PesoNotasUiState> = _ui.asStateFlow()
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            try {
                instituicaoRepository.instituicaoUsuario()
                    ?.mediaAprovacao
                    ?.let { setMediaAprovacao(it) }
            } catch (_: Exception) {
                // Mantém valor padrão caso falhe buscar instituição
            }
        }
    }

    fun setDisciplinaId(id: Long) {
        if (_ui.value.disciplinaId == id) return
        _ui.value = _ui.value.copy(disciplinaId = id)
        load()
    }

    fun setMediaAprovacao(media: Double) {
        _ui.value = _ui.value.copy(mediaAprovacao = media)
        recalc(_ui.value.itens) // atualiza “faltando X”
    }

    fun load() {
        val discId = _ui.value.disciplinaId ?: return
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true, erro = null)
            repository.getAvaliacaoPorDisciplina(discId)
                .map { it.sortedBy { a -> a.descricao.orEmpty().lowercase() } }
                .catch { e -> _ui.value = _ui.value.copy(isLoading = false, erro = e.message ?: "Erro ao carregar.") }
                .collect { lista ->
                    _ui.value = _ui.value.copy(isLoading = false, erro = null)
                    recalc(lista)
                }
        }
    }

    private fun recalc(lista: List<Avaliacao>) {
        val somaTotal = lista.sumOf { it.peso ?: 0.0 }

        //val somaComNota = lista.filter { it.nota != null }.sumOf { it.peso ?: 0.0 }
        val notaGeral = lista.sumOf { (it.nota ?: 0.0) * ((it.peso ?: 0.0) / 100.0) }
        val falta = max(0.0, _ui.value.mediaAprovacao - notaGeral)

        _ui.value = _ui.value.copy(
            itens = lista,
            somaPesosTotal = somaTotal,
            //somaPesosComNota = somaComNota,
            notaGeral = notaGeral,
            faltandoParaAprovacao = falta
        )
    }

    fun salvarNota(av: Avaliacao, novaNota: Double?, onDone: (Boolean, String?) -> Unit) {
        val id = av.id ?: return onDone(false, "ID inválido")
        val req = av.toRequest(overrideNota = novaNota)
        viewModelScope.launch {
            try {
                val ok = repository.updateAvaliacao(id, req)
                if (ok) load()
                onDone(ok, if (ok) null else "Falha ao salvar nota.")
            } catch (e: Exception) {
                onDone(false, e.message)
            }
        }
    }

    fun salvarPeso(av: Avaliacao, novoPeso: Double?, onDone: (Boolean, String?) -> Unit) {
        val id = av.id ?: return onDone(false, "ID inválido")
        val req = av.toRequest(overridePeso = novoPeso)
        viewModelScope.launch {
            try {
                val ok = repository.updateAvaliacao(id, req)
                if (ok) load()
                onDone(ok, if (ok) null else "Falha ao salvar peso.")
            } catch (e: Exception) {
                onDone(false, e.message)
            }
        }
    }

    /**
     * Monta o mesmo DTO usado nos updates de Avaliação, preservando campos.
     */
    private fun Avaliacao.toRequest(
        overrideNota: Double? = this.nota,
        overridePeso: Double? = this.peso
    ): AvaliacaoRequestDto {
        return AvaliacaoRequestDto(
            id = this.id,
            descricao = this.descricao,
            disciplina = this.disciplina?.id?.let { DisciplinaIdDto(it) },
            tipoAvaliacao = this.tipoAvaliacao,
            modalidade = this.modalidade ?: Modalidade.INDIVIDUAL,
            dataEntrega = this.dataEntrega,
            nota = overrideNota,
            peso = overridePeso,
            integrantes = this.integrantes.orEmpty().mapNotNull { it.id }.map { ContatoIdDto(it) },
            prioridade = this.prioridade ?: Prioridade.MEDIA,
            estado = this.estado ?: EstadoAvaliacao.A_REALIZAR,
            dificuldade = this.dificuldade,
            receberNotificacoes = this.receberNotificacoes ?: false
        )
    }

    fun pesosFecham100(): Boolean = abs(_ui.value.somaPesosTotal - 100.0) < 0.01
}