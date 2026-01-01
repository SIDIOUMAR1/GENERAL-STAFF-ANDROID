package com.genralstaff.responseModel

import java.io.Serializable

data class CategoriesListResponse(
    val body: ArrayList<Body>,
    val code: Int,
    val message: String,
    val success: Boolean
):Serializable {
    data class Body(
        val createdAt: String,
        val deleted_at: Any,
        val id: Int,
        val name: String,
        val name_ar: String,
        var isselect: Boolean,
        val name_fr: String,
        val shop_id: Int,
        val status: Int,
        val updatedAt: String
    ):Serializable
}