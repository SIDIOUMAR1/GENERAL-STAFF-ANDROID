package com.genralstaff.responseModel

data class GetChatMessagesResponse(
    val list: ArrayList<Message>,
    val room: Room
) {
    data class Message(
        val id: Int,
        val sender_id: Int,
        val receiver_id: Int,
        val room_id: String,
        val message: String,
        val message_type: Int,
        var is_read: Int,
        val createdAt: String,
        val updatedAt: String
    )

    data class Room(
        val id: Int,
        val sender_id: Int,
        val receiver_id: Int,
        val shop_id: Int,
        val last_message_id: Int,
        val completed_time: String?,
        val shop_latitude: String?,
        val shop_longitude: String?,
        val user_latitude: String?,
        val user_longitude: String?,
        val createdAt: String,
        val updatedAt: String,
        val distance: String?,
        val shop_detail: ShopDetail
    )

    data class ShopDetail(
        val id: Int,
        val name: String,
        val name_ar: String,
        val name_fr: String,
        val image: String,
        val country_code: String,
        val phone: String,
        val location: String,
        val latitude: String,
        val longitude: String
    )
}