package com.genralstaff.auth

import android.content.Intent
import android.os.Bundle
import com.bumptech.glide.Glide
import com.genralstaff.R
import com.genralstaff.databinding.ActivityCompleteProfileBinding
import com.genralstaff.utils.ImagePickerActivityUtility
import com.genralstaff.utils.Utils

class CompleteProfileActivity : ImagePickerActivityUtility() {
    var countryCode = ""
    var phoneNumber = ""
    var imagePath = ""

    lateinit var binding: ActivityCompleteProfileBinding
    override fun selectedImage(imagePath: String?, code: Int?) {
        this.imagePath=imagePath!!
        Glide.with(this).load(imagePath).placeholder(R.drawable.place_holder)
            .into(binding.civProfile)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompleteProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        phoneNumber = intent.getStringExtra("phoneNumber").toString()
        countryCode = intent.getStringExtra("countryCode").toString()
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.civProfile.setOnClickListener {
            getImage(this, 0)
        }
        binding.ivPick.setOnClickListener {
            getImage(this, 0)
        }
        binding.btnSubmit.setOnClickListener {

//            if (imagePath.isEmpty()) {
//                Utils.showErrorDialog(this, "Please select image")
//            } else
                if (binding.edName.text.toString().isEmpty()) {
                Utils.showErrorDialog(this, getString(R.string.please_enter_name))
            }
            else{
            startActivity(Intent(this,AddDrivingDetailsActivity::class.java)
                .putExtra("countryCode",countryCode)
                .putExtra("phoneNumber",phoneNumber)
                .putExtra("imagePath",imagePath)
                .putExtra("Name",binding.edName.text.toString().trim())
                .putExtra("ReferralCode",binding.edReferralCode.text.toString().trim())
            )
        }}
    }


}