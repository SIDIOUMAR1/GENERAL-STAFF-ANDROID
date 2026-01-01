package com.genralstaff.home.ui
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.parseAsHtml
import androidx.lifecycle.ViewModelProvider
import com.genralstaff.R
import com.genralstaff.databinding.ActivityContentBinding
import com.genralstaff.network.ErrorType
import com.genralstaff.utils.CustomProgressDialog
import com.genralstaff.utils.Utils
import com.genralstaff.utils.sessionExpire
import com.genralstaff.viewmodel.AuthViewModel


class ContentActivity : AppCompatActivity() {
    lateinit var binding: ActivityContentBinding
    var type = ""
    private lateinit var authViewModel: AuthViewModel
private val progressDialog by lazy { CustomProgressDialog() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModelSetupAndResponse()

        type = intent.getStringExtra("type").toString()
        when (type) {
            "help" -> {
                authViewModel.content("4")
                binding.tvTitle.text = getString(R.string.help_support)

            }
            "privacy" -> {
                authViewModel.content("1")

                binding.tvTitle.text = getString(R.string.privacy_policy)

            }
            else -> {
                authViewModel.content("3")

                binding.tvTitle.text = getString(R.string.about)

            }
        }

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
        authViewModel.onContentsResponse().observe(this) { response ->
            response?.let {
                if (it.code == 200) {


                    binding.termsOfSue.text = it.body.description.parseAsHtml()


                }
            }
        }

    }

}