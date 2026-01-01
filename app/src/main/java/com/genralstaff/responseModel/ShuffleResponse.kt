package com.genralstaff.responseModel

data class ShuffleResponse(
    val body: Body,
    val code: Int,
    val message: String,
    val success: Boolean
) {
    class Body
}