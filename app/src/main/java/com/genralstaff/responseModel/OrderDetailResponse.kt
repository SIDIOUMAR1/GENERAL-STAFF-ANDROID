package com.genralstaff.responseModel

data class OrderDetailResponse(
    val body: Body,
    val code: Int,
    val message: String,
    val success: Boolean
) {
    data class Body(
        val created_at: String,
        val delivery_charge: String,
        val driver_detail: DriverDetail,
        val driver_id: Int,
        val driver_status: Int,
        val id: Int,
        val latitude: String,
        val location: String,
        val longitude: String,
        val product: Product,
        val product_id: Int,
        val shop: Shop,
        val shop_id: Int,
        val status: Int,
        val sub_admin_detail: SubAdminDetail,
        val sub_admin_id: Int,
        val updated_at: String,
        val user_detail: UserDetail,
        val user_id: Int
    ) {
        data class DriverDetail(
            val address: String,
            val country_code: String,
            val latitude: String,
            val longitude: String,
            val name: String,
            val phone_no: String,
            val profile_pic: String=""
        )

        data class Product(
            val image: String,
            val name: String,
            val price: String
        )

        data class Shop(
            val category: Category,
            val image: String,
            val latitude: String,
            val location: String,
            val longitude: String,
            val name: String
        ) {
            data class Category(
                val name: String
            )
        }

        data class SubAdminDetail(
            val country_code: Any,
            val name: String,
            val phone_no: String,
            val profile_pic: String=""
        )

        data class UserDetail(
            val country_code: String,
            val name: String,
            val phone_no: String,
            val profile_pic: String=""
        )
    }
}