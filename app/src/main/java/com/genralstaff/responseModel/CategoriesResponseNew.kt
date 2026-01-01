package com.genralstaff.responseModel

data class CategoriesResponseNew(
    val body: ArrayList<Body>,
    val code: Int,
    val message: String,
    val success: Boolean
) {
    data class Body(
        val createdAt: String,
        val id: Int,
        val name: String?,
        val status: Int,
        val name_ar: String?,
        val name_fr: String?,
        var isSelected: Boolean = false,
        val updatedAt: String
    )
}
