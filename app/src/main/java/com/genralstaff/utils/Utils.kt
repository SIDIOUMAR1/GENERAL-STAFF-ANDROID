package com.genralstaff.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import com.genralstaff.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


object Utils {
    @JvmStatic
    fun getCurrentDate(): String {
        val currentDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
        return currentDate

    }

    @JvmStatic
    fun getCurrentTime(): String {
        val currentDate = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        return currentDate.uppercase(Locale.getDefault())

    }

    private const val SECOND_MILLIS = 1000
    private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private const val DAY_MILLIS = 24 * HOUR_MILLIS
    private const val WEEK_MILLIS = 7 * DAY_MILLIS
    private const val MONTH_MILLIS = 4 * WEEK_MILLIS.toLong()
    private const val YEAR_MILLIS = 12 * MONTH_MILLIS


    @JvmStatic
    fun inviteLink(activity: Context, number: String) {
        val uri = Uri.parse("smsto:${number}")
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        intent.putExtra("sms_body", "The SMS text")
        activity.startActivity(intent)

    }

    @JvmStatic
    fun showErrorDialog(activity: Context, msg: String) {
        val errorDialog = Dialog(activity)
        errorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        errorDialog.setContentView(R.layout.error_popup)
        errorDialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        errorDialog.setCancelable(true)
        errorDialog.setCanceledOnTouchOutside(false)
        errorDialog.window!!.setGravity(Gravity.CENTER)
        errorDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        var tvMsg = errorDialog.findViewById<TextView>(R.id.tv_msg)
        var tvOk = errorDialog.findViewById<TextView>(R.id.tv_ok)
        tvMsg.text = msg

        tvOk.setOnClickListener {
            errorDialog.dismiss()
        }

        errorDialog.show()
    }

    @JvmStatic
    fun showSuccessDialog(activity: Activity, msg: String) {
        val errorDialog = Dialog(activity)
        errorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        errorDialog.setContentView(R.layout.error_popup)
        errorDialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        errorDialog.setCancelable(true)
        errorDialog.setCanceledOnTouchOutside(false)
        errorDialog.window!!.setGravity(Gravity.CENTER)
        errorDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val tvMsg = errorDialog.findViewById<TextView>(R.id.tv_msg)
        val tvOk = errorDialog.findViewById<TextView>(R.id.tv_ok)
        tvMsg.text = msg

        tvOk.setOnClickListener {
            activity.finish()
            errorDialog.dismiss()
        }

        errorDialog.show()
    }
    @JvmStatic
    fun showSuccessnewDialog(activity: Activity, msg: String) {
        val errorDialog = Dialog(activity)
        errorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        errorDialog.setContentView(R.layout.error_popup)
        errorDialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        errorDialog.setCancelable(true)
        errorDialog.setCanceledOnTouchOutside(false)
        errorDialog.window!!.setGravity(Gravity.CENTER)
        errorDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val tvMsg = errorDialog.findViewById<TextView>(R.id.tv_msg)
        val tvOk = errorDialog.findViewById<TextView>(R.id.tv_ok)
        tvMsg.text = msg

        tvOk.setOnClickListener {
//            activity.finish()
            errorDialog.dismiss()
        }

        errorDialog.show()
    }


    fun internetAvailability(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false
    }

    fun isValidEmail(target: CharSequence?): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    fun isEmpty(str: String?): Boolean {
        return TextUtils.isEmpty(str)
    }

    fun getTimeInMillis(date: String): Long {

        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val dateServer = fmt.parse(date)
        return dateServer.time
    }

    //for backend timestamp format
    fun convertTimeStampToTimeBackend(timestamp: Long): String? {
        val calendar = Calendar.getInstance()
        val tz = TimeZone.getDefault()
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.timeInMillis))
        val sdf = SimpleDateFormat("hh:mm aa")
        sdf.timeZone = tz
        val currenTimeZone = Date(timestamp * 1000)
        return sdf.format(currenTimeZone)
    }

    @SuppressLint("DefaultLocale")
    fun getVideoTimeInSeconds(context: Context, videoUrl: Uri): Int {
        val mp: MediaPlayer = MediaPlayer.create(context, Uri.parse(videoUrl.toString()))
        val duration = mp.duration
        mp.release()
        var str =
            String.format(Locale.US,TimeUnit.MILLISECONDS.toSeconds(duration.toLong()).toString()).toInt()

        return str
    }

    fun getTimeAgo(timeMillis: Date): String? {
        var time = timeMillis.time
        if (time < 1000000000000L) {
            time *= 1000
        }
        val now = System.currentTimeMillis()

        val diff = now - time
        when {
            diff < 2 * SECOND_MILLIS -> {
                return "Just now"
            }

            diff < 60 * SECOND_MILLIS -> {
                return (diff / SECOND_MILLIS).toString() + " sec"
            }

            diff < 2 * MINUTE_MILLIS -> {
                return "1 min"
            }

            diff < 50 * MINUTE_MILLIS -> {
                return (diff / MINUTE_MILLIS).toString() + " min"
            }

            diff < 90 * MINUTE_MILLIS -> {
                return "1 hour"
            }

            diff < 24 * HOUR_MILLIS -> {
                return (diff / HOUR_MILLIS).toString() + " hours"
            }

            diff < 48 * HOUR_MILLIS -> {
                return "1 day"
            }

            diff < 7 * DAY_MILLIS -> {
                return (diff / DAY_MILLIS).toString() + " days";
            }

            diff < 2 * WEEK_MILLIS.toLong() -> {
                return "1 week"
            }

            diff < 4 * WEEK_MILLIS.toLong() -> {
                return (diff / WEEK_MILLIS.toLong()).toString() + " week"
            }

            diff < 2 * MONTH_MILLIS -> {
                return "1 month"
            }

            diff < 12 * MONTH_MILLIS -> {
                return (diff / MONTH_MILLIS).toString() + " month"
            }

            diff < 2 * YEAR_MILLIS -> {
                return "1 year"
            }

            else -> {
                return (diff / YEAR_MILLIS).toString() + " years"
            }
        }
    }

    fun getNotificationTimeAgo(timeMillis: Long): String? {
        var time = timeMillis
        if (time < 1000000000000L) {
            time *= 1000
        }

        val now = System.currentTimeMillis()

        val diff = now - time
        when {
            diff < 2 * SECOND_MILLIS -> {
                return "Just now"
            }

            diff < 60 * SECOND_MILLIS -> {
                return (diff / SECOND_MILLIS).toString() + " sec"
            }

            diff < 2 * MINUTE_MILLIS -> {
                return "1 min"
            }

            diff < 50 * MINUTE_MILLIS -> {
                return (diff / MINUTE_MILLIS).toString() + " min"
            }

            diff < 90 * MINUTE_MILLIS -> {
                return "an hour"
            }

            diff < 24 * HOUR_MILLIS -> {
                return (diff / HOUR_MILLIS).toString() + " hours"
            }

            diff < 48 * HOUR_MILLIS -> {
                return "1 day"
            }

            diff < 7 * DAY_MILLIS -> {
                return (diff / DAY_MILLIS).toString() + " days";
            }

            diff < 2 * WEEK_MILLIS.toLong() -> {
                return "1 week"
            }

            diff < 4 * WEEK_MILLIS.toLong() -> {
                return (diff / WEEK_MILLIS.toLong()).toString() + " week"
            }

            diff < 2 * MONTH_MILLIS -> {
                return "1 month"
            }

            diff < 12 * MONTH_MILLIS -> {
                return (diff / MONTH_MILLIS).toString() + " month"
            }

            diff < 2 * YEAR_MILLIS -> {
                return "a year"
            }

            else -> {
                return (diff / YEAR_MILLIS).toString() + " years"
            }
        }
    }

    fun convertTimestampToTime(timestamp: Long): String? {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.timeZone = TimeZone.getTimeZone("UTC")
        val sdf = SimpleDateFormat("hh:mm aa", Locale.ENGLISH)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(calendar.time)
    }

    fun convertTimestampToDate(timestamp: Long): String? {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.timeZone = TimeZone.getTimeZone("UTC")
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(calendar.time)
    }

    /*fun convertDateStringToLongUTC(stringUTCDate: String?): Long {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm:ss a", Locale.ENGLISH)
        val localDate: LocalDateTime = LocalDateTime.parse(stringUTCDate, formatter)
        return localDate.atOffset(ZoneOffset.UTC).toInstant().toEpochMilli()
    }*/

    fun isJSONValid(test: String?): Boolean {
        try {
            JSONObject(test)
        } catch (ex: JSONException) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                JSONArray(test)
            } catch (ex1: JSONException) {
                return false
            }
        }
        return true
    }

    // Method to save an bitmap to a file
    fun bitmapToFile(bitmap: Bitmap, mContext: Context): Uri {
        // Get the context wrapper
        val wrapper = ContextWrapper(mContext)

        // Initialize a new file instance to save bitmap object
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            // Compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Return the saved bitmap uri
        return Uri.parse(file.absolutePath)
    }

    //convert a data class to a map
    fun <T> T.serializeToMap(): Map<String, String> {
        return convert()
    }

    //convert a data class to a map
    fun createPartFromString(string: String?): RequestBody {
        return RequestBody.create(
            MultipartBody.FORM, string!!
        )
    }

    // convert a map to a data class
    inline fun <reified T> Map<String, Any>.toDataClass(): T {
        return convert()
    }

    //convert an object of type I to type O
    inline fun <I, reified O> I.convert(): O {
        val json = Gson().toJson(this)
        return Gson().fromJson(json, object : TypeToken<O>() {}.type)
    }

    fun processDate(
        @NonNull tv: TextView,
        @NonNull viewItem: View,
        dateAPIStr: String,
        dateAPICompareStr: String,
        isFirstItem: Boolean
    ) {
        val f = SimpleDateFormat("dd/MM/yyyy")
        if (isFirstItem) {
            //first item always got date/today to shows
            //and overkill to compare with next item flow
            var dateFromAPI: Date? = null
            try {
                dateFromAPI = f.parse(dateAPIStr)
                when {
                    DateUtils.isToday(dateFromAPI.time) -> tv.text = "today"
                    DateUtils.isToday(dateFromAPI.time + DateUtils.DAY_IN_MILLIS) -> tv.text =
                        "yesterday"

                    else -> tv.text = dateAPIStr
                }
                tv.includeFontPadding = false
                tv.visibility = View.VISIBLE
                viewItem.visibility = View.VISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
                tv.visibility = View.GONE
                viewItem.visibility = View.GONE
            }
        } else {
            if (!dateAPIStr.equals(dateAPICompareStr, ignoreCase = true)) {
                try {
                    val dateFromAPI = f.parse(dateAPIStr)
                    when {
                        DateUtils.isToday(dateFromAPI.time) -> tv.text = "today"
                        DateUtils.isToday(dateFromAPI.time + DateUtils.DAY_IN_MILLIS) -> tv.text =
                            "yesterday"

                        else -> tv.text = dateAPIStr
                    }
                    tv.includeFontPadding = false
                    tv.visibility = View.VISIBLE
                    viewItem.visibility = View.VISIBLE
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    tv.visibility = View.GONE
                    viewItem.visibility = View.GONE
                }
            } else {
                tv.visibility = View.GONE
                viewItem.visibility = View.GONE
            }
        }
    }

    fun convertDateStyle(date: String, inputFormat: String, outputFormate: String): String {

        var finalDate = ""
        val input = SimpleDateFormat(inputFormat)
        val output = SimpleDateFormat(outputFormate)
        try {
            var oneWayTripDate = input.parse(date) // parse input
            finalDate = output.format(oneWayTripDate)
        } catch (e: Exception) {
            e.printStackTrace()
            finalDate = ""
        }
        return finalDate
    }

    fun convertTimeStampToDate2(timestamp: Long): String? {
        val calendar = Calendar.getInstance()
        val tz = TimeZone.getDefault()
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.timeInMillis))
        val sdf = SimpleDateFormat("dd/MM/yyyy - hh:mm a")
        sdf.timeZone = tz
        val currenTimeZone = Date(timestamp * 1000)
        return sdf.format(currenTimeZone)
    }

    fun getMiles(distanceInKm: Double): Double {
        val values = 1.6
        return distanceInKm / values
    }

    var ROUTE = ""

    fun showToast(mContext: Context?, message: String?) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
    }

    fun log(activity: Activity, str: String) {
        Log.e(activity.localClassName, str)
    }

    @JvmStatic
    fun convertUTCToLocal(dateStr: String, formatStyle: String): String {

        var finalConversion = ""
        try {
            val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
            df.timeZone = TimeZone.getTimeZone("UTC")
            val date = df.parse(dateStr)

            val df2 = SimpleDateFormat(formatStyle, Locale.ENGLISH)
            df2.timeZone = TimeZone.getDefault()
            val formattedDate = df2.format(date)
            finalConversion = formattedDate
        } catch (e: Exception) {
        }

        return finalConversion
    }


    @JvmStatic
    fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun getMessageDate(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp * 1000
//        val date = DateFormat.format("     HH:mm aa\ndd/MM/yyyy", calendar).toString()
        val date = DateFormat.format("     HH:mm aa", calendar).toString()
        return date
    }

    fun timeStampToTime(timestamp: Int): String {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp * 1000L
        return DateFormat.format("HH:mm a", calendar).toString()
    }

    @JvmStatic
    fun getNotificationTime(time_stamp: String): String? {
        var stringDate: String? = null
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = dateFormat.parse(time_stamp)
            val current = Calendar.getInstance().time
            var diffInSeconds = (current.time - date.time) / 1000
            val sec = if (diffInSeconds >= 60) diffInSeconds % 60 else diffInSeconds
            val min = if ((diffInSeconds / 60).also {
                    diffInSeconds = it
                } >= 60) diffInSeconds % 60 else diffInSeconds
            val hrs = if ((diffInSeconds / 60).also {
                    diffInSeconds = it
                } >= 24) diffInSeconds % 24 else diffInSeconds
            val days = if ((diffInSeconds / 24).also {
                    diffInSeconds = it
                } >= 30) diffInSeconds % 30 else diffInSeconds
            val weeks = days / 7
            val months = if ((diffInSeconds / 30).also {
                    diffInSeconds = it
                } >= 12) diffInSeconds % 12 else diffInSeconds
            val years = (diffInSeconds / 12).also { diffInSeconds = it }
            stringDate = when {
                years > 0 -> if (years == 1L) "1 year ago" else "$years years ago"
                months > 0 -> if (months == 1L) "1 month ago" else "$months months ago"
                weeks > 0 -> if (weeks == 1L) "1 week ago" else "$weeks weeks ago"
                days > 0 -> if (days == 1L) "1 day ago" else "$days days ago"
                hrs > 0 -> if (hrs == 1L) "1 hour ago" else "$hrs hours ago"
                min > 0 -> if (min == 1L) "1 minute ago" else "$min minutes ago"
                else -> "just now"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return stringDate
    }

    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false
    }

    fun getAbsolutePath(activity: Context, uri: Uri): String {
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            val projection = arrayOf("_data")
            val cursor: Cursor?
            try {
                cursor = activity.contentResolver.query(uri, projection, null, null, null)
                val columnIndex = cursor!!.getColumnIndexOrThrow("_data")
                if (cursor.moveToFirst()) {
                    return cursor.getString(columnIndex)
                }
            } catch (e: Exception) {
                // Eat it
                e.printStackTrace()
            }
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path!!
        }
        return ""
    }

    fun EditText.showSpaceOnKeyboardOpen(space: View) {
        this.viewTreeObserver.addOnGlobalLayoutListener {
            if (keyboardShown(this.rootView)) {
                Log.i("Keyboard", "UP")
                space.visibility = View.VISIBLE
            } else {
                Log.i("Keyboard", "DOWN")
                space.visibility = View.GONE
            }
        }
        this.setOnFocusChangeListener { view, b ->
            if (b) {
                space.visibility = View.VISIBLE
            } else {
                space.visibility = View.GONE
            }
        }

    }

    fun Intent.clearStack(additionalFlags: Int = 0) {
        flags = additionalFlags or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    private fun keyboardShown(rootView: View): Boolean {
        val softKeyboardHeight = 200
        val r = Rect()
        rootView.getWindowVisibleDisplayFrame(r)
        val dm = rootView.resources.displayMetrics
        val heightDiff: Int = rootView.bottom - r.bottom
        return heightDiff > softKeyboardHeight * dm.density
    }

}