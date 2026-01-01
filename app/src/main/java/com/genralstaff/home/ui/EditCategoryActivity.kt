package com.genralstaff.home.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.genralstaff.R
import com.genralstaff.adapter.CategoriesAdapter
import com.genralstaff.databinding.ActivityEditCategoryBinding
import com.genralstaff.network.ErrorType
import com.genralstaff.utils.CustomProgressDialog
import com.genralstaff.utils.ItemMoveCallback
import com.genralstaff.utils.Utils
import com.genralstaff.utils.sessionExpire
import com.genralstaff.viewmodel.AuthViewModel

import org.json.JSONArray
import org.json.JSONObject

class EditCategoryActivity : AppCompatActivity() {
    var id = ""
    var name = ""
    var name_ar = ""
    var name_fr = ""
    private lateinit var authViewModel: AuthViewModel
private val progressDialog by lazy { CustomProgressDialog() }
    lateinit var binding: ActivityEditCategoryBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModelSetupAndResponse()
        id = intent.getStringExtra("id").toString()
        name = intent.getStringExtra("name").toString()
        name_ar = intent.getStringExtra("name_ar").toString()
        name_fr = intent.getStringExtra("name_fr").toString()
        binding.apply {
            edName.setText(name)
            edArabicName.setText(name_ar)
            edFrenchName.setText(name_fr)
            ivBack.setOnClickListener {
                finish()
            }
            btnSubmit.setOnClickListener {
                val name = edName.text.toString().trim()
                val nameAr = edArabicName.text.toString().trim()
                val nameFr = edFrenchName.text.toString().trim()

                authViewModel.edit_type(id, name, nameAr, nameFr)
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
        authViewModel.onContentsResponse().observe(this) { response ->
            response?.let {
                if (it.code == 200) {
                    finish()

                }
            }
        }

    }

}