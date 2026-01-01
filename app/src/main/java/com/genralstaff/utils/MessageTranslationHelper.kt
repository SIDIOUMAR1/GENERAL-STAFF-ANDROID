package com.genralstaff.utils

import android.content.Context
import com.genralstaff.R

object MessageTranslationHelper {


    fun translateLocationMessage(context: Context, message: String): String {
        var translatedMessage = message

        translatedMessage = translatedMessage.replace(
            "Here is the pickup location link:",
            context.getString(R.string.pickup_location_text),
            ignoreCase = true
        )


        translatedMessage = translatedMessage.replace(
            "Here is the delivery location link:",
            context.getString(R.string.delivery_location_text),
            ignoreCase = true
        )

        return translatedMessage
    }


    fun isLocationMessage(message: String): Boolean {
        return message.contains("Here is the pickup location link:", ignoreCase = true) ||
                message.contains("Here is the delivery location link:", ignoreCase = true)
    }
}
