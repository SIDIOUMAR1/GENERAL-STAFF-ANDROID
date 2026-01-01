package com.genralstaff.auth

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.Spinner
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.genralstaff.R
import com.genralstaff.databinding.ActivityAddDrivingDetailsBinding
import com.genralstaff.home.HomeActivity
import com.genralstaff.network.ErrorType
import com.genralstaff.utils.CustomProgressDialog
import com.genralstaff.utils.ImagePickerActivityUtility
import com.genralstaff.utils.MyApplication
import com.genralstaff.utils.Utils
import com.genralstaff.utils.getTextRequestBody
import com.genralstaff.utils.prepareFilePart
import com.genralstaff.utils.sessionExpire
import com.genralstaff.viewmodel.AuthViewModel


import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class AddDrivingDetailsActivity : ImagePickerActivityUtility() {
    val vehicleList = listOf(getString(R.string.activa), getString(R.string.car),
        getString(R.string.bike)) // Sample vehicle list
    var countryCode = ""
    var phoneNumber = ""
    var imagePath = ""
    var imagePathLicense = ""
    var Name = ""
    var referralCode = ""
    var selectedItem = ""

    private lateinit var authViewModel: AuthViewModel
private val progressDialog by lazy { CustomProgressDialog() }
    lateinit var binding: ActivityAddDrivingDetailsBinding
    override fun selectedImage(imagePath: String?, code: Int?) {
        imagePathLicense = imagePath!!
        Glide.with(this).load(imagePath).placeholder(R.drawable.place_holder)
            .into(binding.rivLicense)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDrivingDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        MyApplication.prefs!!.getFirebaseToken()
        MyApplication.prefs?.getFcmToken()!!.toString()
        phoneNumber = intent.getStringExtra("phoneNumber").toString()
        countryCode = intent.getStringExtra("countryCode").toString()
        imagePath = intent.getStringExtra("imagePath").toString()
        Name = intent.getStringExtra("Name").toString()
        referralCode = intent.getStringExtra("ReferralCode").toString()
        popUp()


        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        viewModelSetupAndResponse()
        binding.btnSubmit.setOnClickListener {
            if (selectedItem.isEmpty()) {
                Utils.showErrorDialog(this, getString(R.string.please_select_vehicle_type))
            } else if (binding.edNumber.text.toString().isEmpty()) {
                Utils.showErrorDialog(this, getString(R.string.please_enter_name))
            } else if (imagePathLicense.isEmpty()) {
                Utils.showErrorDialog(this, getString(R.string.please_upload_license))
            } else {
                val deviceToken = MyApplication.prefs?.getFcmToken()!!.toString()
                Log.e("getDeviceTokenPrefrence", deviceToken.toString())
                if (Utils.internetAvailability(this)) {
                    val hashMap = HashMap<String, RequestBody>()
                    hashMap["country_code"] = countryCode.getTextRequestBody()
                    hashMap["phone_no"] = phoneNumber.getTextRequestBody()
                    hashMap["device_type"] = "1".getTextRequestBody()
                    hashMap["device_token"] = deviceToken.getTextRequestBody()
                    hashMap["name"] = Name.getTextRequestBody()
                    hashMap["vehicle_type"] = selectedItem.getTextRequestBody()
                    hashMap["plate_number"] = binding.edNumber.text.toString().getTextRequestBody()
                    hashMap["profile_pic"] = imagePath.getTextRequestBody()
                    if (referralCode.toString().isNotEmpty()) {
                        hashMap["referral_code"] =
                            referralCode.toString().trim().getTextRequestBody()
                    }
                    hashMap["type"] = "2".getTextRequestBody()
                    val imagePart: MultipartBody.Part =
                        prepareFilePart("profile_pic", File(imagePath))
                    val imagePathLicense: MultipartBody.Part =
                        prepareFilePart("document", File(imagePathLicense))

//                    1=>user,2=>driver
                    if (imagePath.isEmpty()) {
                        authViewModel.signup(hashMap, imagePathLicense)

                    } else {
                        authViewModel.signup(hashMap, imagePart, imagePathLicense)
                    }
                } else {
                    Utils.showToast(this, getString(R.string.no_internet_connection))
                }
            }
        }
        binding.rlPick.setOnClickListener {
            getImage(this, 0)
        }
        binding.ivPick.setOnClickListener {
            getImage(this, 0)
        }
    }

    private fun popUp() {
        val spinner = findViewById<Spinner>(R.id.vehicleSpinner)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, vehicleList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedItem = parent?.getItemAtPosition(position) as String

                // Do something with the selected item
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
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
        authViewModel.onSignUp().observe(this) { response ->
            response?.let {
                if (it.code == 200) {
                    if (it.body.user.is_approve == 1) {
                        MyApplication.prefs?.storeisLogin(true)
                        MyApplication.prefs?.saveString(
                            "is_notification",
                            it.body.user.notification_status.toString()
                        )

                        MyApplication.prefs?.saveString("EMAIL", it.body.user.email)
                        MyApplication.prefs?.saveString("PHONE", it.body.user.phone_no)
                        MyApplication.prefs?.saveString("name", it.body.user.name)
                        MyApplication.prefs?.saveString("IMAGE", it.body.user.profile_pic)
                        MyApplication.prefs?.saveString("AUTH_KEY_value", it.body.user.token)
                        MyApplication.prefs?.saveString("userId", it.body.user.id.toString())
                        Log.e(
                            "AUTH_KEY_value",
                            MyApplication.prefs?.getString("AUTH_KEY_value").toString()
                        )

                        val intent = Intent(this, HomeActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        overridePendingTransition(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                        )
                        startActivity(intent)

                    }


                }
            }
        }
    }

}