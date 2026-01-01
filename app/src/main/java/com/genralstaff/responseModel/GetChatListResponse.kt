package com.genralstaff.responseModel

data class GetChatListResponse(
    val data: List<ChatItem> = emptyList() // Ensure it's never null
)

data class ChatItem(
    val id: Int = 0,
    val sender_id: Int = 0,
    val receiver_id: Int = 0,
    val shop_id: Int = 0,
    val last_message_id: Int = 0,
    val createdAt: String = "",
    val updatedAt: String = "",
    val distance: String ,
    val sender_detail: SenderDetail? = null, // Mark nullable
    val receiver_detail: ReceiverDetail? = null, // Mark nullable
    val shop_detail: ShopDetail? = null, // Mark nullable
    val last_message_detail: LastMessageDetail? = null, // Mark nullable
    val receiver_name: String = ""
)

data class SenderDetail(
    val id: Int = 0,
    val name: String = "",
    val type: String = "",
    val profile_pic: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val phone_no: String = "",
    val country_code: String = ""
)

data class ReceiverDetail(
    val id: Int = 0,
    val name: String = "",
    val type: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val profile_pic: String = "",
    val phone_no: String = "",
    val country_code: String = ""
)

data class ShopDetail(
    val id: Int = 0,
    val name: String = "",
    val phone: String = "",
    val country_code: String = "",
    val user: User? = null, // Mark nullable
    val latitude: String = "",
    val longitude: String = "",
    val image: String = ""
) {
    data class User(
        val country_code: String = "",
        val phone_no: String = ""
    )
}

data class LastMessageDetail(
    val id: Int = 0,
    val sender_id: Int = 0,
    val receiver_id: Int = 0,
    val is_read: Int = 0,
    val room_id: String = "",
    val message: String = "",
    val message_type: Int = 0,
    val createdAt: String = "",
    val updatedAt: String = ""
)
