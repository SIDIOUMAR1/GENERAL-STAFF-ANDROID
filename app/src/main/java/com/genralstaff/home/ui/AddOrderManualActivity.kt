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
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.genralstaff.R
import com.genralstaff.base.profileBaseUrl
import com.genralstaff.databinding.ActivityAddOrderManualBinding
import com.genralstaff.network.ErrorType
import com.genralstaff.responseModel.AvailableDriversResponse
import com.genralstaff.responseModel.ShopsListResponse
import com.genralstaff.sockets.SocketManager
import com.genralstaff.utils.CustomProgressDialog
import com.genralstaff.utils.MyApplication
import com.genralstaff.utils.RecordAudioActivity
import com.genralstaff.utils.Utils
import com.genralstaff.utils.prepareFilePart
import com.genralstaff.utils.sessionExpire
import com.genralstaff.viewmodel.AuthViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rygelouv.audiosensei.player.AudioSenseiListObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.json.JSONObject
import java.io.File
import java.util.Locale
import java.util.Random

class AddOrderManualActivity : AppCompatActivity(), SocketManager.Observer {

    private var latitudeUser = ""
    private var longitudeUser = ""
    private var latitudeShop = ""
    private var longitudeShop = ""

    // ✅ FIX: ShopsListResponse.Body.Data (pas ShopsListResponse.Shop)
    private var selectedShopId = ""
    private var selectedShop: ShopsListResponse.Body.Data? = null
    private var shopList = ArrayList<ShopsListResponse.Body.Data>()
    private var product_id = "0"

    private var audio = ""
    private var audioSummary = ""
    private var currentRecordingType = ""
    private var pendingUploadType = ""
    private var startRecording = false
    private val RandomAudioFileName = "ABCDEFGHIJKLMNOP"
    private var AudioSavePathInDevice: String? = null
    private var countDownTimer: CountDownTimer? = null
    private var mediaRecorder: MediaRecorder? = null
    private var random: Random? = null
    private var elapsedTime: Long = 0

    private var assignDirectly = false
    private var selectedDriverId: Int? = null
    private var selectedDriverObj: AvailableDriversResponse.Driver? = null
    private var availableDrivers = ArrayList<AvailableDriversResponse.Driver>()
    private var driversLoaded = false
    private val avatarColors = listOf(
        "#6C63FF", "#FF6584", "#43A6C6", "#F7B731", "#26C6DA",
        "#EF5350", "#66BB6A", "#FFA726", "#AB47BC", "#29B6F6"
    )

    private lateinit var socketManager: SocketManager
    private lateinit var authViewModel: AuthViewModel
    private val activityScope = CoroutineScope(Dispatchers.Main)
    private val progressDialog by lazy { CustomProgressDialog() }
    lateinit var binding: ActivityAddOrderManualBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddOrderManualBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AudioSenseiListObserver.getInstance().registerLifecycle(lifecycle)
        checkPermission()
        random = Random()
        initializeSockets()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        socketManager.onRegister(this)
        socketManager.onaddorderListener()
    }

    override fun onPause() {
        super.onPause()
        socketManager.unRegister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        socketManager.unRegister(this)
    }

    private fun initializeSockets() {
        socketManager = MyApplication.mInstance?.getSocketManager()!!
        socketManager.init()
        socketManager.onRegister(this)
        socketManager.onaddorderListener()
        viewModelSetupAndResponse()
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener { finish() }
        binding.btnCancel.setOnClickListener { finish() }
        setupShopSpinner()

        binding.ivCopyShopLocation.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Shop Location", binding.tvShopLocation.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Emplacement copié", Toast.LENGTH_SHORT).show()
        }

        val driverTypes = arrayOf(
            getString(R.string.bicycle), getString(R.string.motorcycle),
            getString(R.string.human), getString(R.string.car)
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
                    val selected = mutableListOf<String>()
                    for (i in driverTypes.indices) { if (checkedItems[i]) selected.add(driverTypes[i]) }
                    binding.tvSelectDriverType.text = selected.joinToString(", ")
                    driversLoaded = false; availableDrivers.clear()
                    selectedDriverId = null; selectedDriverObj = null
                    hideSelectedDriverCard()
                    binding.btnSelectDriver.visibility = View.VISIBLE
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.cancel)) { d, _ -> d.dismiss() }
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

        binding.btnSelectDriver.setOnClickListener {
            if (availableDrivers.isEmpty()) loadAvailableDrivers() else showDriverPickerDialog()
        }

        binding.tvChangeDriver.setOnClickListener {
            hideSelectedDriverCard()
            binding.btnSelectDriver.visibility = View.VISIBLE
            selectedDriverId = null; selectedDriverObj = null
            showDriverPickerDialog()
        }

        binding.cbAssignWhatsapp.setOnCheckedChangeListener { _, isChecked ->
            binding.llWhatsappDriverNumber.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) binding.edWhatsappNumber.setText("")
        }

        binding.ivMicSummary.setOnClickListener {
            currentRecordingType = "order_summary"
            binding.rlRecordView.visibility = View.VISIBLE
            binding.ivMicSummary.visibility = View.GONE  // ✅ cache le bon mic
            Glide.with(this).load(R.drawable.audiogif).into(binding.ivGif)
            startResording()
        }

        binding.tvDeleteSummary.setOnClickListener {
            audioSummary = ""
            binding.ivMicSummary.visibility = View.VISIBLE
            binding.rlAudioPlaySummary.visibility = View.GONE
        }

        binding.ivMic.setOnClickListener {
            currentRecordingType = "user_location"
            binding.rlRecordView.visibility = View.VISIBLE
            binding.ivMic.visibility = View.GONE
            Glide.with(this).load(R.drawable.audiogif).into(binding.ivGif)
            startResording()
        }

        binding.tvSave.setOnClickListener {
            Glide.with(this).clear(binding.ivGif)
            binding.ivGif.setImageDrawable(null)
            binding.rlRecordView.visibility = View.GONE
            if (mediaRecorder != null) {
                try { mediaRecorder!!.stop() } catch (e: RuntimeException) {
                    mediaRecorder!!.release(); mediaRecorder = null
                }
                startRecording = false; countDownTimer?.cancel()
                val file = File(AudioSavePathInDevice ?: return@setOnClickListener)
                if (file.exists()) {
                    pendingUploadType = currentRecordingType
                    authViewModel.uploadFiles(prepareFilePart("media", file))
                    AudioSavePathInDevice = ""
                }
            }
        }

        binding.ivCross.setOnClickListener {
            audio = ""
            Glide.with(this).clear(binding.ivGif)
            binding.ivGif.setImageDrawable(null)
            binding.rlRecordView.visibility = View.GONE
            // ✅ Restaurer le bon mic selon le type en cours
            if (currentRecordingType == "order_summary") {
                binding.ivMicSummary.visibility = View.VISIBLE
            } else {
                binding.ivMic.visibility = View.VISIBLE
            }
            binding.rlAudioPlay.visibility = View.GONE
            mediaRecorder?.stop(); startRecording = false; countDownTimer?.cancel()
            binding.timer.text = "00:00"
        }

        binding.tvDelete.setOnClickListener {
            audio = ""
            binding.ivMic.visibility = View.VISIBLE
            binding.rlAudioPlay.visibility = View.GONE
        }

        binding.btnSubmit.setOnClickListener { validateAndSubmit() }
    }

    // ── Spinner ──
    private fun setupShopSpinner() {
        binding.llShopLoading.visibility = View.VISIBLE
        binding.spinnerShop.setOnClickListener {
            if (shopList.isEmpty()) {
                Utils.showToast(this, "Chargement en cours...")
                return@setOnClickListener
            }
            showShopPickerDialog()
        }
    }

    private fun showShopPickerDialog() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_shop_picker)
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setDimAmount(0.5f)
        dialog.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        val etSearch = dialog.findViewById<android.widget.EditText>(R.id.etSearchShop)
        val rvShops = dialog.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvShops)
        val ivClose = dialog.findViewById<android.widget.ImageView>(R.id.ivCloseShop)

        ivClose.setOnClickListener { dialog.dismiss() }

        val filteredList = ArrayList<ShopsListResponse.Body.Data>(shopList)

        val adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
                val view = android.view.LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_shop_picker, parent, false)
                return object : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {}
            }
            override fun getItemCount() = filteredList.size
            override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
                val tv = holder.itemView.findViewById<android.widget.TextView>(R.id.tvShopName)
                tv.text = filteredList[position].name
                tv.setOnClickListener {
                    val shop = filteredList[position]
                    selectedShop = shop
                    selectedShopId = shop.id.toString()
                    binding.spinnerShop.text = shop.name
                    fillShopFields(shop)
                    authViewModel.shopItems(hashMapOf("shop_id" to selectedShopId, "page" to "1", "limit" to "100"))
                    authViewModel.shopDetail(selectedShopId)
                    driversLoaded = false
                    availableDrivers.clear()
                    selectedDriverId = null
                    selectedDriverObj = null
                    hideSelectedDriverCard()
                    binding.btnSelectDriver.visibility = View.VISIBLE
                    dialog.dismiss()
                }
            }
        }

        rvShops.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        rvShops.adapter = adapter

        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val query = s.toString().trim().lowercase()
                filteredList.clear()
                filteredList.addAll(
                    if (query.isEmpty()) shopList
                    else shopList.filter { it.name.lowercase().contains(query) }
                )
                adapter.notifyDataSetChanged()
            }
        })

        dialog.show()
    }

    // ✅ FIX: paramètre ShopsListResponse.Body.Data
    private fun fillShopFields(shop: ShopsListResponse.Body.Data) {
        latitudeShop = shop.latitude ?: ""
        longitudeShop = shop.longitude ?: ""
        binding.tvShopLocation.text = shop.location ?: ""
        binding.edShopDescription.setText(shop.description ?: "")
    }

    private fun clearShopFields() {
        latitudeShop = ""; longitudeShop = ""
        binding.tvShopLocation.text = ""
        binding.edShopDescription.setText("")
    }

    // ── Validation ──
    private fun validateAndSubmit() {
        val driverType = binding.tvSelectDriverType.text.toString()
        val fee = binding.edFee.text.toString()
        val userWhatsapp = binding.edUserWhatsapp.text.toString().trim()
        when {
            selectedShopId.isEmpty() -> Utils.showErrorDialog(this, "Veuillez sélectionner un restaurant")
            driverType.isEmpty() || driverType == getString(R.string.select_driver_type) -> Utils.showErrorDialog(this, getString(R.string.please_select_driver_type))
            fee.isEmpty() -> Utils.showErrorDialog(this, getString(R.string.please_enter_driver_fee))
            userWhatsapp.isEmpty() -> Utils.showErrorDialog(this, "Le numéro WhatsApp du client est obligatoire")
            assignDirectly && selectedDriverId == null -> Utils.showErrorDialog(this, "Veuillez sélectionner un chauffeur")
            else -> addOrderSocket()
        }
    }

    private fun addOrderSocket() {
        if (!Utils.internetAvailability(this)) { Utils.showToast(this, getString(R.string.no_internet_connection)); return }
        progressDialog.show(this)
        val sub_admin_id = MyApplication.prefs?.getString("userId")

        val driverTypeEnglish = binding.tvSelectDriverType.text.toString()
            .split(",").map { it.trim() }
            .map { t -> when (t) {
                getString(R.string.bicycle) -> getEnglishString(this, R.string.bicycle)
                getString(R.string.motorcycle) -> getEnglishString(this, R.string.motorcycle)
                getString(R.string.human) -> getEnglishString(this, R.string.human)
                getString(R.string.car) -> getEnglishString(this, R.string.car)
                else -> t
            }}.joinToString(",")

        val userLocationLink = binding.edUserLocationLink.text.toString().trim()
        parseLatLngFromMapsUrl(userLocationLink)?.let { latitudeUser = it.first; longitudeUser = it.second }

        val rawUserWa = binding.edUserWhatsapp.text.toString().trim()
        val userWhatsapp = if (rawUserWa.startsWith("+")) rawUserWa else "+222$rawUserWa"

        val json = JSONObject().apply {
            put("user_id", sub_admin_id!!.toInt())
            put("sub_admin_id", sub_admin_id.toInt())
            put("shop_id", selectedShopId.toInt())
            put("product_id", product_id.toInt())
            put("driver_type", driverTypeEnglish.trim())
            put("delivery_charge", binding.edFee.text.toString())
            if (binding.cbSendShopLocation.isChecked && latitudeShop.isNotEmpty() && latitudeShop != "null") {
                put("shop_latitude", latitudeShop); put("shop_longitude", longitudeShop)
            }
            if (binding.cbSendUserLocation.isChecked && latitudeUser.isNotEmpty()) {
                put("latitude", latitudeUser); put("longitude", longitudeUser)
            }
            if (userLocationLink.isNotEmpty()) put("location", userLocationLink)
            if (assignDirectly && selectedDriverId != null) put("assigned_driver_id", selectedDriverId!!)
            if (binding.cbAssignWhatsapp.isChecked) {
                val driverWa = binding.edWhatsappNumber.text.toString().trim()
                if (driverWa.isNotEmpty()) put("whatsapp_number", if (driverWa.startsWith("+")) driverWa else "+222$driverWa")

            }
            put("user_whatsapp", userWhatsapp)
            binding.edDescription.text.toString().let { if (it.isNotEmpty()) put("description", it) }
            if (audioSummary.isNotEmpty()) put("audio_summary", audioSummary)
            if (audio.isNotEmpty()) put("audio_user_location", audio)
            put("is_manual_order", true)
        }
        socketManager.addOrderSocket(json)
    }

    // ── ViewModel ──
    private fun viewModelSetupAndResponse() {
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        authViewModel.getError().observe(this) { Utils.showToast(this, it) }
        authViewModel.progressDialogData().observe(this) { isShow -> if (isShow) progressDialog.show(this) else progressDialog.hide() }
        authViewModel.onShowErrorCode().observe(this) { if (it == ErrorType.UNAUTHORIZED) sessionExpire() }

        // ✅ Observer liste restaurants
        authViewModel.onSubadminShopsResponse().observe(this) { response ->
            binding.llShopLoading.visibility = View.GONE
            Log.e("SHOPS_DEBUG", "=== RESPONSE REÇUE ===")
            Log.e("SHOPS_DEBUG", "response null? ${response == null}")
            Log.e("SHOPS_DEBUG", "code = ${response?.code}")
            Log.e("SHOPS_DEBUG", "message = ${response?.message}")
            Log.e("SHOPS_DEBUG", "data size = ${response?.body?.data?.size}")
            Log.e("SHOPS_DEBUG", "data = ${response?.body?.data}")
            response?.let {
                if (it.code == 200 && it.body.data.isNotEmpty()) {
                    shopList.clear()
                    shopList.addAll(it.body.data) // ✅ ArrayList<ShopsListResponse.Body.Data>
                    val names = ArrayList<String>().apply {
                        add("── Sélectionner un restaurant ──")
                        shopList.forEach { s -> add(s.name) }
                    }
                    binding.spinnerShop.isClickable = true
                } else {
                    Utils.showToast(this, "Aucun restaurant disponible")
                }
            }
        }

        authViewModel.onShopDetailResponse().observe(this) { response ->
            response?.let { if (it.code == 200 && it.body != null) binding.edShopDescription.setText(it.body.description ?: "") }
        }

        authViewModel.onUploadProfileResponse().observe(this) { response ->
            response?.let {
                if (it.code == 200) {
                    when (pendingUploadType) {
                        "user_location" -> { audio = it.body.media.toString(); binding.ivMic.visibility = View.GONE; binding.rlAudioPlay.visibility = View.VISIBLE; binding.rightAudioPlayer.setAudioTarget(profileBaseUrl + audio) }
                        "order_summary" -> { audioSummary = it.body.media.toString(); binding.ivMicSummary.visibility = View.GONE; binding.rlAudioPlaySummary.visibility = View.VISIBLE; binding.rightAudioPlayerSummary.setAudioTarget(profileBaseUrl + audioSummary) }
                    }
                    pendingUploadType = ""; binding.rlRecordView.visibility = View.GONE
                }
            }
        }
        Log.e("SHOPS_DEBUG", "=== APPEL getSubadminShops() ===")
        authViewModel.getSubadminShops()
    }

    // ── Socket ──
    override fun onResponse(event: String, args: JSONObject) {
        when (event) {
            SocketManager.add_order_listner -> {
                activityScope.launch {
                    progressDialog.hide()
                    val notAdded = args.optInt("notAdded", -1)
                    val whatsappSent = args.optBoolean("whatsapp_sent", false)

                    if (binding.cbAssignWhatsapp.isChecked) {
                        Utils.showToast(this@AddOrderManualActivity, "🚀 Commande lancée via WhatsApp !")
                        startActivity(
                            Intent(this@AddOrderManualActivity, OrderHistoryActivity::class.java)
                                .putExtra("type", "current_orders")
                                .putExtra("types", "add_orders")
                        )
                        finish()
                        return@launch
                    }
                    if (notAdded == 0) {
                        showAlert(getString(R.string.no_driver_type_found_for_this_order))
                    } else {
                        Utils.showToast(this@AddOrderManualActivity, "🚀 Commande lancée !")
                        startActivity(Intent(this@AddOrderManualActivity, OrderHistoryActivity::class.java).putExtra("type", "current_orders").putExtra("types", "add_orders"))
                        finish()
                    }
                }
            }
        }
    }

    override fun onResponseArray(event: String, args: org.json.JSONArray) {}
    override fun onError(event: String, vararg args: Array<*>) {}
    override fun onBlockError(event: String, args: String) {}

    // ── Drivers ──
    private fun loadAvailableDrivers() {
        if (!Utils.internetAvailability(this)) { Utils.showToast(this, getString(R.string.no_internet_connection)); return }
        if (selectedShopId.isEmpty()) { Utils.showErrorDialog(this, "Veuillez d'abord sélectionner un restaurant"); binding.cbAssignDirectly.isChecked = false; binding.llDriverSelection.visibility = View.GONE; return }
        progressDialog.show(this)

        val types = binding.tvSelectDriverType.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }
            .map { t -> when (t) { getString(R.string.bicycle) -> getEnglishString(this, R.string.bicycle); getString(R.string.motorcycle) -> getEnglishString(this, R.string.motorcycle); getString(R.string.human) -> getEnglishString(this, R.string.human); getString(R.string.car) -> getEnglishString(this, R.string.car); else -> t } }.joinToString(",")

        activityScope.launch {
            try {
                val response = authViewModel.getAvailableDrivers(selectedShopId, types)
                progressDialog.hide()
                if (response.isSuccessful && response.body()?.code == 200) {
                    availableDrivers.clear(); availableDrivers.addAll(response.body()!!.body.drivers)
                    if (availableDrivers.isEmpty()) { Utils.showErrorDialog(this@AddOrderManualActivity, "Aucun chauffeur"); binding.cbAssignDirectly.isChecked = false; binding.llDriverSelection.visibility = View.GONE }
                    else { driversLoaded = true; showDriverPickerDialog() }
                } else { binding.cbAssignDirectly.isChecked = false; binding.llDriverSelection.visibility = View.GONE }
            } catch (e: Exception) { progressDialog.hide(); binding.cbAssignDirectly.isChecked = false; binding.llDriverSelection.visibility = View.GONE }
        }
    }

    private fun showDriverPickerDialog() {
        val bottomSheet = com.google.android.material.bottomsheet.BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.dialog_driver_picker, null)
        bottomSheet.setContentView(view)
        val etSearch = view.findViewById<android.widget.EditText>(R.id.etSearch)
        val ivClose = view.findViewById<android.widget.ImageView>(R.id.ivClose)
        val ivClear = view.findViewById<android.widget.ImageView>(R.id.ivClearSearch)
        val rvDrivers = view.findViewById<RecyclerView>(R.id.rvDrivers)
        val tvCount = view.findViewById<android.widget.TextView>(R.id.tvDriverCount)
        val llEmpty = view.findViewById<android.widget.LinearLayout>(R.id.llEmpty)
        tvCount.text = "${availableDrivers.size} chauffeur(s)"
        val filtered = ArrayList<AvailableDriversResponse.Driver>(availableDrivers)
        val adapter = DriverPickerAdapter(filtered) { driver ->
            selectedDriverId = driver.id; selectedDriverObj = driver
            showSelectedDriverCard(driver); binding.btnSelectDriver.visibility = View.GONE
            bottomSheet.dismiss(); Utils.showToast(this, "✅ ${driver.name} sélectionné")
        }
        rvDrivers.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this); rvDrivers.adapter = adapter
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val q = s.toString().trim().lowercase()
                ivClear.visibility = if (q.isNotEmpty()) View.VISIBLE else View.GONE
                filtered.clear(); filtered.addAll(if (q.isEmpty()) availableDrivers else availableDrivers.filter { d -> d.name.lowercase().contains(q) || d.phone.contains(q) })
                adapter.notifyDataSetChanged(); tvCount.text = "${filtered.size} résultat(s)"
                llEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE; rvDrivers.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
            }
        })
        ivClear.setOnClickListener { etSearch.setText("") }; ivClose.setOnClickListener { bottomSheet.dismiss() }; bottomSheet.show()
    }

    private fun showSelectedDriverCard(driver: AvailableDriversResponse.Driver) {
        binding.tvSelectedDriverInitials.text = driver.name.take(2).uppercase()
        binding.tvSelectedDriverName.text = driver.name
        binding.tvSelectedDriverInfo.text = "${driver.phone}  •  ${getVehicleEmoji(driver.vehicle_type)} ${driver.vehicle_type}  •  ${driver.distance} km"
        binding.llSelectedDriverCard.visibility = View.VISIBLE
    }

    private fun hideSelectedDriverCard() { binding.llSelectedDriverCard.visibility = View.GONE }

    private fun getVehicleEmoji(type: String?) = when (type?.lowercase()) { "bicycle" -> "🚲"; "motorcycle" -> "🏍️"; "car" -> "🚗"; "human" -> "🚶"; else -> "🚗" }

    inner class DriverPickerAdapter(
        private val drivers: List<AvailableDriversResponse.Driver>,
        private val onSelect: (AvailableDriversResponse.Driver) -> Unit
    ) : RecyclerView.Adapter<DriverPickerAdapter.VH>() {
        inner class VH(val view: View) : RecyclerView.ViewHolder(view) {
            val tvInitials: android.widget.TextView = view.findViewById(R.id.tvInitials)
            val tvName: android.widget.TextView = view.findViewById(R.id.tvDriverName)
            val tvPhone: android.widget.TextView = view.findViewById(R.id.tvDriverPhone)
            val tvVehicle: android.widget.TextView = view.findViewById(R.id.tvVehicleType)
            val tvDistance: android.widget.TextView = view.findViewById(R.id.tvDistance)
            val llCall: android.widget.LinearLayout = view.findViewById(R.id.llCall)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(layoutInflater.inflate(R.layout.item_driver_card, parent, false))
        override fun getItemCount() = drivers.size
        override fun onBindViewHolder(holder: VH, position: Int) {
            val driver = drivers[position]
            holder.tvInitials.text = driver.name.take(2).uppercase()
            holder.tvInitials.background.setColorFilter(android.graphics.Color.parseColor(avatarColors[Math.abs(driver.name.hashCode()) % avatarColors.size]), android.graphics.PorterDuff.Mode.SRC_IN)
            holder.tvName.text = driver.name; holder.tvPhone.text = driver.phone
            holder.tvVehicle.text = "${getVehicleEmoji(driver.vehicle_type)} ${driver.vehicle_type ?: "N/A"}"
            holder.tvDistance.text = "${driver.distance} km"
            holder.itemView.setOnClickListener { onSelect(driver) }
            holder.llCall.setOnClickListener { if (driver.phone.isNotEmpty()) startActivity(Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:${driver.phone}"))) }
        }
    }

    // ── Audio ──
    private fun startResording() {
        if (startRecording) { countDownTimer?.cancel(); countDownTimer?.onFinish(); startRecording = false; return }
        if (!checkPermission()) { requestPermissions(); return }
        val cw = ContextWrapper(applicationContext)
        val directory = cw.getDir("imageDir", Context.MODE_PRIVATE)
        AudioSavePathInDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) "${directory.absolutePath}/${CreateRandomAudioFileName(5)}audioRecording.m4a"
        else "${Environment.getExternalStorageDirectory().absolutePath}/${CreateRandomAudioFileName(5)}audioRecording.m4a"
        mediaRecorderReady()
        try {
            startRecording = true; mediaRecorder!!.prepare(); mediaRecorder!!.start()
            countDownTimer = object : CountDownTimer(300000, 1000) {
                override fun onTick(millisUntilFinished: Long) { elapsedTime = 300 - (millisUntilFinished / 1000); binding.timer.text = String.format(Locale.US, "%02d:%02d", elapsedTime / 60, elapsedTime % 60) }
                override fun onFinish() { startRecording = false; binding.timer.text = "05:00"; mediaRecorder?.let { try { it.stop() } catch (e: RuntimeException) { it.release(); mediaRecorder = null; return }; val file = File(AudioSavePathInDevice ?: return); if (file.exists()) { pendingUploadType = currentRecordingType; authViewModel.uploadFiles(prepareFilePart("media", file)) } } }
            }.start()
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun mediaRecorderReady() {
        mediaRecorder = MediaRecorder()
        try { mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC); mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC); mediaRecorder!!.setAudioEncodingBitRate(128000); mediaRecorder!!.setAudioSamplingRate(44100); mediaRecorder!!.setOutputFile(AudioSavePathInDevice) } catch (e: Exception) { e.printStackTrace() }
    }

    private fun CreateRandomAudioFileName(string: Int): String { val sb = StringBuilder(string); repeat(string) { sb.append(RandomAudioFileName[random!!.nextInt(RandomAudioFileName.length)]) }; return sb.toString() }

    fun checkPermission(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    else ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions() { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RecordAudioActivity.RequestPermissionCode) else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO), RecordAudioActivity.RequestPermissionCode) }

    private fun parseLatLngFromMapsUrl(url: String): Pair<String, String>? {
        if (url.isEmpty()) return null
        try { listOf(Regex("@(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)"), Regex("[?&]q=(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)"), Regex("destination=(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)")).forEach { pattern -> pattern.find(url)?.let { return Pair(it.groupValues[1], it.groupValues[2]) } } } catch (e: Exception) { Log.w("ManualOrder", "parseLatLng: ${e.message}") }
        return null
    }

    private fun getEnglishString(context: Context, resId: Int): String { val config = Configuration(context.resources.configuration); config.setLocale(Locale.ENGLISH); return context.createConfigurationContext(config).resources.getString(resId) }

    private fun showAlert(message: String) { AlertDialog.Builder(this).setTitle("Alerte").setMessage(message).setPositiveButton("OK") { d, _ -> d.dismiss() }.show() }

    companion object { const val RequestPermissionCode = 101 }
}