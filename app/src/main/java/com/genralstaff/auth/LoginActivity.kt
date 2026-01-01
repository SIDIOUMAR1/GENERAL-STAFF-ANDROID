package com.genralstaff.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.genralstaff.R
import com.genralstaff.base.LANGUAGE
import com.genralstaff.databinding.ActivityLoginBinding
import com.genralstaff.home.HomeActivity
import com.genralstaff.home.ui.ChooseLanguageActivity
import com.genralstaff.network.ErrorType
import com.genralstaff.sockets.SocketManager
import com.genralstaff.utils.CustomProgressDialog
import com.genralstaff.utils.MyApplication
import com.genralstaff.utils.MyApplication.Companion.prefs
import com.genralstaff.utils.Utils
import com.genralstaff.utils.Validator.userLoginValidation
import com.genralstaff.utils.getCity
import com.genralstaff.utils.sessionExpire
import com.genralstaff.viewmodel.AuthViewModel
import com.genraluser.utils.LocationUpdateUtilityActivity

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginActivity : LocationUpdateUtilityActivity() {
    private lateinit var authViewModel: AuthViewModel
private val progressDialog by lazy { CustomProgressDialog() }

    override fun updatedLatLng(lat: Double, lng: Double) {
        prefs!!.saveString("lat", lat.toString())
        prefs!!.saveString("lng", lng.toString())
        prefs!!.saveString("address", getCity(lat.toString(), lng.toString(), this))


    }

    override fun onChangedLocation(lat: Double, lng: Double) {
    }

    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getLiveLocation(this)

        MyApplication.mSocketManager = null

        prefs!!.getFirebaseToken()
        prefs?.getFcmToken()!!.toString()
        viewModelSetupAndResponse()
        binding.tvLanague.setOnClickListener {
            startActivity(Intent(this, ChooseLanguageActivity::class.java)
                .putExtra("type", "login")
  )
        }
        binding.btnLogin.setOnClickListener {
            val lang = prefs!!.getPrefrenceLanguage(LANGUAGE, "en").toString()

            val deviceToken = prefs?.getFcmToken()!!.toString()
            Log.e("getDeviceTokenPrefrence", deviceToken.toString())
            if (userLoginValidation(this, binding)) {
                if (Utils.internetAvailability(this)) {
                    val hashMap = HashMap<String, String>()
                    hashMap["country_code"] = binding.ccplogin.selectedCountryCodeWithPlus.toString()
                    hashMap["phone_no"] = binding.edNumber.text.toString().trim()
                    hashMap["password"] = binding.edPass.text.toString().trim()
                    hashMap["device_type"] = "1"
                    hashMap["device_token"] = deviceToken
                    hashMap["language_type"] = lang
                    hashMap["type"] = "3"

                    authViewModel.login(hashMap)
                } else {
                    Utils.showToast(this, getString(R.string.no_internet_connection))
                }
            }
        }
    }

    private fun viewModelSetupAndResponse() {

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        authViewModel.getError().observe(this) {
            Utils.showToast(this, it)
        }
        authViewModel.progressDialogData().observe(this) { isShowProgress ->
            if (isShowProgress) {
                progressDialog.show(this)
            } else {
                progressDialog.hide()
            }
        }
        authViewModel.onShowErrorCode().observe(this) {
            when (it) {
                ErrorType.UNAUTHORIZED -> {
                    sessionExpire()
                }

                else -> {}
            }
        }
        authViewModel.onLoginResponse().observe(this) { response ->
            response?.let {
                if (it.code == 200) {


                    prefs?.saveString("PHONE", it.body.user.phone_no)
                    prefs?.saveString("name", it.body.user.name)
                    MyApplication.prefs?.saveString("IMAGE", it.body.user.profile_pic)
                    prefs?.saveString("userId", it.body.user.id.toString())
                    MyApplication.prefs?.storeisLogin(true)
                    MyApplication.prefs?.saveString("AUTH_KEY_value", it.body.user.token.toString())
                    initializeSockets()
                    lifecycleScope.launch {
                        delay(500)
                        startActivity(
                            Intent(
                                this@LoginActivity,
                                HomeActivity::class.java
                            )
                        )
                    }



                }
            }
        }
    }

    private lateinit var socketManager: SocketManager

    private fun initializeSockets() {
        val application = MyApplication.mInstance
        if (application != null) {
            socketManager = application.getSocketManager()!!
            socketManager.init()

        } else {
            // Handle the case where the application instance is null
            // This should not happen in a normal scenario
            throw IllegalStateException("Application instance is null")
        }
    }
}