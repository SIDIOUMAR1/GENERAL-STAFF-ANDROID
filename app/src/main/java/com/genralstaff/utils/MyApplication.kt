package com.genralstaff.utils

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.genralstaff.R
import com.genralstaff.base.AppLifecycleHandler
import com.genralstaff.sockets.SocketManager
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import com.munchhh.munchhhuser.prefs.SharePrefs


class MyApplication : Application(), AppLifecycleHandler.AppLifecycleDelegates {

    companion object {
        var mInstance: MyApplication? = null
        var prefs: SharePrefs? = null
        var mSocketManager: SocketManager? = null

        private var lifecycleHandler: AppLifecycleHandler? = null



        @Synchronized
        fun getInstance(): MyApplication? {
            return mInstance
        }
    }
    fun getSocketManager(): SocketManager? {

        mSocketManager = if (mSocketManager == null) {

            SocketManager()

        } else {
            return mSocketManager
        }

        return mSocketManager

    }
    fun clearData() {
        prefs!!.clearSharedPreference()
    }

    override fun onCreate() {
        super.onCreate()

        mInstance = this
        mSocketManager = getSocketManager()
        FirebaseApp.initializeApp(mInstance!!)
        prefs = SharePrefs(applicationContext)
        GoogleMapsAPI.initialize(this) // Initialize API Key

        Places.initialize(this, getString(R.string.googlePlaceKey_live))


        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        lifecycleHandler = AppLifecycleHandler(this)
        registerLifeCycleHandler(lifecycleHandler)
    }

    override fun onAppBackgrounded() {
        Log.e("Application", "Background")
//        mSocketManager!!.disconnectAll()
    }

    override fun onAppForegrounded() {
        Log.e("Application", "Foreground")
        if (mSocketManager!=null){
        if (!mSocketManager!!.isConnected() || mSocketManager!!.getmSocket() == null) {
            mSocketManager!!.init()
        }
        }

    }

    private fun registerLifeCycleHandler(lifeCycleHandler: AppLifecycleHandler?) {
        registerActivityLifecycleCallbacks(lifeCycleHandler)
        registerComponentCallbacks(lifeCycleHandler)
    }

}
