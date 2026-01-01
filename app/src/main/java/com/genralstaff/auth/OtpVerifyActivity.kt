package com.genralstaff.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.genralstaff.R
import com.genralstaff.databinding.ActivityOtpVerifyBinding
import com.genralstaff.home.HomeActivity
import com.genralstaff.network.ErrorType
import com.genralstaff.sockets.SocketManager
import com.genralstaff.utils.CustomProgressDialog
import com.genralstaff.utils.MyApplication
import com.genralstaff.utils.MyApplication.Companion.prefs
import com.genralstaff.utils.Utils
import com.genralstaff.utils.getCity
import com.genralstaff.utils.sessionExpire
import com.genralstaff.viewmodel.AuthViewModel
import com.genraluser.utils.LocationUpdateUtilityActivity
import com.google.android.material.internal.ViewUtils.hideKeyboard

import `in`.aabhasjindal.otptextview.OTPListener

class OtpVerifyActivity : LocationUpdateUtilityActivity() {
    var otp = ""

    var countryCode = ""
    var phoneNumber = ""
    var auth = ""
    var isApproved = ""
    private lateinit var authViewModel: AuthViewModel
private val progressDialog by lazy { CustomProgressDialog() }
    lateinit var binding: ActivityOtpVerifyBinding
    override fun updatedLatLng(lat: Double, lng: Double) {
        prefs!!.saveString("lat",lat.toString())
        prefs!!.saveString("lng",lng.toString())
        prefs!!.saveString("address",getCity(lat.toString(),lng.toString(),this))


    }

    override fun onChangedLocation(lat: Double, lng: Double) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpVerifyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getLiveLocation(this)
//        if (intent.getStringExtra("auth") != null) {
//            auth = intent.getStringExtra("auth").toString()
//        }
        prefs!!.getFirebaseToken()
        prefs?.getFcmToken()!!.toString()
//        otp = intent.getStringExtra("otp").toString()
//        phoneNumber = intent.getStringExtra("phoneNumber").toString()
//        countryCode = intent.getStringExtra("countryCode").toString()
//        isApproved = intent.getStringExtra("isApproved").toString()
        binding.tvPhone.text = countryCode + phoneNumber

        viewModelSetupAndResponse()

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.tvResend.setOnClickListener {
//            val deviceToken = prefs?.getFcmToken()!!.toString()
//            Log.e("getDeviceTokenPrefrence", deviceToken.toString())
//            if (Utils.internetAvailability(this)) {
//                val hashMap = HashMap<String, String>()
//                hashMap["country_code"] =
//                    countryCode
//                hashMap["phone_no"] = phoneNumber
//                hashMap["device_type"] = "1"
//                hashMap["device_token"] = deviceToken
//                hashMap["user_type"] = "2"
//
//                authViewModel.login(hashMap)
//            } else {
//                Utils.showToast(this, getString(R.string.no_internet_connection))
//            }

        }
        binding.ivEdit.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnSubmit.setOnClickListener {
            startActivity(
                        Intent(this, HomeActivity::class.java)

                    )

//            if (binding.otpPin.otp.toString().trim().isEmpty()) {
//                Utils.showErrorDialog(this, "Please enter OTP")
//
//            } else if (otp != binding.otpPin.otp.toString().trim()) {
//                Utils.showErrorDialog(this, "Please enter correct OTP")
//
//            } else {
//                if (isApproved == "1") {
//                    MyApplication.prefs?.storeisLogin(true)
//                    MyApplication.prefs?.saveString("AUTH_KEY_value", auth)
//                    initializeSockets()
//                    startActivity(
//                        Intent(this, HomeActivity::class.java)
//                    )
//                } else {
//                    startActivity(
//                        Intent(this, CompleteProfileActivity::class.java)
//                            .putExtra("otp", otp)
//                            .putExtra("countryCode", countryCode)
//                            .putExtra("phoneNumber", phoneNumber)
//                    )
//                }
//            }

        }
        binding.otpPin.otpListener = object : OTPListener {
            override fun onInteractionListener() {

            }

            @SuppressLint("RestrictedApi")
            override fun onOTPComplete(otp: String) {
                // Perform your action when OTP is filled
                // For example, you can hide the keyboard
                // Check if the OTP is complete
                if (otp?.length == 4) {
                    // Hide the keyboard
                    hideKeyboard(binding.otpPin)
                }
                // You can also validate the OTP or perform any other action here
            }


        }
//        // Set up a TextWatcher for the PinView
//        binding.otpPin.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//
//            @SuppressLint("RestrictedApi")
//            override fun afterTextChanged(s: Editable?) {
//                // Check if the OTP is complete
//                if (s?.length == 4) {
//                    // Hide the keyboard
//                    hideKeyboard(binding.otpPin)
//                }
//            }
//        })
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
                    Utils.showErrorDialog(this,
                        getString(R.string.otp_send_successfully_please_enter_1111_as_otp))
//                    otp = it.body.otp.otp.toString()

                }
            }
        }
    }

}