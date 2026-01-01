package com.genralstaff.home.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.genralstaff.R
import com.genralstaff.auth.LoginActivity
import com.genralstaff.auth.SplashActivity
import com.genralstaff.base.LANGUAGE
import com.genralstaff.databinding.ActivityChooseLanguageBinding
import com.genralstaff.home.HomeActivity
import com.genralstaff.network.ErrorType
import com.genralstaff.utils.CustomProgressDialog
import com.genralstaff.utils.MyApplication.Companion.prefs
import com.genralstaff.utils.Utils
import com.genralstaff.utils.changeLanguage
import com.genralstaff.utils.getTextRequestBody
import com.genralstaff.utils.sessionExpire
import com.genralstaff.viewmodel.AuthViewModel

import okhttp3.RequestBody
import java.util.HashMap

class ChooseLanguageActivity : AppCompatActivity() {

    var status = 0
    var type = ""
    private lateinit var authViewModel: AuthViewModel
private val progressDialog by lazy { CustomProgressDialog() }
    lateinit var binding: ActivityChooseLanguageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        type = intent.getStringExtra("type").toString()
        val lang = prefs!!.getPrefrenceLanguage(LANGUAGE, "en").toString()
        status = when (lang) {
            "en" -> {
                0
            }

            "fr" -> {
                1

            }

            else -> {
                2

            }
        }
        selectLanguageUI()
        viewModelSetupAndResponse()
        clicks()
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
                    progressDialog.hide()
                    startActivity(
                        Intent(this@ChooseLanguageActivity, HomeActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    )
                }
            }
        }

    }

    private fun clicks() {
        binding.apply {
            ivBack.setOnClickListener {
                finish()

            }
            tvEnglish.setOnClickListener {
                status = 0
                selectLanguageUI()
            }
            tvFrench.setOnClickListener {
                status = 1
                selectLanguageUI()
            }
            tvArabic.setOnClickListener {
                status = 2
                selectLanguageUI()
            }
            tvSave.setOnClickListener {
                progressBar.visibility = View.VISIBLE
                var language = ""
                language = when (status) {
                    0 -> {
                        "en"
                    }

                    1 -> {
                        "fr"
                    }

                    else -> {
                        "ar"
                    }
                }
                prefs!!.savePrefrenceLanguage(LANGUAGE, language)
                changeLanguage(this@ChooseLanguageActivity)
                (this@ChooseLanguageActivity)
                if (type == "login") {

                    startActivity(
                        Intent(this@ChooseLanguageActivity, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    )
                } else if (type == "splash") {
                    prefs!!.savePrefrenceLanguage("LANGUAGE_save", "1")

//                    val intent = Intent(this@ChooseLanguageActivity, ::class.java)
                    val intent = Intent(this@ChooseLanguageActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish() // Ensures SplashActivity is removed
                } else {
                    prefs!!.savePrefrenceLanguage(LANGUAGE, language)
                    val hashMap = HashMap<String, RequestBody>()
                    hashMap["language_type"] = language.getTextRequestBody()
                    authViewModel.editProfileWithoutImage(hashMap)

//                    startActivity(
//                        Intent(this@ChooseLanguageActivity, HomeActivity::class.java).apply {
//                            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
//                        }
//                    )
//                    finish()
                }

                progressBar.visibility = View.GONE

            }
        }
    }

    private fun selectLanguageUI() {
        binding.apply {
            if (type == "splash") {
                ivBack.visibility = View.INVISIBLE
            }
            when (status) {
                0 -> {
                    tvEnglish.setBackgroundResource(R.drawable.button_back)
                    tvFrench.setBackgroundResource(R.drawable.round_corner_stroke_back_black)
                    tvArabic.setBackgroundResource(R.drawable.round_corner_stroke_back_black)

                }

                1 -> {
                    tvEnglish.setBackgroundResource(R.drawable.round_corner_stroke_back_black)
                    tvFrench.setBackgroundResource(R.drawable.button_back)
                    tvArabic.setBackgroundResource(R.drawable.round_corner_stroke_back_black)

                }

                2 -> {
                    tvEnglish.setBackgroundResource(R.drawable.round_corner_stroke_back_black)
                    tvFrench.setBackgroundResource(R.drawable.round_corner_stroke_back_black)
                    tvArabic.setBackgroundResource(R.drawable.button_back)

                }

                else -> {

                }
            }
        }
    }
}