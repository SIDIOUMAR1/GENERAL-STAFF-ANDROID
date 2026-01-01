package com.genralstaff.responseModel

data class DashboardResponse(
    val body: Body,
    val code: Int,
    val message: String,
    val success: Boolean
) {
    data class Body(
        val total_orders: String,
        val current_orders: String,
        val total_shops: String
    )
}