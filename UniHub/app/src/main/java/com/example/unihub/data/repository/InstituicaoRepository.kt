package com.example.unihub.data.repository

import com.example.unihub.data.model.Instituicao

/**
 * Repositório simples em memória para buscar e salvar instituições.
 */
class InstituicaoRepository {

    private val instituicoes = mutableListOf(
        Instituicao(
            id = 1,
            nome = "UNIVERSIDADE FEDERAL DO PARANÁ",
            mediaAprovacao = 7.0,
            frequenciaMinima = 75
        ),
        Instituicao(
            id = 2,
            nome = "UNIVERSIDADE DE SÃO PAULO",
            mediaAprovacao = 5.0,
            frequenciaMinima = 70
        )
    )

    private var instituicaoSelecionada: Instituicao? = null

    fun buscarInstituicoes(query: String): List<Instituicao> {
        return instituicoes.filter { it.nome.contains(query, ignoreCase = true) }
    }

    fun getInstituicaoPorNome(nome: String): Instituicao? {
        return instituicoes.find { it.nome.equals(nome, ignoreCase = true) }
    }

    fun salvarInstituicao(instituicao: Instituicao) {
        instituicaoSelecionada = instituicao
        if (instituicoes.none { it.nome.equals(instituicao.nome, ignoreCase = true) }) {
            instituicoes.add(instituicao.copy(id = instituicoes.size + 1))
        }
    }

    fun instituicaoUsuario(): Instituicao? = instituicaoSelecionada
}