package com.genralstaff.home.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.genralstaff.adapter.OrderItemsAdapter
import com.genralstaff.adapter.ShopsAdapter
import com.genralstaff.databinding.ActivityShopsBinding
import com.genralstaff.network.ErrorType
import com.genralstaff.responseModel.GetShopsResponse
import com.genralstaff.responseModel.OrderHistoryResponse
import com.genralstaff.utils.CustomProgressDialog
import com.genralstaff.utils.Utils
import com.genralstaff.utils.getTextRequestBody
import com.genralstaff.utils.prepareFilePart
import com.genralstaff.utils.sessionExpire
import com.genralstaff.viewmodel.AuthViewModel

import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class ShopsActivity : AppCompatActivity() {
    var limit = 100
    var page = 1
    var ordersList = ArrayList<GetShopsResponse.Body.Data>()
    var type = "0"
    private lateinit var authViewModel: AuthViewModel
private val progressDialog by lazy { CustomProgressDialog() }
    lateinit var binding: ActivityShopsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.ivAddShop.setOnClickListener {
            startActivity(
                Intent(this, AddShopActivity::class.java)
                    .putExtra("type", "add")

            )
        }
    }

    private fun callOrdersApi() {
        val map = HashMap<String, String>()

        map["page"] = page.toString()
        map["limit"] = limit.toString()

        authViewModel.shops(map)
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
        authViewModel.onGetShopsResponse().observe(this) { response ->
            response?.let {
                if (it.code == 200) {
                    ordersList.clear()
                    ordersList.addAll(it.body.data)
                    if (ordersList.isEmpty()) {
                        binding.llNoNewRequest.visibility = View.VISIBLE
                        binding.rvShops.visibility = View.GONE
                    } else {
                        binding.llNoNewRequest.visibility = View.GONE
                        binding.rvShops.visibility = View.VISIBLE
                        val adapter = ShopsAdapter(this, ordersList, ordersList)
                        binding.rvShops.adapter = adapter

                        adapter.onItemClickListener = { pos, type, id ->
                            val hashMap = java.util.HashMap<String, RequestBody>()
                            hashMap["status"] = type.getTextRequestBody()
                            hashMap["update_id"] = id.getTextRequestBody()

                            authViewModel.editShop(hashMap)

                        }


                        binding.edSearch.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                            }

                            override fun afterTextChanged(s: Editable) {
                                try {
                                    if (adapter != null) {
                                        adapter!!.filter(s.toString().trim(), binding)
                                    }
                                } catch (e: java.lang.Exception) {
                                }
                            }
                        })
                    }

                }
            }
        }
        callOrdersApi()
    }

}