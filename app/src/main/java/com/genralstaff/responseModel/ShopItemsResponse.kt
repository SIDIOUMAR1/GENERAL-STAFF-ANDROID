package com.genralstaff.responseModel

import java.io.Serializable

data class ShopItemsResponse(
    val body: Body,
    val code: Int,
    val message: String,
    val success: Boolean
):Serializable {
    data class Body(
        val `data`: ArrayList<Data>,
        val pagination: Pagination
    ):Serializable {
        data class Data(
            val category_id: Int,
            val description: String,
            val id: Int,
            val image: String,
            val name: String,
            val type_id: String,
            val price: String,
            val product_medias: ArrayList<ProductMedia>,
            val shop_id: Int,
            val user_id: Int
        ):Serializable {
            data class ProductMedia(
                val createdAt: String,
                val id: Int,
                val media: String,
                val media_type: String,
                val product_id: Int,
                val thumbnail: String,
                val updatedAt: String,
                val user_id: Int
            ):Serializable
        }

        data class Pagination(
            val limit: Int,
            val offset: Int,
            val page: Int,
            val totalPages: Int
        ):Serializable
    }
}