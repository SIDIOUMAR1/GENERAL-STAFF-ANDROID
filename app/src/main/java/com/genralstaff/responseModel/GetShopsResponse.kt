package com.genralstaff.responseModel


import java.io.Serializable



data class GetShopsResponse(
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
            val distance: String,
            val id: Int,
            val image: String,
            val name_ar: String?,
            val name_fr: String?,
            val country_code: String?,
            val phone: String?,
            val open_time: String? = "",
            val close_time: String? = "",
            var status: String,
            var is_favourite: Int,
            val latitude: String,
            val location: String,
            val description: String? = null,
            val longitude: String,
            val name: String,
            val user_id: Int,

            // ✅ Add Serializable list
            val shop_timings: List<ShopTiming> = listOf()
        ) : Serializable {  // ✅ Also make Data serializable
            data class Category(
                val name: String
            ) : Serializable

            data class ShopTiming(
                val id: Int,
                val shop_id: Int,
                val open_time: String,
                val close_time: String,
                val day: String,
                val created_at: String,
                val updated_at: String
            ) : Serializable  // ✅ Mark this class as Serializable
        }

        data class Pagination(
            val limit: Int,
            val offset: Int,
            val page: Int,
            val totalPages: Int
        ) : Serializable
    }
}
