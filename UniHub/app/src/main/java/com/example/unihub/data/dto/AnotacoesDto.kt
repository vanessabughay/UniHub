package com.example.unihub.data.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO de resposta da API para Anotação.
 * Campos alinhados ao AnotacaoResponse do backend:
 * - id, titulo, conteudo, disciplinaId, createdAt, updatedAt
 */
data class AnotacoesDTO(
    @SerializedName("id") val id: Long,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("conteudo") val conteudo: String,
    @SerializedName("disciplinaId") val disciplinaId: Long,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

/**
 * DTO de requisição para criar/atualizar uma anotação.
 */
data class AnotacoesRequest(
    @SerializedName("titulo") val titulo: String,
    @SerializedName("conteudo") val conteudo: String? = ""
)

/**
 * Resposta paginada padrão do Spring (Page<T>).
 */
data class PageResponse<T>(
    @SerializedName("content") val content: List<T>,
    @SerializedName("totalElements") val totalElements: Long,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("number") val number: Int,
    @SerializedName("size") val size: Int
)