package com.genralstaff.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject
import java.io.IOException
import retrofit2.HttpException
import java.net.SocketTimeoutException


object ApiCallsHandler {
    private const val MSG = "msg"
    private const val MESSAGE_KEY = "message"
    private const val ERROR_KEY = "error"

    fun getErrorMessage(responseBody: ResponseBody?): String {
        return try {
            val jsonObject = JSONObject(responseBody!!.string())
            when {
                jsonObject.has(MESSAGE_KEY) -> jsonObject.getString(MESSAGE_KEY)
                jsonObject.has(MSG) -> jsonObject.getString(MSG)
                jsonObject.has(ERROR_KEY) -> jsonObject.getString(ERROR_KEY)
                else -> "Something wrong happened"
            }
        } catch (e: Exception) {
            "Something wrong happened"
        }
    }

    suspend inline fun <T> safeApiCall(
        emitter: ErrorEmitter,
        crossinline responseFunction: suspend () -> T
    ): T? {
        return try {
            val response = withContext(Dispatchers.IO) { responseFunction.invoke() }
            response
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ApiCalls", "Call error: ${e.localizedMessage}", e.cause)
            when (e) {
                is HttpException -> {
                    val body = e.response()?.errorBody()
                    emitter.onError(getErrorMessage(body))
                    if (e.code() == 403) {
                        emitter.onError(ErrorType.UNAUTHORIZED)
                    }
                }

                is SocketTimeoutException -> emitter.onError(ErrorType.TIMEOUT)
                is IOException -> emitter.onError(ErrorType.NETWORK)
                else -> emitter.onError(ErrorType.UNKNOWN)
            }
            null
        }
    }

}