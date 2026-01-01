package com.munchhh.munchhhuser.prefs

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import android.util.Log
import com.genralstaff.utils.MyApplication
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging


class SharePrefs(context: Context) {
    private val prefsName = "prefs.workforme"
    private val prefs: SharedPreferences =
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = prefs.edit()
    private val fcmToken = "fcmToken"
    private val TAG = "SharePrefs"

    fun saveString(key: String?, value: String?) {
        editor.putString(key, value)
        editor.apply()
    }

    fun savePrefrenceLanguage(key: String, value: Any) {

        val preference = MyApplication.getInstance()!!.applicationContext.getSharedPreferences(
            "SHARED_NAME_LANGUAGE",
            Context.MODE_PRIVATE
        )
        val editor = preference.edit()

        when (value) {
            is String -> editor.putString(key, value)
            is Boolean -> editor.putBoolean(key, value)
            is Int -> editor.putInt(key, value)
        }
        editor.apply()
    }

    inline fun <reified T> getPrefrenceLanguage(key: String, deafultValue: T): T {
        /*
            val preference =     MyApplication.getinstance().applicationContext.getSharedPreferences(GlobalVariables.CONSTANTS.SHARED_NAME, 0)
        */
        val preference = MyApplication.getInstance()!!.applicationContext.getSharedPreferences(
            "SHARED_NAME_LANGUAGE",
            Context.MODE_PRIVATE
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

    fun getString(key: String?): String? {
        return prefs.getString(key, "")
    }

    fun saveInt(key: String?, value: Int) {
        editor.putInt(key, value)
        editor.apply()
    }


    fun getInt(key: String?): Int {
        return prefs.getInt(key, 0)
    }

    private fun setFcmToken(token: String?) {
        editor.putString(fcmToken, token)
        editor.apply()
    }

    fun getFcmToken(): String? {
        return prefs.getString(fcmToken, "")
    }

    fun getFirebaseToken() {

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token: String ->
            if (!TextUtils.isEmpty(token)) {
                Log.d(TAG, "retrieve token successful : $token")
                setFcmToken(token)
                saveString("deviceToken", token)
            } else {
                Log.w(TAG, "token should not be null...")
            }
        }.addOnFailureListener { e: Exception? -> }.addOnCanceledListener {}
            .addOnCompleteListener { task: Task<String> ->
                try {
                    Log.v(
                        TAG,
                        "This is the token : " + task.result
                    )
                } catch (e: Exception) {
                }
            }
    }

    fun storeisLogin(isLogin: Boolean) {
        editor = prefs.edit()
        editor.putBoolean("isLogin", isLogin)
        editor.apply()
    }

    fun retrieveisLogin(): Boolean {
        return prefs.getBoolean("isLogin", false)
    }

    /*--function to clear preferences--*/
    fun clearSharedPreference() {
        editor.clear().apply()
    }
}