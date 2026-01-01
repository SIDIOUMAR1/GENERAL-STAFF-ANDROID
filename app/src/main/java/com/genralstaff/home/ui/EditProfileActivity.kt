package com.genralstaff.home.ui

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.genralstaff.R
import com.genralstaff.base.imageURL
import com.genralstaff.base.profileBaseUrl
import com.genralstaff.databinding.ActivityEditProfileBinding
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
import java.util.HashMap

class EditProfileActivity : ImagePickerActivityUtility() {
    lateinit var binding: ActivityEditProfileBinding
    override fun selectedImage(imagePath: String?, code: Int?) {
        this.imagePath = imagePath!!

        Glide.with(this).load(imagePath).placeholder(R.drawable.place_holder)
            .into(binding.civProfile)
    }

    private lateinit var authViewModel: AuthViewModel
private val progressDialog by lazy { CustomProgressDialog() }
    var name = ""
    var image = ""
    var phone = ""
    var imagePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        name = intent.getStringExtra("name").toString()
        image = intent.getStringExtra("image").toString()
        phone = intent.getStringExtra("phone").toString()
        binding.edNumber.setText(phone)
        binding.edName.setText(name)
        Glide.with(this).load(imageURL + image)
            .placeholder(R.drawable.place_holder).into(binding.civProfile)
        binding.civProfile.setOnClickListener {
            getImage(this, 0)
        }
        binding.ivPick.setOnClickListener {
            getImage(this, 0)
        }
        binding.btnSubmit?.setOnClickListener {
//            finish()

            if (binding.edName.text.toString().isEmpty()

            ) {
                Utils.showErrorDialog(this, getString(R.string.please_enter_name))

            } else {
                if (Utils.internetAvailability(this@EditProfileActivity)) {

                    val hashMap = HashMap<String, RequestBody>()
                    hashMap["name"] = binding.edName.text.toString().trim().getTextRequestBody()
                    hashMap["phone_no"] = binding.edNumber.text.toString().trim().getTextRequestBody()
                    hashMap["country_code"] =
                        binding.ccplogin.selectedCountryCodeWithPlus.toString().trim().getTextRequestBody()
                    if (imagePath.isNotEmpty()) {
                        val imagePart: MultipartBody.Part =
                            prepareFilePart("profile_pic", File(imagePath!!))
                        authViewModel.editProfile(hashMap, imagePart)
                    } else {
                        authViewModel.editProfileWithoutImage(hashMap)

                    }

                } else {
                    Utils.showToast(
                        this@EditProfileActivity,
                        getString(R.string.no_internet_connection)
                    )
                }
            }
        }

        binding.ivBack.setOnClickListener {
            finish()
        }
        viewModelSetupAndResponse()
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
        authViewModel.onEditProfileResponse().observe(this) { response ->
            response?.let {
                if (it.code == 200) {
                    MyApplication.prefs?.saveString("name", it.body.name)
                    MyApplication.prefs?.saveString("IMAGE", it.body.profile_pic)
                    finish()
                }
            }


        }
    }

}