package com.genraluser.responseModel

data class GetFavouritesResponse(
    val body: Body,
    val code: Int,
    val message: String,
    val success: Boolean
) {
    data class Body(
        val `data`: ArrayList<Data>,
        val pagination: Pagination
    ) {
        data class Data(
            val id: Int,
            val shop_detail: ShopDetail,
            val shop_id: Int,
            val user_id: Int
        ) {
            data class ShopDetail(
                val category: Category,
                val category_id: Int,
                val created_at: String,
                val id: Int,
                val image: String,
                val description: String,
                val latitude: Any,
                val location: String,
                val longitude: Any,
                val name: String,
                val status: Int,
                val updated_at: String,
                val user_id: Int
            ) {
                data class Category(
                    val name: String
                )
            }
        }

        data class Pagination(
            val limit: Int,
            val offset: Int,
            val page: Int,
            val totalPages: Int
        )
    }
}