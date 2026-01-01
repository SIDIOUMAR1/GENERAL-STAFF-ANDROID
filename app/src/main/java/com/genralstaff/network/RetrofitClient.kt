package com.genralstaff.network

import android.util.Log
import com.genralstaff.base.BASE_URL
import com.genralstaff.utils.MyApplication
import com.google.gson.GsonBuilder

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit.Builder

import java.util.concurrent.TimeUnit


val retrofitService by lazy {
    RetrofitClient.create()
}


class RetrofitClient {
    companion object {
        fun create(): RetrofitInterface {
            var gson = GsonBuilder()
                .setLenient()
                .create()

            val retrofit = Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient())
                .build()
            return retrofit.create(RetrofitInterface::class.java)
        }

        private fun httpClient(): OkHttpClient {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            return OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addInterceptor(provideHeaderInterceptor())
                .connectTimeout(30 * 3, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS).build()

        }

        private fun provideHeaderInterceptor(): Interceptor {

            return Interceptor { chain ->
                val request: Request
                if ( MyApplication.prefs!!.getString(
                        "AUTH_KEY_value"
                    )!!.isNotEmpty()
                ) {
                    Log.e("Authorization", "" + MyApplication.prefs!!.getString("AUTH_KEY_value"))
                    val auth = MyApplication.prefs!!.getString("AUTH_KEY_value")

                    request = chain.request().newBuilder().header("Authorization", "Bearer $auth")

                        .header("Accept", "application/json")
                        .build()
                } else {

                    request = chain.request().newBuilder()
                        .header("Accept", "application/json")

                        .build()
                }
                chain.proceed(request)


            }


        }


    }


}
