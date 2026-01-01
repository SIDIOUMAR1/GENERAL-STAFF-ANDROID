package com.genraluser.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent

import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.genralstaff.BuildConfig
import com.genralstaff.R
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes

abstract class LocationUpdateUtilityActivity : AppCompatActivity() {

    private val TAG = "LocationUpdateUtility"
    private lateinit var mActivity: Activity
    private var locationRequest: LocationRequest? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    var latitude = 0.0
    var longitude = 0.0

    @RequiresApi(Build.VERSION_CODES.M)
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.isNotEmpty()) {
                permissions.entries.forEach {
                    Log.d(TAG, "${it.key} = ${it.value}")
                }

                val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION]
                val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION]

                if (fineLocation == true && coarseLocation == true) {
                    Log.e(TAG, "Permission Granted Successfully")
                    checkGpsOn()
                } else {
                    Log.e(TAG, "Permission not granted")
                    checkPermissionDenied(permissions.keys.first())
                }
            }

        }


    private val gpsOnLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                Log.e(TAG, "GPS Turned on successfully")
                startLocationUpdates()
            } else if (result.resultCode == RESULT_CANCELED) {
                Log.e(TAG, "GPS Turned on failed")
                locAlertDialogMethod()

            }
        }

    private fun locAlertDialogMethod() {
        val locationDialog = Dialog(this)
        locationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        locationDialog.setContentView(R.layout.location_alert)

        locationDialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        locationDialog.setCancelable(true)
        locationDialog.setCanceledOnTouchOutside(true)
        locationDialog.window!!.setGravity(Gravity.CENTER)

        locationDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        locationDialog.findViewById<Button>(R.id.btnTryAgain).setOnClickListener {
            locationDialog.dismiss()
            checkGpsOn()
        }

        locationDialog.show()

    }

    @RequiresApi(Build.VERSION_CODES.M)
    open fun getLiveLocation(activity: Activity) {

        mActivity = activity

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity)

        checkLocationPermissions()
        locationCallback = object : LocationCallback() {

            override fun onLocationResult(p0: LocationResult) {
                p0 ?: return
                for (location in p0.locations) {
                    // Update UI with location data
                    // ...
                    Log.e(
                        TAG, "==========" + location.latitude.toString() + ", " +
                                location.longitude + "========="
                    )

                    if (latitude == 0.0 && longitude == 0.0) {
                        latitude = location.latitude
                        longitude = location.longitude
                        updatedLatLng(location.latitude, location.longitude)
                    } else if (location.hasMovedSignificantly(latitude, longitude)) {
                        latitude = location.latitude
                        longitude = location.longitude
                        onChangedLocation(location.latitude, location.longitude)
                    }
                }
            }

        }
    }
    private fun Location.hasMovedSignificantly(prevLat: Double, prevLng: Double, threshold: Float = 10f): Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(prevLat, prevLng, this.latitude, this.longitude, results)
        return results[0] > threshold
    }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkLocationPermissions() {
        if (hasPermissions(permissions)) {
            Log.e(TAG, "Permissions Granted")
            // getLiveLocation(requireActivity())
            checkGpsOn()
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            checkPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            checkPermissionDenied(Manifest.permission.ACCESS_COARSE_LOCATION)
        } else {
            Log.e(TAG, "Request for Permissions")
            requestPermission2()
        }
    }

    // util method
    private fun hasPermissions(permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(mActivity, it) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestPermission2() {
        requestMultiplePermissions.launch(permissions)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermissionDenied(permission: String) {
        if (shouldShowRequestPermissionRationale(permission)) {
            Log.e(TAG, "Permissions Denied")
            val mBuilder = AlertDialog.Builder(mActivity)
            val dialog: AlertDialog =
                mBuilder.setTitle(R.string.alert)
                    .setMessage(R.string.permission_rationale)
                    .setPositiveButton(R.string.ok2
                    ) { dialog, which ->
                        // Request permission
                        requestPermission2()
                    }.create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    ContextCompat.getColor(
                        mActivity, R.color.app_theme_color
                    )
                )
            }
            dialog.show()


        } else {
            val builder = AlertDialog.Builder(mActivity)
            val dialog: AlertDialog =
                builder.setTitle(R.string.alert)
                    .setMessage(R.string.permissionRequired)
                    .setCancelable(
                        false
                    )
                    .setPositiveButton("Settings") { dialog, which ->
                        // Build intent that displays the App settings screen.
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            BuildConfig.APPLICATION_ID,
                            null
                        )
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }.create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    ContextCompat.getColor(
                        mActivity, R.color.app_theme_color
                    )
                )
            }
            dialog.show()
//            locAlertDialogMethod()

        }
    }



    private fun checkGpsOn() {
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000 // 1 second
            fastestInterval = 1000 // 1 second
            smallestDisplacement = 1f // 1 meter
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest!!)
        builder.setAlwaysShow(true)


        val result =
            LocationServices.getSettingsClient(mActivity).checkLocationSettings(builder.build())
        result.addOnCompleteListener { task ->
            try {

                val response = task.getResult(ApiException::class.java)
                Log.e(TAG, "==========GPS is ON=============")

                startLocationUpdates()
            } catch (e: ApiException) {
                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        val resolvableApiException = e as ResolvableApiException

                        gpsOnLauncher.launch(
                            IntentSenderRequest.Builder(resolvableApiException.resolution).build()
                        )
                    }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {

                    }
                }
            }
        }

    }

    //call startLocationUpdates() method for start live location update
    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                mActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                mActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            hasPermissions(permissions)
            return
        }

        locationRequest?.let {
            fusedLocationClient.requestLocationUpdates(
                it,
                locationCallback,
                Looper.getMainLooper()
            )
        }
        Log.e(TAG, "Get Live Location Start")
    }


    //call stopLocationUpdates() method for stop live location update
    fun stopLocationUpdates() {
        try {
            if (fusedLocationClient != null) {

                if (locationCallback != null) {

                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            }
            Log.e(TAG, "Get Live Location Stop")
        } catch (e: Exception) {
        }
    }

    abstract fun updatedLatLng(lat: Double, lng: Double)
    abstract fun onChangedLocation(lat: Double, lng: Double)

}