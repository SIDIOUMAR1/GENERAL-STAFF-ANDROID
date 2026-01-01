package com.genralstaff.auth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.util.SparseIntArray
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.genralstaff.R
import com.genralstaff.base.LANGUAGE
import com.genralstaff.databinding.ActivitySplashBinding
import com.genralstaff.home.HomeActivity
import com.genralstaff.home.ui.ChooseLanguageActivity
import com.genralstaff.utils.MyApplication
import com.genralstaff.utils.changeLanguageSplash
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    lateinit var binding: ActivitySplashBinding
    val activityScope = CoroutineScope(Dispatchers.Main)
    private val requestPermission = 20
    private var mErrorString: SparseIntArray? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Glide.with(this)
            .load(R.drawable.splash_icon)
            .into(binding.ivImage)
        val lang = MyApplication.prefs!!.getPrefrenceLanguage(LANGUAGE, "ar").toString()
        when (lang) {
            "en" -> {
                MyApplication.prefs!!.savePrefrenceLanguage(LANGUAGE, "en")

            }

            "fr" -> {
                MyApplication.prefs!!.savePrefrenceLanguage(LANGUAGE, "fr")


            }

            else -> {
                MyApplication.prefs!!.savePrefrenceLanguage(LANGUAGE, "ar")
            }
        }
//        changeLanguage(this@SplashActivity)
        changeLanguageSplash(this)

        setUpPermission()
        printKeyHash(this)
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setUpPermission() {
        mErrorString = SparseIntArray()

        val currentapiVersion = Build.VERSION.SDK_INT
        if (currentapiVersion >= Build.VERSION_CODES.M) {
            val array = arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            val array2 = arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestAppPermissions(array2, R.string.permission, requestPermission)

            } else {
                requestAppPermissions(array, R.string.permission, requestPermission)
            }
        } else {
            onPermissionsGranted()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun requestAppPermissions(
        requestedPermissions: Array<String>,
        stringId: Int,
        requestCode: Int
    ) {

        mErrorString!!.put(requestCode, stringId)
        var permissionCheck = PackageManager.PERMISSION_GRANTED
        var shouldShowRequestPermissionRationale = false

        for (permission in requestedPermissions) {
            permissionCheck += ContextCompat.checkSelfPermission(this, permission)
            shouldShowRequestPermissionRationale =
                shouldShowRequestPermissionRationale || ActivityCompat.shouldShowRequestPermissionRationale(
                    this, permission
                )
        }

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale) {
                val snack = Snackbar.make(
                    findViewById(android.R.id.content),
                    stringId,
                    Snackbar.LENGTH_INDEFINITE
                )
                val view = snack.view
//
//                val tv =
//                    view.findViewById(com.google.android.materia) as TextView
//                tv.setTextColor(Color.WHITE)
                snack.setAction(getString(R.string.grant))
                { ActivityCompat.requestPermissions(this, requestedPermissions, requestCode) }
                    .show()
            } else {
                ActivityCompat.requestPermissions(this, requestedPermissions, requestCode)
            }
        } else {
            onPermissionsGranted()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var permissionCheck = PackageManager.PERMISSION_GRANTED
        for (permission in grantResults) {
            permissionCheck += permission
        }
        if (grantResults.isNotEmpty() && permissionCheck == PackageManager.PERMISSION_GRANTED) {
            onPermissionsGranted()
        } else {
            mErrorString!!.put(requestCode, R.string.permission)
            var shouldShowRequestPermissionRationale = false

            for (permission in permissions) {
                permissionCheck += ContextCompat.checkSelfPermission(
                    this,
                    permission
                )
                shouldShowRequestPermissionRationale =
                    shouldShowRequestPermissionRationale || ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        permission
                    )
            }

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale) {
                    val snack = Snackbar.make(
                        findViewById(android.R.id.content), R.string.permission,
                        Snackbar.LENGTH_INDEFINITE
                    )
                    val view = snack.view
//                    val tv =
//                        view.findViewById(com.google.android.material.R.id.snackbar_text) as TextView

//                    tv.setTextColor(Color.WHITE)
                    snack.setAction(getString(R.string.grant)) {
                        ActivityCompat.requestPermissions(
                            this, permissions, requestCode
                        )
                    }.show()
                } else {
                    ActivityCompat.requestPermissions(this, permissions, requestCode)
                }
            } else {
                onPermissionsGranted()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

    }


    @RequiresApi(Build.VERSION_CODES.P)
    private fun onPermissionsGranted() {
        activityScope.launch {
            delay(3000)
            if (MyApplication.prefs?.retrieveisLogin()!!) {
                MyApplication.prefs?.storeisLogin(true)
                val intent = Intent(this@SplashActivity, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish() // Ensures SplashActivity is removed
            } else {
                val lang_save =
                    MyApplication.prefs!!.getPrefrenceLanguage("LANGUAGE_save", "0").toString()
                if (lang_save == "1") {
                    val intent = Intent(this@SplashActivity, LoginActivity::class.java)

//                val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish() // Ensures SplashActivity is removed
                } else {
                    val intent = Intent(this@SplashActivity, ChooseLanguageActivity::class.java)
                        .putExtra("type", "splash")

//                val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish() // Ensures SplashActivity is removed
                }

            }


        }
    }


    private fun printKeyHash(context: Activity): String? {
        val packageInfo: PackageInfo
        var key: String? = null
        try {
            val packageName = context.applicationContext.packageName
            packageInfo = context.packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNATURES
            )
            for (signature in packageInfo.signatures!!) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                key = String(Base64.encode(md.digest(), 0))
                Log.e("keyHash", key)
            }
        } catch (e1: PackageManager.NameNotFoundException) {
        } catch (e: NoSuchAlgorithmException) {
        } catch (e: Exception) {
        }
        return key
    }
}