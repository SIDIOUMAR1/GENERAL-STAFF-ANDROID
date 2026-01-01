package com.genralstaff.responseModel

data class LogOutResponse(
    val body: Body,
    val code: Int,
    val message: String,
    val success: Boolean
) {
    data class Body(
        val address: Any,
        val bio: Any,
        val country_code: String,
        val createdAt: String,
        val deleted_at: Any,
        val device_id: String,
        val device_token: String,
        val device_type: Int,
        val document: Any,
        val email: Any,
        val id: Int,
        val is_approve: Int,
        val last_name: Any,
        val latitude: Any,
        val longitude: Any,
        val name: String,
        val notification_status: Int,
        val otp: Any,
        val password: Any,
        val phone_no: String,
        val plate_number: Any,
        val profile_pic: String="",
        val refer_by: Any,
        val referral_code: String,
        val social_id: String,
        val social_type: Int,
        val status: Int,
        val type: Int,
        val updatedAt: String,
        val user_name: Any,
        val vehicle_type: Any
    )
}