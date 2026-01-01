package com.genralstaff.responseModel

data class OrderDetailNewResponse(
    val body: Body,
    val code: Int,
    val message: String,
    val success: Boolean
) {
    data class Body(
        val audio: String,
        val created_at: String,
        val delivery_charge: String,
        val description: String,
        val driver_detail: Any,
        val driver_id: Any,
        val driver_status: Int,
        val driver_type: String,
        val id: Int,
        val latitude: String,
        val location: String,
        val longitude: String,
        val product: Product,
        val product_id: Int,
        val shop: Shop,
        val shop_address: String,
        val shop_id: Int,
        val shop_latitude: String,
        val shop_longitude: String,
        val status: Int,
        val sub_admin_detail: SubAdminDetail,
        val sub_admin_id: Int,
        val updated_at: String,
        val user_detail: UserDetail,
        val user_id: Int
    ) {
        data class Product(
            val image: Any,
            val name: String,
            val price: String,
            val product_medias: ArrayList<ProductMedia>,
            val type: Type
        ) {
            data class ProductMedia(
                val createdAt: String,
                val id: Int,
                val media: String,
                val media_type: Any,
                val product_id: Int,
                val thumbnail: Any,
                val updatedAt: String,
                val user_id: Int
            )

            data class Type(
                val createdAt: String,
                val deleted_at: Any,
                val id: Int,
                val name: String,
                val name_ar: Any,
                val name_fr: Any,
                val shop_id: Int,
                val sort_order: Int,
                val status: Int,
                val updatedAt: String
            )
        }

        data class Shop(
            val category: Category,
            val country_code: String,
            val image: String,
            val latitude: String,
            val location: String,
            val longitude: String,
            val name: String,
            val name_ar: String,
            val name_fr: String,
            val phone: String
        ) {
            data class Category(
                val name: String
            )
        }

        data class SubAdminDetail(
            val country_code: String,
            val name: String,
            val phone_no: String,
            val profile_pic: String
        )

        data class UserDetail(
            val country_code: String,
            val name: String,
            val phone_no: String,
            val profile_pic: String
        )
    }
}