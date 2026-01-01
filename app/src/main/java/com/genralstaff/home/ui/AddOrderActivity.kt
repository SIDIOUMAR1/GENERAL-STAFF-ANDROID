package com.genralstaff.home.ui

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.MediaRecorder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.genralstaff.R
import com.genralstaff.adapter.ProductsAdapter
import com.genralstaff.base.profileBaseUrl
import com.genralstaff.databinding.ActivityAddOrderBinding
import com.genralstaff.network.ErrorType
import com.genralstaff.responseModel.CategoriesResponse
import com.genralstaff.responseModel.GetChatMessagesResponse
import com.genralstaff.responseModel.ShopItemsResponse
import com.genralstaff.sockets.SocketManager
import com.genralstaff.utils.CustomProgressDialog
import com.genralstaff.utils.MyApplication
import com.genralstaff.utils.RecordAudioActivity
import com.genralstaff.utils.Utils
import com.genralstaff.utils.getCity
import com.genralstaff.utils.prepareFilePart
import com.genralstaff.utils.sessionExpire
import com.genralstaff.viewmodel.AuthViewModel
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson

import com.rygelouv.audiosensei.player.AudioSenseiListObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

class AddOrderActivity : AppCompatActivity(), SocketManager.Observer {
    var latitude = ""
    var longitude = ""
    var latitudeShop = ""
    var longitudeShop = ""
    var userId = ""
    var shop_id = ""
    var shop_name = ""
    var product_id = "0"
    var audio = ""
    var startRecording = false
    var RandomAudioFileName = "ABCDEFGHIJKLMNOP"
    var playStatus = "0"
    var AudioSavePathInDevice: String? = null
    var countDownTimer: CountDownTimer? = null
    var mediaRecorder: MediaRecorder? = null
    var random: Random? = null
    private var elapsedTime: Long = 0

    private lateinit var socketManager: SocketManager
    private val activityScope = CoroutineScope(Dispatchers.Main)
private val progressDialog by lazy { CustomProgressDialog() }

    lateinit var binding: ActivityAddOrderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AudioSenseiListObserver.getInstance().registerLifecycle(this@AddOrderActivity.lifecycle)
        checkPermission()
        random = Random()

        userId = intent.getStringExtra("userId").toString()
        shop_id = intent.getStringExtra("shopId").toString()
        shop_name = intent.getStringExtra("shopName").toString()
        latitude = intent.getStringExtra("user_latitude").toString()
        longitude = intent.getStringExtra("user_longitude").toString()
        latitudeShop = intent.getStringExtra("latitudeShop").toString()
        longitudeShop = intent.getStringExtra("longitudeShop").toString()
        binding.tvShopName.text = shop_name
        binding.tvLocation.text = getCity(latitude, longitude, this)
        binding.tvShopLocation.text = getCity(latitudeShop, longitudeShop, this)
        Log.e("onCreate: ", latitude + longitude)
        initializeSockets()

        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.ivMic!!.setOnClickListener {

            binding.rlRecordView.visibility = View.VISIBLE
            binding.ivMic.visibility = View.GONE
            Glide.with(this)
                .load(R.drawable.audiogif)
                .into(binding.ivGif!!)
            startResording()
        }

        binding.tvSave!!.setOnClickListener {
            Glide.with(this).clear(binding.ivGif!!)
            binding.ivGif!!.setImageDrawable(null) // Optionally clear the ImageView content

            binding.rlRecordView.visibility = View.GONE
//            binding.rlChatView.visibility = View.VISIBLE
            if (mediaRecorder != null) {
                mediaRecorder!!.stop()
            }
            startRecording = false
            countDownTimer!!.cancel()

            val file: File = File(AudioSavePathInDevice!!)
            if (file.exists()) {
                val audioPath = AudioSavePathInDevice!!
                val imagePart: MultipartBody.Part =
                    prepareFilePart("media", File(audioPath))
                authViewModel.uploadFiles(imagePart)
                AudioSavePathInDevice = ""
            }
        }

        binding.ivCross.setOnClickListener {
            audio = ""
            Glide.with(this).clear(binding.ivGif!!)
            binding.ivGif!!.setImageDrawable(null) // Optionally clear the ImageView content
            binding.rlRecordView.visibility = View.GONE
            binding.ivMic.visibility = View.VISIBLE
            binding.rlAudioPlay.visibility = View.GONE
            if (mediaRecorder != null) {
                mediaRecorder!!.stop()

            }
            startRecording = false
            countDownTimer!!.cancel()
            startRecording = false
            binding.timer!!.text = "00:00"

        }
        binding.tvDelete.setOnClickListener {
            audio = ""
            Glide.with(this).clear(binding.ivGif!!)
            binding.ivGif!!.setImageDrawable(null) // Optionally clear the ImageView content
            binding.rlRecordView.visibility = View.GONE
            binding.tvDelete.visibility = View.GONE
            binding.ivMic.visibility = View.VISIBLE
            binding.rlAudioPlay.visibility = View.GONE

        }

        // List of driver types
        val driverTypes = arrayOf(
            getString(R.string.bicycle),
            getString(R.string.motorcycle), getString(R.string.human), getString(R.string.car)
        )
        binding.tvSelectDriverType.text = driverTypes.joinToString(", ")

        // Boolean array to track checked items
        val checkedItems = BooleanArray(driverTypes.size) { false }

        // When the user clicks on the TextView, show the multi-choice dialog
        binding.tvSelectDriverType.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.driver_type))
                .setMultiChoiceItems(driverTypes, checkedItems) { _, which, isChecked ->
                    // Update the checkedItems array when user selects or unselects an item
                    checkedItems[which] = isChecked
                }
                .setPositiveButton(getString(R.string.done)) { dialog, _ ->
                    // Handle the 'Done' action
                    val selectedDriverTypes = mutableListOf<String>()
                    for (i in driverTypes.indices) {
                        if (checkedItems[i]) {
                            selectedDriverTypes.add(driverTypes[i])
                        }
                    }
                    // Set the selected items to the TextView
                    binding.tvSelectDriverType.text = selectedDriverTypes.joinToString(", ")
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()

        }

        binding.ivCopyShopLocation.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Shop Location", binding.tvShopLocation.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Shop Location copied", Toast.LENGTH_SHORT).show()
        }
        binding.ivCopyLocation.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Shop Location", binding.tvLocation.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Location copied", Toast.LENGTH_SHORT).show()
        }
        binding.btnCancel.setOnClickListener {
            finish()
        }
        binding.btnSubmit.setOnClickListener {
            if (binding.tvSelectDriverType.text.toString().isEmpty()) {
                Utils.showErrorDialog(this, getString(R.string.please_select_driver_type))
            } else if (binding.edFee.text.isEmpty()) {
                Utils.showErrorDialog(this, getString(R.string.please_enter_driver_fee))
            } else if (binding.cbSendUserLocation.isChecked && (latitude.isEmpty() || latitude == "null" || longitude.isEmpty() || longitude == "null")) {
                Utils.showErrorDialog(this, getString(R.string.please_enter_location))
            } else if (binding.cbSendShopLocation.isChecked && (latitudeShop.isEmpty() || latitudeShop == "null" || longitudeShop.isEmpty() || longitudeShop == "null")) {
                Utils.showErrorDialog(this, getString(R.string.please_enter_shop_location))
            } else if (!binding.cbSendUserLocation.isChecked && !binding.cbSendShopLocation.isChecked) {
                Utils.showErrorDialog(this, getString(R.string.at_least_one_location_required))
            } else {
                addOrderSocket()
            }
        }
        binding.tvLocation.setOnClickListener {
            openPlacePicker()
        }
        binding.tvShopLocation.setOnClickListener {
            openPlacePickerNew()
        }
    }

    private fun startResording() {
        if (!startRecording) {
            if (checkPermission()) {

                var cw: ContextWrapper = ContextWrapper(applicationContext)
                var directory = cw.getDir("imageDir", Context.MODE_PRIVATE);



                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    AudioSavePathInDevice = directory.absolutePath + "/" + CreateRandomAudioFileName(5) + "audioRecording.mp3"
                    AudioSavePathInDevice =
                        directory.absolutePath + "/" + CreateRandomAudioFileName(5) + "audioRecording.m4a"
                } else {
//                    AudioSavePathInDevice = Environment.getExternalStorageDirectory().absolutePath + "/" + CreateRandomAudioFileName(5) + "audioRecording.mp3"
                    AudioSavePathInDevice =
                        Environment.getExternalStorageDirectory().absolutePath + "/" + CreateRandomAudioFileName(
                            5
                        ) + "audioRecording.m4a"
                }

                mediaRecorderReady()
                try {

                    startRecording = true
                    playStatus = "0"

                    mediaRecorder!!.prepare()
                    mediaRecorder!!.start()

                    countDownTimer =
                        object : CountDownTimer(300000, 1000) { // 300,000 milliseconds = 5 minutes
                            override fun onTick(millisUntilFinished: Long) {
                                elapsedTime =
                                    300 - (millisUntilFinished / 1000) // Calculate elapsed time in seconds
                                val minutes = elapsedTime / 60
                                val seconds = elapsedTime % 60
                                binding.timer!!.text =
                                    String.format(Locale.US, "%02d:%02d", minutes, seconds)
                            }

                            override fun onFinish() {
                                startRecording = false
                                binding.timer!!.text = "05:00"
                                if (mediaRecorder != null) {
                                    mediaRecorder!!.stop()
                                    val file: File = File(AudioSavePathInDevice!!)
                                    if (file.exists()) {

                                        val audioPath = AudioSavePathInDevice!!
                                        val imagePart: MultipartBody.Part =
                                            prepareFilePart("media", File(audioPath))
                                        authViewModel.uploadFiles(imagePart)

                                    }
                                }


                            }
                        }.start()
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                requestPermissions()
            }
        } else {
            countDownTimer!!.cancel()
            countDownTimer!!.onFinish()
            startRecording = false
        }
    }


    private fun mediaRecorderReady() {
        mediaRecorder = MediaRecorder()
        try {
            mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            // Set higher quality parameters
            mediaRecorder!!.setAudioEncodingBitRate(128000) // 128 kbps
            mediaRecorder!!.setAudioSamplingRate(44100)     // CD quality

            mediaRecorder!!.setOutputFile(AudioSavePathInDevice)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error initializing recorder: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    fun CreateRandomAudioFileName(string: Int): String {
        val stringBuilder = StringBuilder(string)
        var i = 0
        while (i < string) {
            stringBuilder.append(RandomAudioFileName[random!!.nextInt(RandomAudioFileName.length)])
            i++
        }
        return stringBuilder.toString()
    }

    private fun requestPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this@AddOrderActivity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RecordAudioActivity.RequestPermissionCode
            )

        } else {

            ActivityCompat.requestPermissions(
                this@AddOrderActivity,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO
                ),
                RecordAudioActivity.RequestPermissionCode
            )
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestPermissionCode && resultCode == RESULT_OK) {
            try {
                val bundle = data!!.extras
                val recordingFilePath = bundle!!.getString("data")
                Log.e("fileUrlSong", recordingFilePath.toString())
                val file: File = File(recordingFilePath)
                if (file.exists()) {

                    val audioPath = recordingFilePath!!
                    val imagePart: MultipartBody.Part =
                        prepareFilePart("media", File(audioPath))
                    authViewModel.uploadFiles(imagePart)

                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        const val RequestPermissionCode = 100
    }

    fun checkPermission(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val result1 = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.RECORD_AUDIO
            )
            return result1 == PackageManager.PERMISSION_GRANTED
        } else {
            val result = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            val result1 = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.RECORD_AUDIO
            )
            return result == PackageManager.PERMISSION_GRANTED &&
                    result1 == PackageManager.PERMISSION_GRANTED

        }

    }


    private fun initializeSockets() {
        socketManager = MyApplication.mInstance?.getSocketManager()!!
        socketManager.init()
        socketManager.onRegister(this)
        viewModelSetupAndResponse()
        callShopsApi()

    }

    private fun openPlacePicker() {
        val fields =
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val intent =
            Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this)
        startActivityForResult.launch(intent)
    }

    private fun openPlacePickerNew() {
        val fields =
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val intent =
            Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this)
        startActivityForResultNew.launch(intent)
    }

    private fun addOrderSocket() {
        val description = binding.edDescription.text.toString()
        if (Utils.internetAvailability(this)) {
            progressDialog.show(this)
            val selectedDriverTypes = binding.tvSelectDriverType.text.toString()
                .split(",")
                .map { it.trim() }
                .map { selectedType ->
                    when (selectedType) {
                        getString(R.string.bicycle) -> getEnglishString(this, R.string.bicycle)
                        getString(R.string.motorcycle) -> getEnglishString(this, R.string.motorcycle)
                        getString(R.string.human) -> getEnglishString(this, R.string.human)
                        getString(R.string.car) -> getEnglishString(this, R.string.car)
                        else -> selectedType
                    }
                }

            val driverTypeEnglish = selectedDriverTypes.joinToString(",")

            val sub_admin_id = MyApplication.prefs?.getString("userId")
            val jsonObjects = JSONObject().apply {
                put("user_id", userId!!.toInt())
                put("sub_admin_id", sub_admin_id!!.toInt())
                put("shop_id", shop_id!!.toInt())
                put("product_id", product_id!!.toInt())
                put("location", binding.tvLocation.text.toString())
                put("shop_address", binding.tvShopLocation.text.toString())
                put("driver_type", driverTypeEnglish.trim())

                if (description.isNotEmpty()) {
                    put("description", description)
                }
                if (audio.isNotEmpty()) {
                    put("audio", audio)
                }

                if (binding.cbSendUserLocation.isChecked &&
                    latitude.isNotEmpty() &&
                    latitude != "null" &&
                    longitude.isNotEmpty() &&
                    longitude != "null") {
                    put("latitude", latitude)
                    put("longitude", longitude)
                }

                if (binding.cbSendShopLocation.isChecked &&
                    latitudeShop.isNotEmpty() &&
                    latitudeShop != "null" &&
                    longitudeShop.isNotEmpty() &&
                    longitudeShop != "null") {
                    put("shop_latitude", latitudeShop)
                    put("shop_longitude", longitudeShop)
                }

                put("delivery_charge", binding.edFee.text.toString())
            }

            Log.e("jsonObjects: ", jsonObjects.toString())
            socketManager.addOrderSocket(jsonObjects)

        } else {
            Utils.showToast(this, getString(R.string.no_internet_connection))
        }
    }

    fun getEnglishString(context: Context, resId: Int): String {
        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale.ENGLISH) // Force English locale
        return context.createConfigurationContext(config).resources.getString(resId)
    }

    private lateinit var authViewModel: AuthViewModel
    var shopItems2 = ArrayList<String>()

    private fun popUp() {
        val spinner = findViewById<Spinner>(R.id.productSpinner)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, shopItems2)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter



        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != 0) {
                    product_id = shopItems[position - 1].id.toString()
                }
                // Do something with the selected item
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    var shopItems = ArrayList<ShopItemsResponse.Body.Data>()
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
        authViewModel.onUploadProfileResponse().observe(this) { response ->
            response?.let {
                if (it.code == 200) {
                    audio = it.body.media.toString()
                    binding.apply {
                        rlRecordView.visibility = View.GONE
                        ivMic.visibility = View.GONE
                        rlAudioPlay.visibility = View.VISIBLE
                        val audio_url = profileBaseUrl + audio
                        binding.rightAudioPlayer.setAudioTarget(audio_url)
                    }

                }
            }
        }
        authViewModel.onShopItemsResponse().observe(this) { response ->
            response?.let {
                if (it.code == 200) {
                    shopItems2.clear()
                    shopItems.clear()
                    shopItems.addAll(it.body.data)
                    if (shopItems.isEmpty()) {
                        shopItems2.add(getString(R.string.no_products_available))

                    } else {
                        shopItems2.add(getString(R.string.select_product))
                    }
                    for (i in 0 until shopItems.size) {
                        shopItems2.add(shopItems[i].name)
                    }
                    popUp()

                }
            }
        }
        authViewModel.onShopDetailResponse().observe(this) { response ->
            response?.let {
                if (it.code == 200 && it.body != null) {
                    val data = it.body




                    binding.apply {

                        edShopDescription.setText(data.description)


                    }
                }
            }
        }

        authViewModel.shopDetail(shop_id)
    }

    private fun callShopsApi() {
        val map = HashMap<String, String>()
        map["shop_id"] = shop_id
        map["page"] = "1"
        map["limit"] = "100"
        authViewModel.shopItems(map)
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
                }
            }
        }
    var startActivityForResultNew =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == AutocompleteActivity.RESULT_OK) {
                val place: Place? = result.data?.let { Autocomplete.getPlaceFromIntent(it) }
                place?.let {
                    latitudeShop = place.latLng!!.latitude.toString()
                    longitudeShop = place.latLng!!.longitude.toString()
                    binding.tvShopLocation.setHorizontallyScrolling(true)
                    binding.tvShopLocation.movementMethod = ScrollingMovementMethod()
                    binding.tvShopLocation.text = place.address!!.toString()
                }
            }
        }

    override fun onResponseArray(event: String, args: JSONArray) {

    }

    override fun onResponse(event: String, args: JSONObject) {
        when (event) {
            SocketManager.add_order_listner -> {
                activityScope.launch {
                    progressDialog.hide()

                    // Extract 'notAdded' from the 'args' JSONObject
                    val notAdded = args.optInt("notAdded", -1)  // -1 is the default if not found

                    if (notAdded == 0) {
                        // Show alert if notAdded is 0
                        showAlert(getString(R.string.no_driver_type_found_for_this_order))
                    } else {
                        // Continue with the intent if notAdded is not 0
                        startActivity(
                            Intent(this@AddOrderActivity, OrderHistoryActivity::class.java)
                                .putExtra("type", "current_orders")
                                .putExtra("types", "add_orders")
                        )
                    }
                }
            }
        }
    }

    private fun showAlert(message: String) {
        AlertDialog.Builder(this)
            .setTitle("alert")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


    override fun onError(event: String, vararg args: Array<*>) {
    }

    override fun onBlockError(event: String, args: String) {
    }
}