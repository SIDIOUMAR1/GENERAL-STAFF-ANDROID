package com.genralstaff.responseModel
data class AvailableDriversResponse(
    val code: Int,
    val message: String,
    val body: Body
) {
    data class Body(
        val drivers: List<Driver>,
        val count: Int
    )

    data class Driver(
        val id: Int,
        val name: String,
        val phone: String,
        val distance: String, // "1.25" km
        val vehicle_type: String,
        val profile_pic: String?
    )
}