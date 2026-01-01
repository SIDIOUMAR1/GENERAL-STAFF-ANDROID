package com.genralstaff.responseModel

data class UploadProfileResponse(
    val body: Body,
    val code: Int,
    val message: String,
    val success: Boolean
) {
    data class Body(
        val media: String
    )
}