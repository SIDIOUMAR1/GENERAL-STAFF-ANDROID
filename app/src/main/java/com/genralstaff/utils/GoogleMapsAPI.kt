package com.genralstaff.utils

import android.content.Context
import com.genralstaff.R
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

object GoogleMapsAPI {
    private var apiKey: String? = null

    fun initialize(context: Context) {
        apiKey = context.getString(R.string.googlePlaceKey_live)
    }

    fun getTravelDistance(
        pickupCoordinate: Pair<Double, Double>,
        dropCoordinate: Pair<Double, Double>,
        context: Context, // Ensure context is available if needed
        completion: (Result<String>) -> Unit
    ) {
        if (apiKey == null) {
            apiKey = context.getString(R.string.googlePlaceKey_live)
        }

        val pickupString = "${pickupCoordinate.first},${pickupCoordinate.second}"
        val dropString = "${dropCoordinate.first},${dropCoordinate.second}"
        val urlString = "https://maps.googleapis.com/maps/api/directions/json?origin=$pickupString&destination=$dropString&mode=transit&key=$apiKey"

        Executors.newSingleThreadExecutor().execute {
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)

                if (jsonResponse.getString("status") == "ZERO_RESULTS") {
                    val distanceKm = calculateHaversineDistance(pickupCoordinate, dropCoordinate)
                    completion(Result.success(formatDistance(distanceKm)))
                    return@execute
                }

                val routes = jsonResponse.optJSONArray("routes")
                val legs = routes?.optJSONObject(0)?.optJSONArray("legs")
                val distance = legs?.optJSONObject(0)?.optJSONObject("distance")?.optString("text")

                if (distance != null) {
                    completion(Result.success(parseAndFormatDistance(distance)))
                } else {
                    completion(Result.failure(Exception("Failed to parse response")))
                }
            } catch (e: Exception) {
                completion(Result.failure(e))
            }
        }
    }

    /** Haversine Formula to calculate the great-circle distance **/
    private fun calculateHaversineDistance(coord1: Pair<Double, Double>, coord2: Pair<Double, Double>): Double {
        val R = 6371.0 // Earth radius in km
        val lat1 = Math.toRadians(coord1.first)
        val lon1 = Math.toRadians(coord1.second)
        val lat2 = Math.toRadians(coord2.first)
        val lon2 = Math.toRadians(coord2.second)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return R * c
    }

    /** Formats distance according to your old logic **/
    private fun formatDistance(distanceKm: Double): String {
        return if (distanceKm < 1.0) {
            val distanceInMeters = (distanceKm * 1000).toInt()
            val roundedMeters = ((distanceInMeters + 5) / 10) * 10 // Round to the nearest 10 meters
            "$roundedMeters m"
        } else {
            String.format("%.1f km", distanceKm)
        }
    }

    /** Parses distance text (e.g., "8738.3 km") and formats it properly **/
    private fun parseAndFormatDistance(distanceText: String): String {
        val components = distanceText.split(" ")
        val distanceValue = components.getOrNull(0)?.toDoubleOrNull() ?: return "Unknown Distance"
        return formatDistance(distanceValue)
    }
}
