package com.genraluser.responseModel

data class FavouriteUnFavResponse(
    val body: Body,
    val code: Int,
    val message: String,
    val success: Boolean
) {
    class Body
}