package com.genralstaff.responseModel

data class LoginResponse(
    val body: Body,
    val code: Int,
    val message: String,
    val success: Boolean
) {
    data class Body(
        val user: User
    ) {
        data class User(
            val country_code: String?,
            val id: Int,
            val is_approve: Int,
            val last_name: String?,
            val name: String,
            val notification_status: Int,
            val password: String,
            val phone_no: String,
            val profile_pic: String,
            val token: String,
            val type: Int,
            val updatedAt: String,
            val user_name: String?,
        )
    }
}
