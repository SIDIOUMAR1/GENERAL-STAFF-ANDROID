package com.genralstaff.home.ui

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.genralstaff.R
import com.genralstaff.base.imageURL
import com.genralstaff.base.profileBaseUrl
import com.genralstaff.databinding.ActivityOrderDetailBinding
import com.genralstaff.home.HomeActivity
import com.genralstaff.network.ErrorType
import com.genralstaff.utils.CustomProgressDialog
import com.genralstaff.utils.MyApplication
import com.genralstaff.utils.Utils
import com.genralstaff.utils.printDate
import com.genralstaff.utils.sessionExpire
import com.genralstaff.viewmodel.AuthViewModel
import com.google.android.gms.maps.model.LatLng

import com.rygelouv.audiosensei.player.AudioSenseiListObserver
import org.json.JSONObject

class OrderDetailActivity : AppCompatActivity() {
    var id = ""
    lateinit var binding: ActivityOrderDetailBinding
    private lateinit var authViewModel: AuthViewModel
private val progressDialog by lazy { CustomProgressDialog() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        id = intent.getStringExtra("id").toString()
        viewModelSetupAndResponse()
        binding.ivBack.setOnClickListener {
            finish()
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
        authViewModel.onOrderDetailResponse().observe(this) { response ->
            response?.let {
                if (it.code == 200) {

                    binding.tvProductName.text = it.body.product.name
                    binding.tvShopName.text = it.body.shop.name
                    binding.tvShopLocation.text = it.body.shop.location
                    binding.tvLocation.text = it.body.location
                    binding.edDescription.text = it.body.description
                    binding.tvSelectDriverType.text = it.body.driver_type
                    binding.edFee.text = it.body.delivery_charge
                    if (it.body.audio.isNotEmpty() || it.body.audio != null) {

//         Register lifecycle to the AudioSenseiListObserver
                        AudioSenseiListObserver.getInstance()
                            .registerLifecycle(this@OrderDetailActivity.lifecycle)
                        binding.apply {
                            tvAudio.visibility = View.VISIBLE
                            rlAudio.visibility = View.VISIBLE
                            val audio_url = profileBaseUrl + it.body.audio
                            binding.rightAudioPlayer.setAudioTarget(audio_url)
                        }
                    }


                }
            }
        }

        val map = HashMap<String, String>()
        map["order_id"] = id
        authViewModel.orderDetail(map)
    }


}