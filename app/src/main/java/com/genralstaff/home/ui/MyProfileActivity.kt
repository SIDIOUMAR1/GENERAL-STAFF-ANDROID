package com.genralstaff.home.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.genralstaff.R
import com.genralstaff.base.imageURL
import com.genralstaff.base.profileBaseUrl
import com.genralstaff.databinding.ActivityMyProfileBinding
import com.genralstaff.network.ErrorType
import com.genralstaff.utils.CustomProgressDialog
import com.genralstaff.utils.Utils
import com.genralstaff.utils.sessionExpire
import com.genralstaff.viewmodel.AuthViewModel


class MyProfileActivity : AppCompatActivity() {
    private lateinit var authViewModel: AuthViewModel
private val progressDialog by lazy { CustomProgressDialog() }
    lateinit var binding: ActivityMyProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnEdit.setOnClickListener {
            startActivity(
                Intent(this, EditProfileActivity::class.java)
            )
        }
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModelSetupAndResponse()

    }

    private fun viewModelSetupAndResponse() {

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        authViewModel.getError().observe(this) {
            Utils.showToast(this, it)
        }
        authViewModel.progressDialogData().observe(this) { isShowProgress ->
            if (isShowProgress) {
//                progressDialog.show(this)
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
        authViewModel.onProfileResponse().observe(this) { response ->
            response?.let {
                if (it.code == 200) {

                    binding.edName.text = it.body.name
                    binding.tvPhone.text =  it.body.phone_no
                    Glide.with(this).load(imageURL + it.body.profile_pic)
                        .placeholder(R.drawable.place_holder).into(binding.civProfile)
                    var image = ""
                    if (it.body.profile_pic != null) {
                        image = it.body.profile_pic
                    }
                    binding.btnEdit.setOnClickListener {
                        startActivity(
                            Intent(this, EditProfileActivity::class.java)
                                .putExtra("name", binding.edName.text.toString().trim())
                                .putExtra("image", image)
                                .putExtra("phone", binding.tvPhone.text.toString())
                        )
                    }

                }
            }
        }
        authViewModel.profile()
    }


}