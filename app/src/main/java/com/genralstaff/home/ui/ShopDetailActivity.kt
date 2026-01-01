package com.genralstaff.home.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.genralstaff.R
import com.genralstaff.adapter.CategoriesItemsAdapter
import com.genralstaff.adapter.ProductsAdapter
import com.genralstaff.base.imageURL
import com.genralstaff.databinding.ActivityShopDetailBinding
import com.genralstaff.home.AddProductActivity
import com.genralstaff.network.ErrorType
import com.genralstaff.responseModel.CategoriesResponseNew
import com.genralstaff.responseModel.ShopItemsResponse
import com.genralstaff.utils.CustomProgressDialog
import com.genralstaff.utils.Utils
import com.genralstaff.utils.sessionExpire
import com.genralstaff.viewmodel.AuthViewModel


class ShopDetailActivity : AppCompatActivity() {
    var id = ""
    var name = ""
    var image = ""
    var location = ""
    var limit = 100
    var page = 1
    private lateinit var authViewModel: AuthViewModel
private val progressDialog by lazy { CustomProgressDialog() }
    lateinit var binding: ActivityShopDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        id = intent.getStringExtra("id").toString()
        name = intent.getStringExtra("name").toString()
        image = intent.getStringExtra("image").toString()
        location = intent.getStringExtra("location").toString()
        viewModelSetupAndResponse()

        Glide.with(this).load(imageURL + image).placeholder(
            R.drawable.place_holder
        ).into(binding.rivRestaurantPic)
        binding.tvName.text = name
        binding.tvDateTime.text = location

        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.ivAddProduct.setOnClickListener {
            startActivity(
                Intent(this, CategoriesActivity::class.java)
                    .putExtra("id", id.toString())
            )
//            startActivity(
//                Intent(this, AddProductActivity::class.java)
//                    .putExtra("id", id.toString())
//                    .putExtra("type", "add")
//            )
        }
    }

    override fun onResume() {
        super.onResume()
        callShopsApi()
    }

    var shopItems = ArrayList<ShopItemsResponse.Body.Data>()
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
        authViewModel.onCategoriesResponseNew().observe(this@ShopDetailActivity) { response ->
            response?.let {
                if (it.code == 200) {
                    categoriesList.clear()
                    categoriesList.addAll(it.body)
                    categoriesList.firstOrNull()?.let { firstCategory ->
                        firstCategory.isSelected = true
                        categoryId = firstCategory.id.toString()
                    }
                    if (categoriesList.isNotEmpty()) {
                        callShopsItemsApi()
                    }
                    binding.rvCategories.adapter =
                        CategoriesItemsAdapter(this@ShopDetailActivity, categoriesList).apply {
                            onItemClickListener = { pos ->
                                categoryId = categoriesList[pos].id.toString()
                                callShopsItemsApi()
                            }
                        }
                }
            }
        }

        authViewModel.onShopItemsResponse().observe(this) { response ->
            response?.let {
                if (it.code == 200) {
                    shopItems.clear()
                    shopItems.addAll(it.body.data)
                    if (shopItems.isEmpty()) {
                        binding.llNoNewRequest.visibility = View.VISIBLE
                        binding.rvProducts.visibility = View.GONE
                    } else {
                        binding.llNoNewRequest.visibility = View.GONE
                        binding.rvProducts.visibility = View.VISIBLE
                        val adapter = ProductsAdapter(this, shopItems)
                        binding.rvProducts.adapter = adapter
                        adapter.onItemClickListener = { pos ->
                            AlertDialog.Builder(this@ShopDetailActivity)
                                .setTitle(getString(R.string.delete))
                                .setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_product))
                                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                                    authViewModel.delete_product(shopItems[pos].id.toString())
                                    shopItems.removeAt(pos)
                                    adapter.notifyDataSetChanged()
                                }
                                .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .create()
                                .show()
                        }

                    }
                }
            }
        }
    }

    private var categoriesList = ArrayList<CategoriesResponseNew.Body>()
    private var categoryId = ""
    private fun callShopsItemsApi() {
        authViewModel.shopsItems(
            hashMapOf(
                "shop_id" to id,
                "page" to page.toString(),
                "limit" to limit.toString(),
                "type_id" to categoryId
            )
        )
    }

    private fun callShopsApi() {
        authViewModel.categoriesQuery(id)

//        val map = HashMap<String, String>()
//
//        map["shop_id"] = id.toString()
//        map["page"] = page.toString()
//        map["limit"] = limit.toString()
//
//        authViewModel.shopItems(map)
    }

}