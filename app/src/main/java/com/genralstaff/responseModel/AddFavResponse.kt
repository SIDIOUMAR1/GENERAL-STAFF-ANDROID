package com.genraluser.responseModel

data class AddFavResponse(
    val body: Body,
    val code: Int,
    val message: String,
    val success: Boolean
) {
    class Body
}