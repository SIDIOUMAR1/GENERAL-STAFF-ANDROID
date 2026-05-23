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
import android.widget.AutoCompleteTextView
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
import com.genralstaff.responseModel.AvailableDriversResponse
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
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

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
    var audioSummary = ""
    var currentRecordingType = ""
    var startRecording = false
    var RandomAudioFileName = "ABCDEFGHIJKLMNOP"
    var playStatus = "0"
    var AudioSavePathInDevice: String? = null
    var countDownTimer: CountDownTimer? = null
    var mediaRecorder: MediaRecorder? = null
    var random: Random? = null
    private var elapsedTime: Long = 0

    // ✅ NOUVELLES VARIABLES AJOUTÉES
    private var assignDirectly = false
    private var selectedDriverId: Int? = null
    private var selectedDriverObj: AvailableDriversResponse.Driver? = null
    private var availableDrivers = ArrayList<AvailableDriversResponse.Driver>()
    private var driversLoaded = false
    private val avatarColors = listOf(
        "#6C63FF", "#FF6584", "#43A6C6", "#F7B731", "#26C6DA",
        "#EF5350", "#66BB6A", "#FFA726", "#AB47BC", "#29B6F6"
    )

    private var whatsappNumber = ""
    var userPhone = ""
    private var pendingUploadType = ""
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
        userPhone = intent.getStringExtra("user_phone") ?: ""
        binding.tvShopName.text = shop_name
        binding.tvLocation.text = getCity(latitude, longitude, this)
        binding.tvShopLocation.text = getCity(latitudeShop, longitudeShop, this)
        Log.e("onCreate: ", latitude + longitude)
        initializeSockets()

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.ivMic!!.setOnClickListener {
            currentRecordingType = "user_location"
            binding.rlRecordView.visibility = View.VISIBLE
            binding.ivMic.visibility = View.GONE
            Glide.with(this)
                .load(R.drawable.audiogif)
                .into(binding.ivGif!!)
            startResording()
        }

        binding.ivMicSummary!!.setOnClickListener {
            currentRecordingType = "order_summary"
            binding.rlRecordView.visibility = View.VISIBLE
            binding.ivMicSummary.visibility = View.GONE
            Glide.with(this)
                .load(R.drawable.audiogif)
                .into(binding.ivGif!!)
            startResording()
        }

        binding.tvSave!!.setOnClickListener {
            Glide.with(this).clear(binding.ivGif!!)
            binding.ivGif!!.setImageDrawable(null)
            binding.rlRecordView.visibility = View.GONE
            if (mediaRecorder != null) {
                try {
                    mediaRecorder!!.stop()
                } catch (e: RuntimeException) {
                    e.printStackTrace()
                    mediaRecorder!!.release()
                    mediaRecorder = null
                }

                startRecording = false
                countDownTimer!!.cancel()

                val file: File = File(AudioSavePathInDevice!!)
                if (file.exists()) {
                    val audioPath = AudioSavePathInDevice!!
                    val imagePart: MultipartBody.Part = prepareFilePart("media", File(audioPath))
                    pendingUploadType = currentRecordingType
                    authViewModel.uploadFiles(imagePart)
                    AudioSavePathInDevice = ""
                }
            }
        }

        binding.ivCross.setOnClickListener {
            audio = ""
            Glide.with(this).clear(binding.ivGif!!)
            binding.ivGif!!.setImageDrawable(null)
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
            binding.ivMic.visibility = View.VISIBLE
            binding.rlAudioPlay.visibility = View.GONE
            Glide.with(this).clear(binding.ivGif!!)
            binding.ivGif!!.setImageDrawable(null)
            binding.rlRecordView.visibility = View.GONE
            binding.tvDelete.visibility = View.GONE
        }

        binding.tvDeleteSummary.setOnClickListener {
            audioSummary = ""
            binding.ivMicSummary.visibility = View.VISIBLE
            binding.rlAudioPlaySummary.visibility = View.GONE
        }

        val driverTypes = arrayOf(
            getString(R.string.bicycle),
            getString(R.string.motorcycle),
            getString(R.string.human),
            getString(R.string.car)
        )
        binding.tvSelectDriverType.text = driverTypes.joinToString(", ")
        val checkedItems = BooleanArray(driverTypes.size) { false }

        binding.tvSelectDriverType.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.driver_type))
                .setMultiChoiceItems(driverTypes, checkedItems) { _, which, isChecked ->
                    checkedItems[which] = isChecked
                }
                .setPositiveButton(getString(R.string.done)) { dialog, _ ->
                    val selectedDriverTypes = mutableListOf<String>()
                    for (i in driverTypes.indices) {
                        if (checkedItems[i]) selectedDriverTypes.add(driverTypes[i])
                    }
                    binding.tvSelectDriverType.text = selectedDriverTypes.joinToString(", ")
                    driversLoaded = false
                    availableDrivers.clear()
                    selectedDriverId = null
                    selectedDriverObj = null
                    hideSelectedDriverCard()
                    binding.btnSelectDriver.visibility = View.VISIBLE
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.cbAssignDirectly.setOnCheckedChangeListener { _, isChecked ->
            assignDirectly = isChecked
            if (isChecked) {
                val typeSelected = binding.tvSelectDriverType.text.toString()
                if (typeSelected.isEmpty() || typeSelected == getString(R.string.select_driver_type)) {
                    Utils.showErrorDialog(this, "Veuillez d'abord sélectionner le type de véhicule")
                    binding.cbAssignDirectly.isChecked = false
                    return@setOnCheckedChangeListener
                }
                binding.llDriverSelection.visibility = View.VISIBLE
                if (selectedDriverObj != null) showSelectedDriverCard(selectedDriverObj!!)
                if (!driversLoaded) loadAvailableDrivers()
            } else {
                binding.llDriverSelection.visibility = View.GONE
                hideSelectedDriverCard()
            }
        }

        // ✅ NOUVEAU: Checkbox WhatsApp
        binding.cbAssignWhatsapp.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.llWhatsappNumber.visibility = View.VISIBLE
            } else {
                binding.llWhatsappNumber.visibility = View.GONE
                binding.edWhatsappNumber.setText("")
            }
        }

        binding.btnSelectDriver.setOnClickListener {
            if (availableDrivers.isEmpty()) loadAvailableDrivers()
            else showDriverPickerDialog()
        }

        binding.tvChangeDriver.setOnClickListener {
            hideSelectedDriverCard()
            binding.btnSelectDriver.visibility = View.VISIBLE
            selectedDriverId = null
            selectedDriverObj = null
            showDriverPickerDialog()
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

    override fun onResume() {
        super.onResume()
        Log.e("AddOrderActivity", "📡 onResume() - Activation du listener")
        socketManager.onRegister(this)
        socketManager.onaddorderListener()
    }

    override fun onPause() {
        super.onPause()
        Log.e("AddOrderActivity", "📡 onPause() - Désactivation")
        socketManager.unRegister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("AddOrderActivity", "📡 onDestroy()")
        socketManager.unRegister(this)
    }

    private fun startResording() {
        if (!startRecording) {
            if (checkPermission()) {
                var cw: ContextWrapper = ContextWrapper(applicationContext)
                var directory = cw.getDir("imageDir", Context.MODE_PRIVATE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    AudioSavePathInDevice =
                        directory.absolutePath + "/" + CreateRandomAudioFileName(5) + "audioRecording.m4a"
                } else {
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
                        object : CountDownTimer(300000, 1000) {
                            override fun onTick(millisUntilFinished: Long) {
                                elapsedTime = 300 - (millisUntilFinished / 1000)
                                val minutes = elapsedTime / 60
                                val seconds = elapsedTime % 60
                                binding.timer!!.text =
                                    String.format(Locale.US, "%02d:%02d", minutes, seconds)
                            }

                            override fun onFinish() {
                                startRecording = false
                                binding.timer!!.text = "05:00"
                                if (mediaRecorder != null) {
                                    try {
                                        mediaRecorder!!.stop()
                                    } catch (e: RuntimeException) {
                                        e.printStackTrace()
                                        mediaRecorder!!.release()
                                        mediaRecorder = null
                                        return
                                    }
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
            mediaRecorder!!.setAudioEncodingBitRate(128000)
            mediaRecorder!!.setAudioSamplingRate(44100)
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
        Log.e("AddOrderActivity", "🔧 DÉBUT initializeSockets()")
        socketManager = MyApplication.mInstance?.getSocketManager()!!
        Log.e("AddOrderActivity", "✅ SocketManager obtenu")
        socketManager.init()
        Log.e("AddOrderActivity", "✅ Socket initialisé")
        socketManager.onRegister(this)
        Log.e("AddOrderActivity", "✅ Observer enregistré")
        socketManager.onaddorderListener()
        Log.e("AddOrderActivity", "✅ Listener add_order activé")
        if (socketManager.isConnected()) {
            Log.e("AddOrderActivity", "✅ Socket CONNECTÉ")
        } else {
            Log.e("AddOrderActivity", "❌ Socket NON CONNECTÉ")
        }
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

        Log.e("AddOrder", "==========================================")
        Log.e("AddOrder", "DÉBUT addOrderSocket()")
        Log.e("AddOrder", "assignDirectly = $assignDirectly")
        Log.e("AddOrder", "selectedDriverId = $selectedDriverId")
        Log.e("AddOrder", "==========================================")

        if (assignDirectly && selectedDriverId == null) {
            Utils.showErrorDialog(this, "Veuillez sélectionner un chauffeur")
            return
        }

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

                if (assignDirectly && selectedDriverId != null) {
                    put("assigned_driver_id", selectedDriverId!!)
                    Log.e("AddOrder", "✅ AJOUT DE assigned_driver_id = $selectedDriverId")
                } else {
                    Log.e("AddOrder", "❌ PAS D'ASSIGNATION DIRECTE")
                    Log.e("AddOrder", "   Raison: assignDirectly=$assignDirectly, selectedDriverId=$selectedDriverId")
                }

                if (description.isNotEmpty()) {
                    put("description", description)
                }
                if (audio.isNotEmpty()) {
                    put("audio_user_location", audio)
                }
                if (audioSummary.isNotEmpty()) {
                    put("audio_summary", audioSummary)
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

                // ✅ NOUVEAU: Numéro WhatsApp externe
                // APRÈS
                if (binding.cbAssignWhatsapp.isChecked) {
                    val number = binding.edWhatsappNumber.text.toString().trim()
                    if (number.isNotEmpty()) {
                        val fullNumber = if (number.startsWith("+")) number else "+222$number"
                        put("whatsapp_number", fullNumber)
                        Log.e("AddOrder", "✅ WhatsApp number: $fullNumber")
                    }
                }
                // ✅ Envoyer confirmation WhatsApp au client
                if (binding.cbAssignWhatsapp.isChecked && userPhone.isNotEmpty()) {
                    val fullPhone = if (userPhone.startsWith("+")) userPhone else "+222$userPhone"
                    put("user_whatsapp", fullPhone)
                }
            }

            Log.e("AddOrder", "==========================================")
            Log.e("AddOrder", "JSON ENVOYÉ AU SOCKET:")
            Log.e("AddOrder", jsonObjects.toString())
            Log.e("AddOrder", "==========================================")

            Log.e("AddOrder", "audio variable: '$audio'")
            Log.e("AddOrder", "audioSummary variable: '$audioSummary'")
            Log.e("AddOrder", "pendingUploadType: '$pendingUploadType'")

            val orderIdToCancel = intent.getStringExtra("order_id_to_cancel")
            if (!orderIdToCancel.isNullOrEmpty()) {
                val cancelJson = JSONObject().apply {
                    put("order_id", orderIdToCancel)
                }
                socketManager.cancelOrderSocket(cancelJson)
                Log.e("AddOrder", "✅ Ancienne commande annulée: $orderIdToCancel")
            }


            socketManager.addOrderSocket(jsonObjects)

        } else {
            Utils.showToast(this, getString(R.string.no_internet_connection))
        }
    }

    fun getEnglishString(context: Context, resId: Int): String {
        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale.ENGLISH)
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
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
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
                    if (pendingUploadType == "user_location") {
                        audio = it.body.media.toString()
                        binding.ivMic.visibility = View.GONE
                        binding.rlAudioPlay.visibility = View.VISIBLE
                        val audio_url = profileBaseUrl + audio
                        binding.rightAudioPlayer.setAudioTarget(audio_url)
                    } else if (pendingUploadType == "order_summary") {
                        audioSummary = it.body.media.toString()
                        binding.ivMicSummary.visibility = View.GONE
                        binding.rlAudioPlaySummary.visibility = View.VISIBLE
                        val audio_url = profileBaseUrl + audioSummary
                        binding.rightAudioPlayerSummary.setAudioTarget(audio_url)
                    }
                    pendingUploadType = ""
                    binding.rlRecordView.visibility = View.GONE
                    binding.apply {
                        rlRecordView.visibility = View.GONE
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
                Log.e("AddOrderActivity", "📥 RÉPONSE REÇUE: add_order_listner")
                Log.e("AddOrderActivity", "📦 ARGS: $args")

                activityScope.launch {
                    progressDialog.hide()
                    val notAdded = args.optInt("notAdded", -1)

                    if (binding.cbAssignWhatsapp.isChecked) {
                        Utils.showToast(this@AddOrderActivity, "🚀 Commande lancée via WhatsApp !")
                        startActivity(
                            Intent(this@AddOrderActivity, OrderHistoryActivity::class.java)
                                .putExtra("type", "current_orders")
                                .putExtra("types", "add_orders")
                        )
                        finish()
                        return@launch
                    }

                    if (notAdded == 0) {
                        Log.e("AddOrderActivity", "❌ Aucun chauffeur disponible")
                        showAlert(getString(R.string.no_driver_type_found_for_this_order))
                    } else {
                        val orderId = args.optInt("id", -1)
                        if (orderId != -1) {
                            Log.e("AddOrderActivity", "✅ Commande créée ID: $orderId")
                            Utils.showToast(this@AddOrderActivity, "Commande créée avec succès")
                        } else {
                            Log.e("AddOrderActivity", "⚠️ Réponse reçue mais format inattendu")
                            Utils.showToast(this@AddOrderActivity, "Commande envoyée")
                        }
                        startActivity(
                            Intent(this@AddOrderActivity, OrderHistoryActivity::class.java)
                                .putExtra("type", "current_orders")
                                .putExtra("types", "add_orders")
                        )
                        finish()
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

    private fun loadAvailableDrivers() {
        if (!Utils.internetAvailability(this)) {
            Utils.showToast(this, getString(R.string.no_internet_connection))
            return
        }

        progressDialog.show(this)

        val selectedDriverTypes = binding.tvSelectDriverType.text.toString()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() && it != getString(R.string.select_driver_type) }
            .map { selectedType ->
                when (selectedType) {
                    getString(R.string.bicycle) -> getEnglishString(this, R.string.bicycle)
                    getString(R.string.motorcycle) -> getEnglishString(this, R.string.motorcycle)
                    getString(R.string.human) -> getEnglishString(this, R.string.human)
                    getString(R.string.car) -> getEnglishString(this, R.string.car)
                    else -> selectedType
                }
            }
            .joinToString(",")

        Log.e("LoadDrivers", "Shop ID: $shop_id")
        Log.e("LoadDrivers", "Driver Types: $selectedDriverTypes")

        activityScope.launch {
            try {
                val response = authViewModel.getAvailableDrivers(shop_id, selectedDriverTypes)
                progressDialog.hide()

                Log.e("LoadDrivers", "Response Code: ${response.code()}")
                Log.e("LoadDrivers", "Response Successful: ${response.isSuccessful}")
                Log.e("LoadDrivers", "Response Body: ${response.body()}")

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    Log.e("LoadDrivers", "Body Code: ${body.code}")
                    Log.e("LoadDrivers", "Drivers Count: ${body.body.drivers.size}")

                    if (body.code == 200) {
                        availableDrivers.clear()
                        availableDrivers.addAll(body.body.drivers)

                        Log.e("LoadDrivers", "Available Drivers: ${availableDrivers.size}")

                        if (availableDrivers.isEmpty()) {
                            Utils.showErrorDialog(this@AddOrderActivity, "Aucun chauffeur disponible")
                            binding.cbAssignDirectly.isChecked = false
                            binding.llDriverSelection.visibility = View.GONE
                        } else {
                            Log.e("LoadDrivers", "Calling setupDriverAdapter()")
                            driversLoaded = true
                            showDriverPickerDialog()
                            Log.e("LoadDrivers", "Adapter set, driver count: ${availableDrivers.size}")
                        }
                    } else {
                        Log.e("LoadDrivers", "Error: ${body.message}")
                        Utils.showToast(this@AddOrderActivity, body.message)
                        binding.cbAssignDirectly.isChecked = false
                        binding.llDriverSelection.visibility = View.GONE
                    }
                } else {
                    Log.e("LoadDrivers", "Response not successful or body null")
                    Utils.showToast(this@AddOrderActivity, "Erreur lors du chargement des chauffeurs")
                    binding.cbAssignDirectly.isChecked = false
                    binding.llDriverSelection.visibility = View.GONE
                }
            } catch (e: Exception) {
                progressDialog.hide()
                Log.e("LoadDrivers", "Exception: ${e.message}", e)
                e.printStackTrace()
                Utils.showToast(this@AddOrderActivity, "Erreur: ${e.message ?: "Unknown"}")
                binding.cbAssignDirectly.isChecked = false
                binding.llDriverSelection.visibility = View.GONE
            }
        }
    }

    private fun showDriverPickerDialog() {
        val bottomSheet = com.google.android.material.bottomsheet.BottomSheetDialog(
            this, R.style.BottomSheetDialogTheme
        )
        val view = layoutInflater.inflate(R.layout.dialog_driver_picker, null)
        bottomSheet.setContentView(view)

        val etSearch = view.findViewById<android.widget.EditText>(R.id.etSearch)
        val ivClose = view.findViewById<android.widget.ImageView>(R.id.ivClose)
        val ivClearSearch = view.findViewById<android.widget.ImageView>(R.id.ivClearSearch)
        val rvDrivers = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvDrivers)
        val tvDriverCount = view.findViewById<android.widget.TextView>(R.id.tvDriverCount)
        val llEmpty = view.findViewById<android.widget.LinearLayout>(R.id.llEmpty)

        tvDriverCount.text = "${availableDrivers.size} chauffeur(s) disponible(s)"

        val filteredList = ArrayList<AvailableDriversResponse.Driver>(availableDrivers)
        val adapter = DriverPickerAdapter(filteredList) { driver ->
            selectedDriverId = driver.id
            selectedDriverObj = driver
            showSelectedDriverCard(driver)
            binding.btnSelectDriver.visibility = View.GONE
            bottomSheet.dismiss()
            Utils.showToast(this, "✅ ${driver.name} sélectionné")
        }
        rvDrivers.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        rvDrivers.adapter = adapter

        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val query = s.toString().trim().lowercase()
                ivClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                filteredList.clear()
                filteredList.addAll(
                    if (query.isEmpty()) availableDrivers
                    else availableDrivers.filter { d ->
                        d.name.lowercase().contains(query) || d.phone.contains(query)
                    }
                )
                adapter.notifyDataSetChanged()
                tvDriverCount.text = "${filteredList.size} résultat(s)"
                llEmpty.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
                rvDrivers.visibility = if (filteredList.isEmpty()) View.GONE else View.VISIBLE
            }
        })

        ivClearSearch.setOnClickListener { etSearch.setText("") }
        ivClose.setOnClickListener { bottomSheet.dismiss() }
        bottomSheet.show()
    }

    private fun showSelectedDriverCard(driver: AvailableDriversResponse.Driver) {
        binding.tvSelectedDriverInitials.text = driver.name.take(2).uppercase()
        binding.tvSelectedDriverName.text = driver.name
        binding.tvSelectedDriverInfo.text = "${driver.phone}  •  ${getVehicleEmoji(driver.vehicle_type)} ${driver.vehicle_type}  •  ${driver.distance} km"
        binding.llSelectedDriverCard.visibility = View.VISIBLE
    }

    private fun hideSelectedDriverCard() {
        binding.llSelectedDriverCard.visibility = View.GONE
    }

    private fun getVehicleEmoji(type: String?): String = when (type?.lowercase()) {
        "bicycle" -> "🚲"
        "motorcycle" -> "🏍️"
        "car" -> "🚗"
        "human" -> "🚶"
        else -> "🚗"
    }

    inner class DriverPickerAdapter(
        private val drivers: List<AvailableDriversResponse.Driver>,
        private val onSelect: (AvailableDriversResponse.Driver) -> Unit
    ) : RecyclerView.Adapter<DriverPickerAdapter.VH>() {

        inner class VH(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            val tvInitials: android.widget.TextView = view.findViewById(R.id.tvInitials)
            val tvName: android.widget.TextView = view.findViewById(R.id.tvDriverName)
            val tvPhone: android.widget.TextView = view.findViewById(R.id.tvDriverPhone)
            val tvVehicle: android.widget.TextView = view.findViewById(R.id.tvVehicleType)
            val tvDistance: android.widget.TextView = view.findViewById(R.id.tvDistance)
            val llCall: android.widget.LinearLayout = view.findViewById(R.id.llCall)
            val llMessage: android.widget.LinearLayout = view.findViewById(R.id.llMessage)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(layoutInflater.inflate(R.layout.item_driver_card, parent, false))

        override fun getItemCount() = drivers.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val driver = drivers[position]
            val colorIndex = Math.abs(driver.name.hashCode()) % avatarColors.size
            holder.tvInitials.text = driver.name.take(2).uppercase()
            holder.tvInitials.background.setColorFilter(
                android.graphics.Color.parseColor(avatarColors[colorIndex]),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            holder.tvName.text = driver.name
            holder.tvPhone.text = driver.phone
            holder.tvVehicle.text = "${getVehicleEmoji(driver.vehicle_type)} ${driver.vehicle_type ?: "N/A"}"
            holder.tvDistance.text = "${driver.distance} km"
            holder.itemView.setOnClickListener { onSelect(driver) }

            holder.llCall.setOnClickListener {
                val phone = driver.phone
                if (phone.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:$phone"))
                    it.context.startActivity(intent)
                }
            }

            holder.llMessage.setOnClickListener {
                selectedDriverId = driver.id
                selectedDriverObj = driver
                showSelectedDriverCard(driver)
                binding.btnSelectDriver.visibility = View.GONE

                try {
                    startActivity(
                        Intent(this@AddOrderActivity, ChatActivity::class.java)
                            .putExtra("otherUserId", driver.id.toString())
                            .putExtra("otherUserName", driver.name ?: "")
                            .putExtra("phone", driver.phone ?: "")
                            .putExtra("otherUserImage", driver.profile_pic ?: "")
                            .putExtra("shopId", shop_id ?: "")
                            .putExtra("order_id", "")
                            .putExtra("type", "staff_driver")
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    Utils.showToast(this@AddOrderActivity, "Erreur: ${e.message}")
                }
            }
        }
    }
}