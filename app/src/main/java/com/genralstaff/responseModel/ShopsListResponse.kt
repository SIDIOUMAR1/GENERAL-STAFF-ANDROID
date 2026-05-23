package com.genralstaff.responseModel

data class ShopsListResponse(
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
            val category: Category,
            val category_id: Int,
            val description: String?,
            val id: Int,
            val image: String?,
            val latitude: String?,
            val location: String?,
            val longitude: String?,
            val name: String,
            val user_id: Int,
            val name_ar: String? = null,
            val name_fr: String? = null,
            val shop_location_audio: String? = null,  // ← IMPORTANT pour l'audio auto
            val phone: String? = null,
            val country_code: String? = null,
            val open_time: String? = null,
            val close_time: String? = null,
            val status: Int = 1
        ) {
            data class Category(
                val name: String
            )
        }

        data class Pagination(
            val limit: Int,
            val offset: Int,
            val page: Int,
            val totalPages: Int
        )
    }
}