package com.genralstaff.responseModel

data class ShopAddResponse(
    val body: Body,
    val code: Int,
    val message: String,
    val success: Boolean
) {
    class Body
}