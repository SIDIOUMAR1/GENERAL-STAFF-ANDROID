package com.genralstaff.home.ui

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.TimePicker
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.genralstaff.R
import com.genralstaff.base.imageURL
import com.genralstaff.databinding.ActivityAddShopBinding
import com.genralstaff.network.ErrorType
import com.genralstaff.responseModel.CategoriesResponse
import com.genralstaff.responseModel.GetShopsResponse
import com.genralstaff.utils.CustomProgressDialog
import com.genralstaff.utils.ImagePickerActivityUtility
import com.genralstaff.utils.Utils
import com.genralstaff.utils.Validator
import com.genralstaff.utils.getTextRequestBody
import com.genralstaff.utils.prepareFilePart
import com.genralstaff.utils.sessionExpire
import com.genralstaff.viewmodel.AuthViewModel
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddShopActivity : ImagePickerActivityUtility(), TimePickerDialog.OnTimeSetListener {
    var imagePath = ""
    var category_id = ""
    var latitude = ""
    var longitude = ""
    var phone_no = ""
    var country_code = ""
    var categoriesList = ArrayList<CategoriesResponse.Body>()
    var categoriesList2 = ArrayList<String>()
    private var shopTimings: List<GetShopsResponse.Body.Data.ShopTiming>? = null

    var type = ""
    var id = ""
    var name = ""
    var name_ar = ""
    var name_fr = ""
    var image = ""
    var open_time = ""
    var close_time = ""
    var location = ""
    var status = ""
    var description = ""


    private lateinit var authViewModel: AuthViewModel
private val progressDialog by lazy { CustomProgressDialog() }
    lateinit var binding: ActivityAddShopBinding
    override fun selectedImage(imagePath: String?, code: Int?) {
        this.imagePath = imagePath!!

        Glide.with(this).load(imagePath).placeholder(R.drawable.place_holder)
            .into(binding.civProfile)
    }
    private fun populateDayTimeViews() {
        shopTimings?.forEach { timing ->
            val day = timing.day  // Example: "Monday"
            val openTime = timing.open_time?.removeSuffix(":00") ?: ""
            val closeTime = timing.close_time?.removeSuffix(":00") ?: ""

            val openViewId = resources.getIdentifier("tvOpenTime$day", "id", packageName)
            val closeViewId = resources.getIdentifier("tvCloseTime$day", "id", packageName)

            val openView = findViewById<TextView?>(openViewId)
            val closeView = findViewById<TextView?>(closeViewId)

            openView?.text = openTime
            closeView?.text = closeTime
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddShopBinding.inflate(layoutInflater)
        setContentView(binding.root)
        type = intent.getStringExtra("type").toString()
        if (type=="edit"){
            shopTimings = intent.getSerializableExtra("shop_timings") as? List<GetShopsResponse.Body.Data.ShopTiming>
            populateDayTimeViews()
        }
        setupDayTimePickers() // ✅ Call here

        viewModelSetupAndResponse()
//        binding.tvOpenTime.setOnClickListener {
//            status = "0"
//
//            val now = Calendar.getInstance()
//            val hour: Int = now.get(Calendar.HOUR_OF_DAY)
//            val minute: Int = now.get(Calendar.MINUTE)
//
//            val tpd = TimePickerDialog(
//                this, R.style.DialogTimePicker, this, hour,
//                minute,
//                false
//            )
//            tpd.show()
//
//
//        }
//        binding.tvCloseTime.setOnClickListener {
//            status = "1"
//
//            val now = Calendar.getInstance()
//            val hour: Int = now.get(Calendar.HOUR_OF_DAY)
//            val minute: Int = now.get(Calendar.MINUTE)
//
//            val tpd = TimePickerDialog(
//                this, R.style.DialogTimePicker, this, hour,
//                minute,
//                false
//            )
//            tpd.show()
//
//
//        }

        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.btnCancel.setOnClickListener {
            finish()
        }
        binding.btnSubmit.setOnClickListener {
            finish()
        }
        binding.civProfile.setOnClickListener {
            getImage(this, 0)
        }
        binding.ivPick.setOnClickListener {
            getImage(this, 0)
        }
        binding.tvLocation.setOnClickListener {
            openPlacePicker()
        }
        binding.btnSubmit?.setOnClickListener {
//            finish()
            Log.e("category_id: ", category_id)
            when {
                category_id == "" -> {
                    Utils.showErrorDialog(this, getString(R.string.please_select_shop_category))

                }

                binding.edName.text.toString().isEmpty() -> {
                    Utils.showErrorDialog(this, getString(R.string.please_enter_shop_name))

                }

                binding.edNumber.text.toString().isEmpty() -> {
                    Utils.showErrorDialog(this, getString(R.string.please_enter_phone_number))

                }

                !Validator.isValidMobile(binding.edNumber.text.toString().trim()) -> {
                    Utils.showErrorDialog(
                        this, getString(R.string.please_enter_valid_phone_number)
                    )
                }


                binding.tvLocation.text.toString().isEmpty() -> {
                    Utils.showErrorDialog(this, getString(R.string.please_enter_shop_location))

                }

                !validateDaysTime() -> {
                    return@setOnClickListener // ⛔ Validation failed, error already shown
                }


                binding.edDescription.text.toString().isEmpty() -> {
                    Utils.showErrorDialog(this, getString(R.string.please_enter_shop_description))

                }

                else -> {
                    val shopTimingsArray = JSONArray()
                    val days =
                        listOf(getString(R.string.monday), getString(R.string.tuesday), getString(R.string.wednesday), getString(R.string.thursday), getString(R.string.friday), getString(R.string.saturday),getString(R.string.sunday))

                    for (day in days) {
                        val openView = findViewById<TextView>(resources.getIdentifier("tvOpenTime$day", "id", packageName))
                        val closeView = findViewById<TextView>(resources.getIdentifier("tvCloseTime$day", "id", packageName))

                        val openTime = openView.text.toString()
                        val closeTime = closeView.text.toString()

                        // Only add if openTime is selected (as per your logic)
                        if (openTime.isNotEmpty() && closeTime.isNotEmpty()) {
                            val obj = JSONObject()
                            obj.put("day", day)
                            obj.put("open_time", openTime)
                            obj.put("close_time", closeTime)
                            shopTimingsArray.put(obj)
                        }
                    }
                    Log.e("onCreate: ------------", shopTimingsArray.toString())

                    if (Utils.internetAvailability(this@AddShopActivity)) {
                        if (type == "edit") {
                            val hashMap = HashMap<String, RequestBody>()
                            hashMap["name"] =
                                binding.edName.text.toString().trim().getTextRequestBody()
                            hashMap["description"] =
                                binding.edDescription.text.toString().trim().getTextRequestBody()
                            hashMap["location"] =
                                binding.tvLocation.text.toString().trim().getTextRequestBody()
                            hashMap["category_id"] = category_id.trim().getTextRequestBody()
                            hashMap["longitude"] = longitude.trim().getTextRequestBody()
                            hashMap["latitude"] = latitude.trim().getTextRequestBody()
                            hashMap["update_id"] = id.trim().getTextRequestBody()
//                            hashMap["open_time"] = binding.tvOpenTime.text.toString().getTextRequestBody()
                            hashMap["name_ar"] =
                                binding.edArabicName.text.toString().getTextRequestBody()
                            hashMap["name_fr"] =
                                binding.edFrenchName.text.toString().getTextRequestBody()
                            hashMap["phone"] = binding.edNumber.text.toString().getTextRequestBody()
                            hashMap["country_code"] =
                                binding.ccplogin.selectedCountryCodeWithPlus.toString()
                                    .getTextRequestBody()
                            hashMap["shop_timings"] = shopTimingsArray.toString().getTextRequestBody()

//                            hashMap["close_time"] = binding.tvCloseTime.text.toString().getTextRequestBody()

                            if (imagePath.isNotEmpty()) {
                                val imagePart: MultipartBody.Part =
                                    prepareFilePart("image", File(imagePath!!))
                                authViewModel.editShop(hashMap, imagePart)
                            } else {
                                authViewModel.editShopWithoutImage(hashMap)

                            }
                        } else {
                            if (imagePath == ""

                            ) {
                                Utils.showErrorDialog(
                                    this,
                                    getString(R.string.please_select_shop_image)
                                )

                            } else {
                                val hashMap = HashMap<String, RequestBody>()
                                hashMap["name"] =
                                    binding.edName.text.toString().trim().getTextRequestBody()
                                hashMap["description"] =
                                    binding.edDescription.text.toString().trim()
                                        .getTextRequestBody()
                                hashMap["location"] =
                                    binding.tvLocation.text.toString().trim().getTextRequestBody()
                                hashMap["category_id"] = category_id.trim().getTextRequestBody()
                                hashMap["longitude"] = longitude.trim().getTextRequestBody()
                                hashMap["latitude"] = latitude.trim().getTextRequestBody()
                                hashMap["name_ar"] =
                                    binding.edArabicName.text.toString().getTextRequestBody()
                                hashMap["name_fr"] =
                                    binding.edFrenchName.text.toString().getTextRequestBody()
                                hashMap["phone"] =
                                    binding.edNumber.text.toString().getTextRequestBody()
                                hashMap["country_code"] =
                                    binding.ccplogin.selectedCountryCodeWithPlus.toString()
                                        .getTextRequestBody()
                                hashMap["shop_timings"] = shopTimingsArray.toString().getTextRequestBody()

//                                hashMap["open_time"] = binding.tvOpenTime.text.toString().getTextRequestBody()
//                                hashMap["close_time"] = binding.tvCloseTime.text.toString().getTextRequestBody()
                                val imagePart: MultipartBody.Part =
                                    prepareFilePart("image", File(imagePath!!))
                                authViewModel.addShop(hashMap, imagePart)

                            }
                        }
                    } else {
                        Utils.showToast(
                            this@AddShopActivity,
                            getString(R.string.no_internet_connection)
                        )
                    }
                }
            }
        }

    }

    private fun validateDaysTime(): Boolean {
        val days =
            listOf(getString(R.string.monday), getString(R.string.tuesday), getString(R.string.wednesday), getString(R.string.thursday), getString(R.string.friday), getString(R.string.saturday),getString(R.string.sunday))

        for (day in days) {
            val openView =
                findViewById<TextView>(resources.getIdentifier("tvOpenTime$day", "id", packageName))
            val closeView = findViewById<TextView>(
                resources.getIdentifier(
                    "tvCloseTime$day",
                    "id",
                    packageName
                )
            )

            val openTime = openView.text.toString().trim()
            val closeTime = closeView.text.toString().trim()

            // Only one is selected — invalid
            if ((openTime.isNotEmpty() && closeTime.isEmpty()) || (closeTime.isNotEmpty() && openTime.isEmpty())) {
                Utils.showErrorDialog(this,
                    getString(R.string.please_select_both_open_and_close_time_for, day))
                return false
            }
        }

        return true
    }

    private var timeStatusTag = ""

    private fun setupDayTimePickers() {
        val days =
            listOf(getString(R.string.monday), getString(R.string.tuesday), getString(R.string.wednesday), getString(R.string.thursday), getString(R.string.friday), getString(R.string.saturday),getString(R.string.sunday))

        for (day in days) {
            val openId = resources.getIdentifier("tvOpenTime$day", "id", packageName)
            val closeId = resources.getIdentifier("tvCloseTime$day", "id", packageName)

            findViewById<TextView>(openId).setOnClickListener {
                timeStatusTag = "open_$day"
                showTimePicker()
            }

            findViewById<TextView>(closeId).setOnClickListener {
                timeStatusTag = "close_$day"
                showTimePicker()
            }
        }
    }

    private fun showTimePicker() {
        val now = Calendar.getInstance()
        val hour: Int = now.get(Calendar.HOUR_OF_DAY)
        val minute: Int = now.get(Calendar.MINUTE)

        val tpd = TimePickerDialog(
            this, R.style.DialogTimePicker, this, hour,
            minute,
            false
        )
        tpd.show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val hh12Format = if (hourOfDay.toString().length == 1) "0$hourOfDay" else "$hourOfDay"
        val mintFormat = if (minute.toString().length == 1) "0$minute" else "$minute"
        val timeString = "$hh12Format:$mintFormat"

        try {
            val sdf = SimpleDateFormat("H:mm", Locale.getDefault())
            val obj = sdf.parse(timeString)
            val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(obj!!)

            val idName = timeStatusTag.replace("open_", "tvOpenTime")
                .replace("close_", "tvCloseTime")
            val viewId = resources.getIdentifier(idName, "id", packageName)
            findViewById<TextView>(viewId).text = formattedTime

        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }


    @SuppressLint("SetTextI18n")
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
        authViewModel.onCategoriesResponse().observe(this) { response ->
            response?.let {
                if (it.code == 200) {
                    progressDialog.hide()
                    categoriesList.clear()
                    categoriesList2.clear()
                    categoriesList.add(
                        CategoriesResponse.Body(
                            0,
                            getString(R.string.select_category)
                        )
                    )
                    categoriesList.addAll(it.body)
                    for (i in 0 until categoriesList.size) {
                        categoriesList2.add(categoriesList[i].name)
                    }
                    if (type == "edit") {
                        category_id = intent.getStringExtra("category_id").toString()
                        id = intent.getStringExtra("id").toString()
                        name = intent.getStringExtra("name").toString()
                        name_fr = intent.getStringExtra("name_fr").toString()
                        name_ar = intent.getStringExtra("name_ar").toString()
                        open_time = intent.getStringExtra("open_time").toString()
                        close_time = intent.getStringExtra("close_time").toString()
                        image = intent.getStringExtra("image").toString()
                        location = intent.getStringExtra("location").toString()
                        latitude = intent.getStringExtra("latitude").toString()
                        longitude = intent.getStringExtra("longitude").toString()
                        description = intent.getStringExtra("description").toString()
                        country_code = intent.getStringExtra("country_code").toString()
                        phone_no = intent.getStringExtra("phone_no").toString()
                        binding.btnSubmit.text = getString(R.string.update)
                        binding.tvTitle.text = getString(R.string.edit_shop)
//                        binding.tvOpenTime.text = open_time
//                        binding.tvCloseTime.text = close_time
                        binding.edName.setText(name)
                        binding.edFrenchName.setText(name_fr)
                        binding.edArabicName.setText(name_ar)
                        binding.edNumber.setText(phone_no)
                        binding.ccplogin.setCountryForPhoneCode(country_code.toInt())
                        binding.tvLocation.text = location
                        binding.edDescription.setText(description)
                        Glide.with(this).load(imageURL + image).placeholder(
                            R.drawable.place_holder
                        ).into(binding.civProfile)

                    }
                    popUp()


                }
            }
        }
        authViewModel.onShopAddResponse().observe(this) { response ->
            response?.let {
                if (it.code == 200) {
                    progressDialog.hide()
                    finish()
                }
            }
        }

        authViewModel.categories()
    }

    private fun popUp() {
        val spinner = findViewById<Spinner>(R.id.categorySpinner)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoriesList2)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        if (type == "edit") {
            for (i in categoriesList.indices) {
                if (category_id == categoriesList[i].id.toString()) {
                    spinner.setSelection(i)
                }
            }

        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (categoriesList[position].id != 0) {
                    category_id = categoriesList[position].id.toString()
                }
                // Do something with the selected item
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun openPlacePicker() {
        val fields =
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val intent =
            Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this)
        startActivityForResult.launch(intent)
    }

    var startActivityForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == AutocompleteActivity.RESULT_OK) {
                val place: Place? = result.data?.let { Autocomplete.getPlaceFromIntent(it) }
                place?.let {
                    latitude = place.latLng!!.latitude.toString()
                    longitude = place.latLng!!.longitude.toString()
                    binding.tvLocation.setHorizontallyScrolling(true)
                    binding.tvLocation.movementMethod = ScrollingMovementMethod()
                    binding.tvLocation.text = place.address!!.toString()
                    location = place.address!!.toString()
                }
            }
        }

}