package com.genralstaff.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.genralstaff.auth.LoginActivity
import com.genralstaff.utils.MyApplication.Companion.prefs

import com.google.gson.Gson


import com.genralstaff.utils.Utils.getTimeAgo
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.lang.Math.abs
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.suspendCancellableCoroutine
import com.google.android.gms.maps.model.LatLng
import com.akexorcist.googledirection.GoogleDirection
import com.akexorcist.googledirection.model.Direction
import com.akexorcist.googledirection.constant.TransportMode
import com.akexorcist.googledirection.constant.AvoidType
import com.akexorcist.googledirection.util.execute
import com.genralstaff.base.LANGUAGE
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


fun getCity(lat: String, lng: String, context: Context): String {


    val geocoder = Geocoder(context, Locale.getDefault())
    val addresses: List<Address>?
    var city = ""
    try {
        addresses = geocoder.getFromLocation(
            lat.toDouble(),
            lng.toDouble(),
            1
        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        val address1 =
            addresses!![0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        Log.e("Address------", address1)


        try {
            city = address1
//            if (city == null|| city.isEmpty()) city = addresses[0].subLocality
//            if (city == null) city = addresses[0].subAdminArea
//            if (city == null) city = addresses[0].adminArea

        } catch (e: Exception) {
            city = address1
        }

    } catch (e: Exception) {
        e.printStackTrace()
        Log.d("TAG", "catch")
    }

    return city
}

fun hideKeyboard(activity: Activity) {
    val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    //Find the currently focused view, so we can grab the correct window token from it.
    var view = activity.currentFocus
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(activity)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}


var dfDate = SimpleDateFormat("dd/MM/yy");

fun checkDates(date: String, start: String, end: String): Boolean {

    return (dfDate.parse(date)!! == dfDate.parse(start) || dfDate.parse(date)!! == dfDate.parse(end)) || dfDate.parse(
        date
    )!!.after(dfDate.parse(start)) && dfDate.parse(date)!!.before(dfDate.parse(end));
}

@SuppressLint("SimpleDateFormat")
fun printDate(dateAndTime: String?): String {
    //2022-05-16T19:34:11.000Z
    val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val date: Date =
        dateFormat.parse(dateAndTime) //You will get date object relative to server/client timezone wherever it is parsed
    val formatter: DateFormat =
        SimpleDateFormat("MMM dd, YYYY | hh:mm a") //If you need time just put specific format for time like 'HH:mm:ss'
    val dateStr: String = formatter.format(date)
    return dateStr.replace("am", "AM").replace("pm", "PM")
}

fun getTimesAgo(created: String, format: String): String {
    val dateFormatter = SimpleDateFormat(format, Locale.getDefault())
    dateFormatter.timeZone = TimeZone.getTimeZone("UTC")
    val createdDate: Date = dateFormatter.parse(created) ?: Date()
    val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val date: Date =
        dateFormat.parse(created) //You will get date object relative to server/client timezone wherever it is parsed
    val formatter: DateFormat =
        SimpleDateFormat("dd-MM-YYYY") //If you need time just put specific format for time like 'HH:mm:ss'
    val dateStr: String = formatter.format(date)
    val objectTimestamp: Long = createdDate.time / 1000
    val currentTimeStamp: Long = System.currentTimeMillis() / 1000

    val timeDifference = abs(currentTimeStamp - objectTimestamp)
    val noMinutes: Long = timeDifference / 60
    var showString = ""

    if (noMinutes < 60) {
        showString = if (noMinutes == 1L) {
            "$noMinutes minute ago"
        } else if (noMinutes <= 0) {
            "Just now"
        } else {
            "$noMinutes minutes ago"
        }
    } else {
        val nohours: Long = noMinutes / 60
        if (nohours < 24) {
            showString = if (nohours == 1L) {
                "$nohours hour ago"
            } else {
                "$nohours hours ago"
            }
        } else {
            val noDays: Long = nohours / 24
            showString = if (noDays == 1L) {
                "$noDays day ago"
            } else {
                "$noDays days ago"
            }
        }
    }
    return dateStr + " " + showString
}

fun main(createds: String): String {
    val created = createds
    val format = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    val result = getTimesAgo(created, format)
//    println(result)
    return result
}

fun printDate2(dateAndTime: String?): String {
    //2022-05-16T19:34:11.000Z
    val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val date: Date =
        dateFormat.parse(dateAndTime) //You will get date object relative to server/client timezone wherever it is parsed
    val formatter: DateFormat =
        SimpleDateFormat("dd MMM yyyy") //If you need time just put specific format for time like 'HH:mm:ss'
    val dateStr: String = formatter.format(date)
    return dateStr
}

fun printDate3(dateAndTime: String?): String {
    //2022-05-16T19:34:11.000Z
    val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val date: Date =
        dateFormat.parse(dateAndTime) //You will get date object relative to server/client timezone wherever it is parsed
    val formatter: DateFormat =
        SimpleDateFormat("MMM dd,yyyy") //If you need time just put specific format for time like 'HH:mm:ss'
    val dateStr: String = formatter.format(date)
    return dateStr
}


inline fun <reified T> savePrefObject(key: String, obj: T) {
    savePrefrence(key, Gson().toJson(obj))
}

inline fun <reified T> getprefObject(key: String): T {
    return Gson().fromJson(getPrefrence(key, ""), T::class.java)
}

fun savePrefrence(key: String, value: Any) {

    val prefsName = "prefs.workforme"

    val prefs: SharedPreferences =
        MyApplication.getInstance()!!.applicationContext.getSharedPreferences(
            prefsName,
            Context.MODE_PRIVATE
        )
    val editor = prefs.edit()

    when (value) {
        is String -> editor.putString(key, value)
        is Boolean -> editor.putBoolean(key, value)
        is Int -> editor.putInt(key, value)
    }
    editor.apply()
}

inline fun <reified T> getPrefrence(key: String, deafultValue: T): T {

    val prefsName = "prefs.workforme"


    val prefs: SharedPreferences =
        MyApplication.getInstance()!!.applicationContext.getSharedPreferences(
            prefsName,
            Context.MODE_PRIVATE
        )
    return when (T::class) {
        String::class -> prefs.getString(key, deafultValue as String) as T
        Boolean::class -> prefs.getBoolean(key, deafultValue as Boolean) as T
        Int::class -> prefs.getInt(key, deafultValue as Int) as T
        else -> {
            " " as T
        }
    }

}

fun Fragment.sessionExpire() {
    MyApplication.mSocketManager = null
    prefs?.clearSharedPreference()
    this.goToSelectUserWithClearFlag()

}

fun Fragment.goToSelectUserWithClearFlag() {
    val intent = Intent(requireContext(), LoginActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    requireActivity().overridePendingTransition(
        android.R.anim.fade_in,
        android.R.anim.fade_out
    )
    return startActivity(intent)
}

fun Activity.sessionExpire() {
    MyApplication.mSocketManager = null
    prefs?.clearSharedPreference()
    this.goToSelectUserWithClearFlag()

}

fun Activity.goToSelectUserWithClearFlag() {
    val intent = Intent(this, LoginActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    return startActivity(intent)
}
fun changeLanguage(context: Context) {

    val lang = prefs!!.getPrefrenceLanguage(LANGUAGE, "ar").toString()

    Log.e("lang", "changeLanguage: $lang")

    val locale = Locale(lang)
    Locale.setDefault(locale)
    val config = Configuration()
    config.locale = locale
    context.resources.updateConfiguration(config, null)
    // If you are in an Activity, recreate it to apply changes immediately
    if (context is Activity) {
        context.recreate()
    }
}

fun changeLanguageSplash(context: Context) {
    val lang = prefs!!.getPrefrenceLanguage(LANGUAGE, "ar").toString()

    Log.e("langSplash", "changeLanguage: $lang")

    // Set the new locale
    val locale = Locale(lang)
    Locale.setDefault(locale)

    // Create a new Configuration object
    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)

    // Update the resources configuration
    context.resources.updateConfiguration(config, context.resources.displayMetrics)


}
@SuppressLint("SimpleDateFormat")
fun printTime(dateAndTime: String?): String {
    //2022-05-16T19:34:11.000Z
    val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val date: Date =
        dateFormat.parse(dateAndTime) //You will get date object relative to server/client timezone wherever it is parsed
    val formatter: DateFormat =
        SimpleDateFormat("MMM dd, yyyy | hh:mm a") //If you need time just put specific format for time like 'HH:mm:ss'
    val dateStr: String = formatter.format(date)
    return dateStr.replace("am", "AM").replace("pm", "PM")
}

fun makePhoneCall(phoneNumber: String, context: Context) {
    val callIntent = Intent(Intent.ACTION_DIAL)
    callIntent.data = Uri.parse("tel:$phoneNumber")
    context.startActivity(callIntent)
}

//fun openWhatsAppChat(phoneNumber: String,context: Context) {
//    val packageManager = context.packageManager
//    val numberExistsInWhatsApp: Boolean
//
//    // Check if WhatsApp is installed
//    val isWhatsAppInstalled: Boolean = try {
//        packageManager.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
//        true
//    } catch (e: PackageManager.NameNotFoundException) {
//        false
//    }
//
//    // Check if the provided number exists on WhatsApp
//    numberExistsInWhatsApp = if (isWhatsAppInstalled) {
//        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber")
//        val intent = Intent(Intent.ACTION_VIEW, uri)
//        intent.`package` = "com.whatsapp"
//        intent.resolveActivity(packageManager) != null
//    } else {
//        false
//    }
//
//    if (isWhatsAppInstalled) {
//        if (numberExistsInWhatsApp) {
//            // Open WhatsApp chat
//            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber")
//            val intent = Intent(Intent.ACTION_VIEW, uri)
//            intent.`package` = "com.whatsapp"
//            context.startActivity(intent)
//        } else {
//            // Number doesn't exist on WhatsApp
//            Toast.makeText(context, "The number is not registered on WhatsApp.", Toast.LENGTH_SHORT).show()
//        }
//    } else {
//        // WhatsApp is not installed
//        Toast.makeText(context, "WhatsApp is not installed on your device.", Toast.LENGTH_SHORT).show()
//        // Optionally, you can prompt the user to install WhatsApp from Google Play
//        // or another app store by redirecting them.
//    }
//}

fun saveDeviceTokenPrefrence(key: String, value: Any) {
    val preference = MyApplication.getInstance()!!.applicationContext.getSharedPreferences(
        "device_token",
        0
    )
    val editor = preference.edit()

    when (value) {
        is String -> editor.putString(key, value)
        is Boolean -> editor.putBoolean(key, value)
        is Int -> editor.putInt(key, value)
    }
    editor.apply()
}


inline fun <reified T> getDeviceTokenPrefrence(key: String, deafultValue: T): T {
    val preference = MyApplication.getInstance()!!.applicationContext.getSharedPreferences(
        "device_token",
        0
    )
    return when (T::class) {
        String::class -> preference.getString(key, deafultValue as String) as T
        Boolean::class -> preference.getBoolean(key, deafultValue as Boolean) as T
        Int::class -> preference.getInt(key, deafultValue as Int) as T
        else -> {
            " " as T
        }
    }

}

fun String.getTextRequestBody(): RequestBody {
    return RequestBody.create("text/plain".toMediaTypeOrNull(), this);
}

fun timeStampToTime(textView: TextView, inputTime: String) {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    val output = SimpleDateFormat("dd/MM/yyyy")

    var date: Date? = null
    try {
        date = formatter.parse(inputTime)
    } catch (e: ParseException) {
        e.printStackTrace()
    }

    textView.text = getTimeAgo(date!!)
}

fun timeStampToTime(inputTime: String): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    val output = SimpleDateFormat("dd/MM/yyyy")

    var date: Date? = null
    try {
        date = formatter.parse(inputTime)
    } catch (e: ParseException) {
        e.printStackTrace()
    }

    return getTimeAgo(date!!).toString()
}
// Extension function for getting distance in miles
suspend fun LatLng.getDistanceTo(dropOff: LatLng, googlePlaceKey: String): Result<Double> =
    suspendCancellableCoroutine { continuation ->

        // Start getting the directions
        GoogleDirection.withServerKey(googlePlaceKey)
            .from(this)
            .to(dropOff)
            .avoid(AvoidType.INDOOR)
            .transportMode(TransportMode.DRIVING)
            .execute(
                onDirectionSuccess = { direction: Direction? ->
                    if (direction != null && direction.isOK) {
                        try {
                            // Get the total distance in meters
                            val distanceInMeters = direction.routeList[0].totalDistance.toDouble()

                            // Convert to miles
                            val distanceInMiles = metersToMiles(distanceInMeters)

                            continuation.resume(Result.success(distanceInMiles))
                        } catch (e: Exception) {
                            continuation.resumeWithException(e)
                        }
                    } else {
                        continuation.resume(Result.failure(Exception("Failed to get direction.")))
                    }
                },
                onDirectionFailure = {
                    continuation.resume(Result.failure(Exception("Direction API failed.")))
                }
            )
    }

// Helper function to convert meters to miles
private fun metersToMiles(meters: Double): Double {
    val conversionFactor = 0.000621371
    val distanceInMiles = meters * conversionFactor

    // Format to 2 decimal places and convert it back to Double
    return String.format(Locale.US,"%.2f", distanceInMiles).toDouble()
}

fun prepareFilePart(partName: String?, file: File): MultipartBody.Part {

    var mediaType: MediaType? = null
    mediaType = if (file.endsWith("png")) {
        "image/png".toMediaTypeOrNull()
    } else if (file.endsWith("m4a")) {
        "audio/m4a".toMediaTypeOrNull()
    } else if (file.endsWith("mp3")) {
        "audio/mp3".toMediaTypeOrNull()
    } else {
        "image/jpeg".toMediaTypeOrNull()
    }

    val requestBody = RequestBody.create(
        mediaType, file
    )

    return MultipartBody.Part.createFormData(partName.toString(), file.name, requestBody)

}

fun prepareFilePartVideo(partName: String?, file: File): MultipartBody.Part {

    val videoBody: RequestBody = file.asRequestBody("video/*".toMediaTypeOrNull())
    val body = MultipartBody.Part.createFormData(partName!!, file.name, videoBody)
    return body


}









