package com.genralstaff.home.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.genralstaff.R
import com.genralstaff.adapter.CategoriesAdapter
import com.genralstaff.databinding.ActivityCategoriesBinding
import com.genralstaff.home.AddProductActivity
import com.genralstaff.network.ErrorType
import com.genralstaff.responseModel.CategoriesListResponse
import com.genralstaff.utils.CustomProgressDialog
import com.genralstaff.utils.ItemMoveCallback
import com.genralstaff.utils.Utils
import com.genralstaff.utils.Validator
import com.genralstaff.utils.sessionExpire
import com.genralstaff.viewmodel.AuthViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import org.json.JSONArray
import org.json.JSONObject

class CategoriesActivity : AppCompatActivity() {
    var id = ""
    private lateinit var authViewModel: AuthViewModel
private val progressDialog by lazy { CustomProgressDialog() }
    var categoriesListResponse = ArrayList<CategoriesListResponse.Body>()

    var adapter: CategoriesAdapter? = null
    lateinit var binding: ActivityCategoriesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        id = intent.getStringExtra("id").toString()
        clicks()

    }

    override fun onResume() {
        super.onResume()
        viewModelSetupAndResponse()

        authViewModel.get_types(id)
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
        authViewModel.onCategoriesListResponse().observe(this) { response ->
            response?.let {
                if (it.code == 200) {
                    categoriesListResponse.clear()
                    categoriesListResponse.addAll(it.body)
                    if (categoriesListResponse.isEmpty()) {
                        binding.rvCategories.visibility = View.GONE
                        binding.llNoRequest.visibility = View.VISIBLE
                    } else {
                        binding.rvCategories.visibility = View.VISIBLE

                        binding.llNoRequest.visibility = View.GONE

                    }

                    adapter = CategoriesAdapter(this, categoriesListResponse)
                    binding.rvCategories.adapter = adapter
                    adapter?.onItemClickListener = { pos, type ->
                        if (type == "edit") {
                            startActivity(Intent(
                                this@CategoriesActivity,
                                EditCategoryActivity::class.java
                            )
                                .apply {
                                    putExtra("id", categoriesListResponse[pos].id.toString())
                                    putExtra(
                                        "name",
                                        categoriesListResponse[pos].name.toString()
                                    )
                                    putExtra(
                                        "name_ar",
                                        categoriesListResponse[pos].name_ar.toString()
                                    )
                                    putExtra(
                                        "name_fr",
                                        categoriesListResponse[pos].name_fr.toString()
                                    )
                                }
                            )
                        } else {
                            authViewModel.delete_type(categoriesListResponse[pos].id.toString())
                        }
                    }
                    binding.tvSave.setOnClickListener {
                        val typesArray = JSONArray()

                        categoriesListResponse.forEachIndexed { index, item ->
                            val jsonObject = JSONObject()
                            jsonObject.put("type_id", item.id)
                            jsonObject.put("sort_order", index)
                            typesArray.put(jsonObject)
                        }

                        val map = HashMap<String, String>()
                        map["types_arr"] = typesArray.toString()
                        Log.e("viewModelSetupAndResponse: ", map.toString())
                        authViewModel.shuffle(map)
                    }


                    val callback = ItemMoveCallback(adapter!!)
                    val touchHelper = ItemTouchHelper(callback)
                    touchHelper.attachToRecyclerView(binding.rvCategories)

                    // Set drag listener to adapter
                    adapter?.dragStartListener = object : CategoriesAdapter.OnStartDragListener {
                        override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
                            touchHelper.startDrag(viewHolder)
//                            binding.tvSave.visibility=View.VISIBLE
                            val typesArray = JSONArray()

                            categoriesListResponse.forEachIndexed { index, item ->
                                val jsonObject = JSONObject()
                                jsonObject.put("type_id", item.id)
                                jsonObject.put("sort_order", index)
                                typesArray.put(jsonObject)
                            }

                            val map = HashMap<String, String>()
                            map["types_arr"] = typesArray.toString()
                            Log.e("viewModelSetupAndResponse: ", map.toString())
                            authViewModel.shuffle(map)
                        }
                    }

                }
            }
        }
        authViewModel.onContentsResponse().observe(this) { response ->
            response?.let {
                if (it.code == 200) {
                    authViewModel.get_types(id)
                }
            }
        }
        authViewModel.onCommonResponse().observe(this) { response ->
            response?.let {
                if (it.code == 200) {
                    authViewModel.get_types(id)
                }
            }
        }
        authViewModel.onLoginResponse().observe(this) { response ->
            response?.let {
                if (it.code == 200) {
                    authViewModel.get_types(id)
                }
            }
        }
    }

    fun showCategoryDialog(context: Context, onAddClicked: (String, String, String) -> Unit) {
        val editText = EditText(context).apply {
            hint = context.getString(R.string.enter_category_name)
            textSize = 16f
            setPadding(40, 40, 40, 40) // Adds padding for better spacing
        }
        val editTextArabic = EditText(context).apply {
            hint = context.getString(R.string.enter_category_name)
            textSize = 16f
            setPadding(40, 40, 40, 40) // Adds padding for better spacing
        }
        val editTextFrench = EditText(context).apply {
            hint = context.getString(R.string.enter_category_name)
            textSize = 16f
            setPadding(40, 40, 40, 40) // Adds padding for better spacing
        }

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 10, 50, 10)
            addView(editText)
            addView(editTextArabic)
            addView(editTextFrench)
        }

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.category_name))
            .setMessage(context.getString(R.string.enter_a_new_category))
            .setView(layout)
            .setPositiveButton(
                context.getString(R.string.add),
                null
            ) // Override auto-dismiss behavior
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
            .setCancelable(true)
            .create()

        dialog.show() // Show the dialog

        // Manually handle the positive button click to prevent dismissing on validation failure
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val enteredText = editText.text.toString().trim()
            val enteredTextArabic = editTextArabic.text.toString().trim()
            val enteredTextFrench = editTextFrench.text.toString().trim()

            if (enteredText.isEmpty()) {
                Utils.showErrorDialog(
                    context,
                    context.getString(R.string.please_enter_a_valid_category_name)
                )
                return@setOnClickListener // Prevents dismissing the dialog
            }
//            if (enteredTextArabic.isEmpty()) {
//                Utils.showErrorDialog(context, context.getString(R.string.please_enter_shop_name_in_arabic))
//                return@setOnClickListener
//            }
//            if (enteredTextFrench.isEmpty()) {
//                Utils.showErrorDialog(context, context.getString(R.string.please_enter_shop_name_in_french))
//                return@setOnClickListener
//            }

            // Dismiss dialog only when all fields are valid
            dialog.dismiss()
            onAddClicked(enteredText, enteredTextArabic, enteredTextFrench)
        }
    }


    private fun clicks() {
        binding.apply {
            ivBack.setOnClickListener {
                finish()
            }
            ivAddCategories.setOnClickListener {
                // Call this function wherever needed (like a button click)
                showCategoryDialog(this@CategoriesActivity) { categoryName, categoryNameArbi, categoryFrench ->
                    // Handle the entered category name here
                    if (Utils.internetAvailability(this@CategoriesActivity)) {
                        val hashMap = HashMap<String, String>()
                        hashMap["shop_id"] = id.toString()
                        hashMap["name"] = categoryName
                        hashMap["name_ar"] = categoryNameArbi
                        hashMap["name_fr"] = categoryFrench


                        authViewModel.add_category(hashMap)
                    } else {
                        Utils.showToast(
                            this@CategoriesActivity,
                            getString(R.string.no_internet_connection)
                        )
                    }

                }
            }
            btnNext.setOnClickListener {
                var id_cat = ""
                for (i in 0 until categoriesListResponse.size) {
                    if (categoriesListResponse[i].isselect) {
                        id_cat = categoriesListResponse[i].id.toString()
                    }
                }
                if (id_cat == "") {
                    Utils.showErrorDialog(
                        this@CategoriesActivity,
                        getString(R.string.please_select_category)
                    )
                } else {


                    startActivity(
                        Intent(this@CategoriesActivity, AddProductActivity::class.java)
                            .putExtra("id", id.toString())
                            .putExtra("id_cat", id_cat.toString())
                            .putExtra("type", "add")
                    )
                }
            }

        }
    }

}