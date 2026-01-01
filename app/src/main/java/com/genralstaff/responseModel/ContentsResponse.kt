package com.genraluser.responseModel

data class ContentsResponse(
    val body: Body,
    val code: Int,
    val message: String,
    val success: Boolean
) {
    data class Body(
        val createdAt: String,
        val description: String,
        val id: Int,
        val status: Int,
        val title: String,
        val type: Int,
        val updatedAt: String
    )
}