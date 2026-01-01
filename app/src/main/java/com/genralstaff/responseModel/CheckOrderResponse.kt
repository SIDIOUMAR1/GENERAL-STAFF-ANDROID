package com.genralstaff.responseModel

data class CheckOrderResponse(
    val body: Body,
    val code: Int,
    val message: String,
    val success: Boolean
) {
    data class Body(
        val status: Int
    )
}