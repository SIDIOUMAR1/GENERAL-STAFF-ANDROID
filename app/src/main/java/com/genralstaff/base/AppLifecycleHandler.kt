package com.genralstaff.base

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log

class AppLifecycleHandler(private var lifecycleDelegates: AppLifecycleDelegates) :
    ActivityLifecycleCallbacks, ComponentCallbacks2 {
    private var appInForeground = false

    /*public fun appLifecycleHandler( delegates:AppLifecycleDelegates) {
        this.lifecycleDelegates = delegates;
    }*/


    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        Log.i("Application", "Started")
    }

    override fun onActivityResumed(activity: Activity) {
        Log.i("Application", "Resumed")
        if (!appInForeground) {
            appInForeground = true
            lifecycleDelegates.onAppForegrounded()
        }
    }

    override fun onActivityPaused(activity: Activity) {
        Log.i("Application", "Paused")
    }

    override fun onActivityStopped(activity: Activity) {
        Log.i("Application", "Stopped")
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        Log.i("Application", "Destroyed")
    }

    override fun onTrimMemory(level: Int) {
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            appInForeground = false
            // lifecycleDelegate instance was passed in on the constructor
            lifecycleDelegates.onAppBackgrounded()
        }
    }

    override fun onConfigurationChanged(configuration: Configuration) {}
    override fun onLowMemory() {}
    interface AppLifecycleDelegates {
        fun onAppBackgrounded()
        fun onAppForegrounded()
    }
}
