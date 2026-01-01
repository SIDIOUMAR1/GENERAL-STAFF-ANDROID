package com.genralstaff.utils

import java.text.SimpleDateFormat
import java.util.*

object DateHelper {

    private const val SECOND_MILLIS = 1000
    private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private const val DAY_MILLIS = 24 * HOUR_MILLIS

    private const val DATE_FORMAT = "dd-MMM-yyyy hh:mm a"

    private const val HOURS_12 = "hh:mm a"
    private const val HOURS_12_ONLY = "hh:mm"
    private const val CAL_DATE_FORMAT2 = "dd/MM/yy"
    private const val CAL_DATE_FORMAT = "MM/yy"

    //    2023-10-15
    private const val CAL_DATE_FORMAT_New = "MM/yy"
    private const val DATE_FORMAT_CHAT = "dd/MM/yyyy"

    private var dateFormatCalnNew: SimpleDateFormat =
        SimpleDateFormat(CAL_DATE_FORMAT_New, Locale.getDefault())
    private var dateFormatCal: SimpleDateFormat =
        SimpleDateFormat(CAL_DATE_FORMAT, Locale.getDefault())
    private var dateFormatCal2: SimpleDateFormat =
        SimpleDateFormat(CAL_DATE_FORMAT2, Locale.getDefault())
    private var dateFormatChat: SimpleDateFormat =
        SimpleDateFormat(DATE_FORMAT_CHAT, Locale.getDefault())

    fun getDate(dayOfMonth: Int, monthOfYear: Int, year: Int): Date? {
        val calendar = Calendar.getInstance()
        calendar[Calendar.YEAR] = year
        calendar[Calendar.MONTH] = monthOfYear
        calendar[Calendar.DAY_OF_MONTH] = dayOfMonth
        return calendar.time
    }

    fun getDate(date: Date): String {
        return dateFormatCal.format(date)
    }

    fun getDate2(date: Date): String {
        return dateFormatCal2.format(date)
    }

    fun getDateNew(date: Date): String {
        return dateFormatCalnNew.format(date)
    }


    fun getTimeAgo(input: Long): String {
        var time = input
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000
        }
        val now = Calendar.getInstance().timeInMillis
        if (time > now || time <= 0) {
            return "just now"
        }

        // TODO: localize
        val diff = now - time
        return when {
            diff < MINUTE_MILLIS -> {
                "just now"
            }

            diff < 2 * MINUTE_MILLIS -> {
                "a minute ago"
            }

            diff < 50 * MINUTE_MILLIS -> {
                val dif = diff / MINUTE_MILLIS
                "$dif minutes ago"
            }

            diff < 90 * MINUTE_MILLIS -> {
                "an hour ago"
            }

            diff < 24 * HOUR_MILLIS -> {
                val dif = diff / HOUR_MILLIS
                "$dif hours ago"
            }

            diff < 48 * HOUR_MILLIS -> {
                "yesterday"
            }

            diff / DAY_MILLIS < 7 -> {
                "yesterday"
            }

            else -> {
                val cal = Calendar.getInstance()
                cal.timeInMillis = time
                dateFormatChat.format(cal.time)
            }
        }
    }

}