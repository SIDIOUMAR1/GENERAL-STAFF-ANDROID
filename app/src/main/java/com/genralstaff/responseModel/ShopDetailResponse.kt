package com.genralstaff.responseModel

import java.io.Serializable

data class ShopDetailResponse(
    val body: Body?,
    val code: Int?,
    val message: String?,
    val success: Boolean?
) {
    data class Body(
        val category: Category?,
        val category_id: Int?,
        val close_time: String?,
        val country_code: String?,
        val description: String?,
        val distance: Double?,
        val id: Int?,
        val image: String?,
        val is_favourite: Int?,
        val latitude: Double?,
        val location: String?,
        val shop_timings: List<ShopTiming> = listOf(),

        val longitude: Double?,
        val name: String?,
        val name_ar: String?,
        val name_fr: String?,
        val open_time: String?,
        val phone: String?,
        val status: Int?,
        val user: User?,
        val user_id: Int?
    ) {
        data class Category(
            val name: String?
        )
        data class ShopTiming(
            val id: Int,
            val shop_id: Int,
            val open_time: String,
            val close_time: String,
            val day: String,
            val created_at: String,
            val updated_at: String
        ) : Serializable  // ✅ Mark this class as Serializable
        data class User(
            val country_code: String?,
            val name: String?,
            val phone_no: String?,
            val profile_pic: String?
        )
    }
}
