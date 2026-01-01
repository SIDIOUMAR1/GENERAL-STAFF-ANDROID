package com.genralstaff.responseModel

data class NotificationListResponse(
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
            val body: String,
            val createdAt: String,
            val `data`: String,
            val id: Int,
            val notification_type: String,
            val receiver_id: Int,
            val sender_detail: SenderDetail,
            val sender_id: Int,
            val title: String,
            val updatedAt: String
        ) {
            data class SenderDetail(
                val email: Any,
                val id: Int,
                val last_name: Any,
                val name: String,
                val profile_pic: String="",
                val user_name: Any
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