package com.genralstaff

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

object UpdateManager {

    private const val PLAY_STORE_URL =
        "https://play.google.com/store/apps/details?id=com.genralstaff"

    fun checkForUpdate(activity: Activity, onNoUpdateNeeded: () -> Unit) {
        val remoteConfig = FirebaseRemoteConfig.getInstance()

        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()

        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(
            mapOf("staff_required_version" to 1L)
        )

        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val requiredVersion = remoteConfig.getLong("staff_required_version")
                val currentVersion = getCurrentVersionCode(activity)

                if (currentVersion < requiredVersion) {
                    showUpdateDialog(activity)
                } else {
                    onNoUpdateNeeded()
                }
            } else {
                onNoUpdateNeeded()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun getCurrentVersionCode(activity: Activity): Long {
        return try {
            val pInfo = activity.packageManager
                .getPackageInfo(activity.packageName, 0)
            pInfo.versionCode.toLong()
        } catch (e: Exception) {
            0L
        }
    }

    private fun showUpdateDialog(activity: Activity) {
        val dialog = AlertDialog.Builder(activity)
            .setTitle("Mise à jour requise")
            .setMessage(
                "Une nouvelle version est disponible. " +
                        "Veuillez mettre à jour l'application pour continuer."
            )
            .setCancelable(false)
            .setPositiveButton("Mettre à jour") { _, _ ->
                openPlayStore(activity)
            }
            .create()

        if (!activity.isFinishing) {
            dialog.show()
        }
    }

    private fun openPlayStore(activity: Activity) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(PLAY_STORE_URL)
        )
        activity.startActivity(intent)
    }
}