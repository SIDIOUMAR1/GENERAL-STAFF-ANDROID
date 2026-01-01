package com.genraluser.responseModel

data class GetShopItemsResponse(
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
            val category_id: Int,
            val description: String,
            val id: Int,
            val image: String,
            val name: String,
            val price: String,
            val shop_id: Int,
            val user_id: Int
        )

        data class Pagination(
            val limit: Int,
            val offset: Int,
            val page: Int,
            val totalPages: Int
        )
    }
}