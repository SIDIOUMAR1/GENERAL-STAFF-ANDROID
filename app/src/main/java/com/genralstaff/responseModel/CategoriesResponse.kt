package com.genralstaff.responseModel

data class CategoriesResponse(
    val body: ArrayList<Body>,
    val code: Int,
    val message: String,
    val success: Boolean
) {
    data class Body(
        val id: Int,
        val name: String,
    )
}