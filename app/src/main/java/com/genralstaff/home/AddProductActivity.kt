package com.genralstaff.home

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.genralstaff.R
import com.genralstaff.adapter.AddImagesAdapter
import com.genralstaff.adapter.CategoriesAdapter
import com.genralstaff.base.imageURL
import com.genralstaff.databinding.ActivityAddProductBinding
import com.genralstaff.home.ui.ShopDetailActivity
import com.genralstaff.network.ErrorType
import com.genralstaff.responseModel.CategoriesListResponse
import com.genralstaff.responseModel.CategoriesResponse
import com.genralstaff.responseModel.ShopItemsResponse
import com.genralstaff.utils.CustomProgressDialog
import com.genralstaff.utils.ImagePickerActivityUtility
import com.genralstaff.utils.Utils
import com.genralstaff.utils.getTextRequestBody
import com.genralstaff.utils.prepareFilePart
import com.genralstaff.utils.sessionExpire
import com.genralstaff.viewmodel.AuthViewModel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URL
import java.util.Calendar
import java.util.HashMap

class AddProductActivity : ImagePickerActivityUtility() {
    var id = ""
    var type = ""
    var price = ""
    var id_cat = ""
    var productId = ""
    var commaSeparatedString = ""
    var list = ArrayList<String>()
    var adapter: AddImagesAdapter? = null
    var arrListImages = java.util.ArrayList<MultipartBody.Part>()
    private lateinit var authViewModel: AuthViewModel
private val progressDialog by lazy { CustomProgressDialog() }
    var categoriesListResponse = ArrayList<CategoriesListResponse.Body>()
var listString=ArrayList<String>()
    lateinit var binding: ActivityAddProductBinding
    override fun selectedImage(imagePath: String?, code: Int?) {
        if (list[0] == "") {
            list.clear()

        }
        list.add(imagePath!!)
        adapter?.notifyDataSetChanged()
    }

    private suspend fun getBitmapFromUrl(url: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            getBitmap(url)
        }
    }

    private fun getBitmap(url: String): Bitmap? {
        val url = URL(url)
        return BitmapFactory.decodeStream(url.openConnection().getInputStream())
    }

    private fun persistImage(bitmap: Bitmap, name: String): File {
        val filesDir: File = this.filesDir
        val imageFile = File(filesDir, "$name.jpg")
        val os: OutputStream
        try {
            os = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
            os.flush()
            os.close()
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Error writing bitmap", e)
        }
        return imageFile
    }

    private suspend fun convertImage2(usersImages: String): MultipartBody.Part {
        return withContext(Dispatchers.IO) {
            val bitmap = getBitmapFromUrl(usersImages)
            if (bitmap != null) {
                val file = persistImage(bitmap, Calendar.getInstance().timeInMillis.toString())
                prepareFilePart("image", file)
            } else {
                throw IllegalArgumentException("Could not get bitmap from URL: $usersImages")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        id = intent.getStringExtra("id").toString()
        type = intent.getStringExtra("type").toString()
        id_cat = intent.getStringExtra("id_cat").toString()

        if (type == "edit") {
            val productMedias =
                intent.getSerializableExtra("product_medias") as ArrayList<ShopItemsResponse.Body.Data.ProductMedia>
            price = intent.getStringExtra("price").toString()
            productId = intent.getStringExtra("productId").toString()
            binding.edName.setText(intent.getStringExtra("name").toString())
            binding.edDescription.setText(intent.getStringExtra("description").toString())
            binding.edFee.setText(price)
            binding.tvTitle.text = getString(R.string.edit_product)
            binding.btnSubmit.text = getString(R.string.update)
            for (i in 0 until productMedias.size) {
                list.add(imageURL + productMedias[i].media)
            }
            if (list.isEmpty()) {
                list.add("")
            }
////            / Create a comma-separated string of IDs from productMedias
            commaSeparatedString =
                productMedias.joinToString(separator = ",") { it.id.toString() }

        } else {
            list.add("")
        }
        adapter = AddImagesAdapter(this, list)
        binding.rvImages.adapter = adapter
        adapter?.onItemClickListener = { pos, type ->
            if (type == 0) {
                if (list[0] == "") {
                    if (list.size == 5) {
                        Utils.showErrorDialog(
                            this,
                            getString(R.string.you_can_not_add_more_then_5_images)
                        )
                    } else {
                        getImage(this, 0)
                    }
                }
            } else {
                list.removeAt(pos)
                if (list.isEmpty()) {
                    list.add("")
                }
                adapter?.notifyDataSetChanged()
            }

        }
        binding.edFee.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Called after the text has been changed
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Called before the text is changed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Called as and when the text is being changed
                price = s.toString()
            }
        })
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.btnCancel.setOnClickListener {
            finish()
        }
        binding.btnSubmit.setOnClickListener {
            finish()
        }
        binding.tvAddMore.setOnClickListener {
            if (list.size == 5) {
                Utils.showErrorDialog(this, getString(R.string.you_can_not_add_more_then_5_images))
            } else {
                getImage(this, 0)
            }
        }
        binding.btnSubmit?.setOnClickListener {
            if (binding.edName.text.toString().isEmpty()) {
                Utils.showErrorDialog(this, getString(R.string.please_enter_product_name))

            } else if (binding.edFee.text.toString().isEmpty()

            ) {
                Utils.showErrorDialog(this, getString(R.string.please_enter_price))

            } else if (binding.edDescription.text.toString().isEmpty()

            ) {
                Utils.showErrorDialog(this, getString(R.string.please_enter_description))

            } else if (list[0] == ""

            ) {
                Utils.showErrorDialog(this, getString(R.string.please_select_at_least_one_image))

            } else {
                progressDialog.show(this)
                if (Utils.internetAvailability(this@AddProductActivity)) {
                    lifecycleScope.launch {
                        for (i in 0 until list.size) {
                            if (list[i].contains("http")) {
                                Log.e("onCreate: ", "start")
                                val imagePart = withContext(Dispatchers.IO) {
                                    convertImage2(list[i])
                                }
                                arrListImages.add(imagePart)
                            } else {
                                arrListImages.add(prepareFilePart("image", File(list[i])))
                            }
                        }

                        Log.e("onCreate: ", arrListImages.size.toString())

                        val hashMap = HashMap<String, RequestBody>().apply {
                            put("name", binding.edName.text.toString().trim().getTextRequestBody())
                            put(
                                "description",
                                binding.edDescription.text.toString().trim().getTextRequestBody()
                            )
                            put("price", price.getTextRequestBody())
                            put("shop_id", id.getTextRequestBody())
                            put("type_id", id_cat.toString().getTextRequestBody())
                        }

                        if (type == "edit") {
                            hashMap["update_id"] = productId.getTextRequestBody()
                            hashMap["delete_img_id"] = commaSeparatedString.getTextRequestBody()
                            authViewModel.editProduct(hashMap, arrListImages)
                        } else {
                            authViewModel.addProduct(hashMap, arrListImages)
                        }
                    }
                } else {
                    Utils.showToast(
                        this@AddProductActivity,
                        getString(R.string.no_internet_connection)
                    )
                }
            }
        }
        viewModelSetupAndResponse()

    }

    @SuppressLint("SetTextI18n")
    private fun viewModelSetupAndResponse() {

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        authViewModel.getError().observe(this) {
            Utils.showToast(this, it)
        }
        authViewModel.progressDialogData().observe(this) { isShowProgress ->
//            if (isShowProgress) {
//                progressDialog.show(this)
//            } else {
//                progressDialog.hide()
//            }
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
                    listString.clear()
                    categoriesListResponse.clear()
                    categoriesListResponse.addAll(it.body)
                    for (i in 0 until categoriesListResponse.size) {
                        listString.add(categoriesListResponse[i].name)

                    }
                    popUpCategories()

                }
            }
            authViewModel.onShopAddResponse().observe(this) { response ->
                response?.let {
                    if (it.code == 200) {
                        progressDialog.hide()

                        val intent = Intent(this, ShopDetailActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(intent)
                        finish() // Optional here, but keeps current activity clean
                    }
                }
            }


        }
        authViewModel.get_types(id)

    }
    private fun popUpCategories() {
        val spinner = binding.vehicleSpinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listString)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        for (i in categoriesListResponse.indices) {
            if (id_cat == categoriesListResponse[i].id.toString()) {
                spinner.setSelection(i)
            }
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Log.e("onItemSelected: ",position.toString() )
                Log.e("onItemSelected: ",categoriesListResponse[position].id.toString() )
                id_cat = categoriesListResponse[position].id.toString()

                // Do something with the selected item
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }


    }
}