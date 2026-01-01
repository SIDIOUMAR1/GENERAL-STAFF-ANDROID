package com.genralstaff.home.ui

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.genralstaff.R
import com.genralstaff.adapter.ChatAdapter
import com.genralstaff.databinding.ActivityChatBinding
import com.genralstaff.databinding.CantactUserShopBinding
import com.genralstaff.network.ErrorType
import com.genralstaff.responseModel.GetChatMessagesResponse
import com.genralstaff.sockets.SocketManager
import com.genralstaff.utils.CustomProgressDialog
import com.genralstaff.utils.GoogleMapsAPI
import com.genralstaff.utils.ImagePickerActivityUtility
import com.genralstaff.utils.MyApplication
import com.genralstaff.utils.MyApplication.Companion.prefs
import com.genralstaff.utils.RecordAudioActivity
import com.genralstaff.utils.Utils
import com.genralstaff.utils.getDistanceTo
import com.genralstaff.utils.hideKeyboard
import com.genralstaff.utils.makePhoneCall
import com.genralstaff.utils.prepareFilePart
import com.genralstaff.utils.sessionExpire
import com.genralstaff.viewmodel.AuthViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.GsonBuilder

import com.rygelouv.audiosensei.player.AudioSenseiListObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.Locale
import java.util.Random


class ChatActivity : ImagePickerActivityUtility(), SocketManager.Observer {
    private lateinit var binding: ActivityChatBinding
    private lateinit var socketManager: SocketManager
    private lateinit var progressDialog: CustomProgressDialog

    private var type = "1"
    private var shopId = ""
    private var shopPhone = ""
    private var shopName = ""
    private var userType = ""
    private var otherUserId = ""
    private var otherUserName = ""
    private var otherUserImage = ""
    private var elapsedTime: Long = 0
    private var phoneNumber = ""
    private var room_id = ""


    var startRecording = false
    var RandomAudioFileName = "ABCDEFGHIJKLMNOP"

    var mp: MediaPlayer? = null
    var playStatus = "0"
    var AudioSavePathInDevice: String? = null
    var countDownTimer: CountDownTimer? = null
    var mediaRecorder: MediaRecorder? = null
    var random: Random? = null
    private val activityScope = CoroutineScope(Dispatchers.Main)
    private lateinit var authViewModel: AuthViewModel
    override fun selectedImage(imagePath: String?, code: Int?) {
        type = "2"
        val imagePart: MultipartBody.Part =
            prepareFilePart("media", File(imagePath))
        authViewModel.uploadFiles(imagePart)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

//         Register lifecycle to the AudioSenseiListObserver
        AudioSenseiListObserver.getInstance().registerLifecycle(this@ChatActivity.lifecycle)
        checkPermission()
        initializeViews()
        initializeSockets()
        setupListeners()
//        fetchChatList()
        progressDialog.show(this)
        displayChatMessages()

        viewModelSetupAndResponse()
        random = Random()
        binding.ivMic!!.setOnClickListener {

            binding.rlRecordView.visibility = View.VISIBLE
            binding.rlChatView.visibility = View.GONE
            Glide.with(this)
                .load(R.drawable.audiogif)
                .into(binding.ivGif!!)
            startResording()
        }
        binding.ivCheck!!.setOnClickListener {
            Glide.with(this).clear(binding.ivGif!!)
            binding.ivGif!!.setImageDrawable(null) // Optionally clear the ImageView content

            binding.rlRecordView.visibility = View.GONE
            binding.rlChatView.visibility = View.VISIBLE
            if (mediaRecorder != null) {
                mediaRecorder!!.stop()
            }
            startRecording = false
            countDownTimer!!.cancel()

            val file: File = File(AudioSavePathInDevice!!)
            if (file.exists()) {
                val audioPath = AudioSavePathInDevice!!
                type = "3"
                val imagePart: MultipartBody.Part =
                    prepareFilePart("media", File(audioPath))
                authViewModel.uploadFiles(imagePart)
                AudioSavePathInDevice = ""
            }
        }

        binding.ivCross.setOnClickListener {
            Glide.with(this).clear(binding.ivGif!!)
            binding.ivGif!!.setImageDrawable(null) // Optionally clear the ImageView content
            binding.rlRecordView.visibility = View.GONE
            binding.rlChatView.visibility = View.VISIBLE
            if (mediaRecorder != null) {
                mediaRecorder!!.stop()

            }
            startRecording = false
            countDownTimer!!.cancel()
            startRecording = false
            binding.timer!!.text = "00:00"

        }
        binding.ivBack.setOnClickListener { finish() }

    }

    private fun initializeViews() {
        progressDialog = CustomProgressDialog()
        if (intent.getStringExtra("shopId") != null) {
            shopId = intent.getStringExtra("shopId").toString()
        }
        phoneNumber = intent.getStringExtra("phone_no").toString()
        if (intent.getStringExtra("shopPhone") != null) {
            shopPhone = intent.getStringExtra("shopPhone").toString()
        }
        Log.e("initializeViews: ", phoneNumber)
        userType = intent.getStringExtra("userType").toString()
        otherUserId = intent.getStringExtra("otherUserId").toString()
        otherUserName = intent.getStringExtra("otherUserName").toString()
        otherUserImage = intent.getStringExtra("otherUserImage").toString()
        shopName = intent.getStringExtra("shopName") ?: ""
        Log.e("initializeViews: ", shopName)
        if (shopName == "null") {
            binding.tvName.text = "$otherUserName"
        } else {
            binding.tvName.text = "$otherUserName-$shopName"

//

        }
        binding.tvOrder.setOnClickListener {
            startActivity(
                Intent(this, OrderHistoryActivity::class.java)
                    .putExtra("type", "orders")
                    .putExtra("userId", otherUserId.toString())
            )
        }
        if (userType == "2") {
            // driver
            binding.ivWhatsApp.visibility = View.GONE
            binding.ivCall.visibility = View.GONE
            binding.tvDistance.visibility = View.INVISIBLE
            binding.tvOrder.visibility = View.INVISIBLE
            binding.ivBoost.visibility = View.GONE
        } else {
            // user
            binding.ivWhatsApp.visibility = View.VISIBLE
            binding.tvOrder.visibility = View.VISIBLE
            binding.tvDistance.visibility = View.VISIBLE
            binding.ivCall.visibility = View.VISIBLE
            binding.ivBoost.visibility = View.VISIBLE
        }

        binding.ivBoost.setOnClickListener {
            val map = HashMap<String, String>()
            map["user_id"] = otherUserId
            map["shop_id"] = shopId
            authViewModel.checkOrder(map)


        }
    }

    private fun initializeSockets() {
        socketManager = MyApplication.mInstance?.getSocketManager()!!
        socketManager.init()
        socketManager.onRegister(this)
        socketManager.getMessageListner()
        socketManager.readMessageListener()


    }

    companion object {
        const val RequestPermissionCode = 100
    }

    private fun openWhatsApps(phone: String) {

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data =
            Uri.parse("http://api.whatsapp.com/send?phone=$phone")
        startActivity(intent)

    }

    private fun contactDialog(s: String) {
        // Create a dialog and set the content view
        val dialog = Dialog(this)
        val binding: CantactUserShopBinding =
            CantactUserShopBinding.inflate(layoutInflater) // Use the generated binding class
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Set window properties
        val window = dialog.window
        window?.setGravity(Gravity.BOTTOM)
        window?.setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        // Use binding to access views
        binding.tvContactUser.setOnClickListener {
            if (s == "call") {
                makePhoneCall(phoneNumber, this@ChatActivity)

            } else {
                openWhatsApps(phoneNumber)

            }


        }
        // Use binding to access views
        binding.tvContactShop.setOnClickListener {
            if (s == "call") {
                makePhoneCall(shopPhone, this@ChatActivity)

            } else {
                openWhatsApps(shopPhone)

            }

        }

        // Set the cancel button click listener
        binding.tvCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Show the dialog
        dialog.show()
    }

    private fun setupListeners() {
        binding.ivImage.setOnClickListener {
            getImage(this, 0)
        }
        binding.ivWhatsApp.setOnClickListener {
            if (intent.getStringExtra("shopPhone") == null) {
                openWhatsApps(phoneNumber)

            } else {

                contactDialog("whatsApp")
            }
        }
        binding.ivCall.setOnClickListener {
            if (intent.getStringExtra("shopPhone") == null) {
                makePhoneCall(phoneNumber, this)

            } else {
                contactDialog("call")
            }
        }
//        binding.ivMic.setOnClickListener {
//            val intent = Intent(this, RecordAudioActivity::class.java)
//            startActivityForResult(intent, RequestPermissionCode)
//        }
        binding.ivSend.setOnClickListener {

            when {


                binding.edSearch.text.toString().trim().isEmpty() -> {
                    Toast.makeText(
                        applicationContext.applicationContext,
                        getString(R.string.please_enter_something),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    if (Utils.internetAvailability(this)) {
                        type = "1"
                        val userId = prefs?.getString("userId")
                        val jsonObject = JSONObject()
                        jsonObject.put("message", binding.edSearch.text.toString().trim())
                        jsonObject.put("receiver_id", otherUserId.toInt())
                        jsonObject.put(
                            "message_type",
                            type.toInt()
                        )  //1- simple message,2,image,3 audio
                        jsonObject.put("sender_id", userId?.toInt())
                        if (shopId.isNotEmpty()) {
                            jsonObject.put("shop_id", shopId.toInt())
                        }
                        socketManager.send_message(jsonObject)
                        Log.e("jsonObjectmessage: ", jsonObject.toString())
                        binding.edSearch.setText("")
                        hideKeyboard(this)
                    }
                }
            }
        }

    }


    private fun fetchChatList() {
        if (Utils.internetAvailability(this)) {
            val userId = MyApplication.prefs?.getString("userId")
            Log.e("userId", userId.toString())
            val jsonObjects = JSONObject().apply {
                put("receiver_id", otherUserId.toInt())
                put("sender_id", userId!!.toInt())
                if (shopId.isNotEmpty()) {
                    put("shop_id", shopId.toInt())
                }
            }
            Log.e("jsonObjects: ", jsonObjects.toString())

            socketManager.getChatList(jsonObjects)
        } else {
            Utils.showToast(this, getString(R.string.no_internet_connection))
        }
    }

    override fun onResponseArray(event: String, args: JSONArray) {
        when (event) {
            SocketManager.get_message_list -> handleChatListResponse(args)
        }
    }

    private fun handleChatListResponse(args: JSONArray) {
        activityScope.launch {
            val gson = GsonBuilder().create()
            val chatItemList =
                gson.fromJson(args.toString(), Array<GetChatMessagesResponse.Message>::class.java)
            val chatItemArrayList = ArrayList(chatItemList.toList())
            Log.e("chatItemArrayList: ", chatItemArrayList.toString())
            list.clear()
            list.addAll(chatItemArrayList)

            if (list.isNullOrEmpty() || list.size == 0) {
                binding.rvChat.visibility = View.GONE
                binding.llNoNewRequest.visibility = View.VISIBLE
            } else {
                room_id = list[0].room_id.toString()
// Get the sender ID for the last item in the list
                val lastItem =
                    list.lastOrNull()  // This safely gets the last item or null if the list is empty

// Ensure lastItem is not null before proceeding
                if (lastItem != null) {
                    val lastSenderID = lastItem.sender_id?.toString()
                        ?: ""  // Convert last sender ID to string and handle null

                    // Check if otherUserId matches the sender ID of the last item
                    if (prefs?.getString("userId")?.toInt() != lastSenderID.toInt()) {
                        callReadMessageSocket()  // Call the function if sender IDs match
                    }
                } else {
                    // Handle the case when the list is empty or lastItem is null
                    Log.e("ListError", "List is empty or lastItem is null")
                }
                binding.rvChat.visibility = View.VISIBLE
                binding.llNoNewRequest.visibility = View.GONE
                list.reverse()
                messageAdapter.updateList(list)

                if (list.size > 0) {
                    binding.rvChat.scrollToPosition(list.size - 1)
                }
            }


            progressDialog.hide()
        }
    }

    private lateinit var messageAdapter: ChatAdapter

    private fun displayChatMessages() {

        messageAdapter = ChatAdapter(this, list, otherUserImage)
        binding.rvChat.adapter = messageAdapter

    }

    var user_latitude = ""
    var user_longitude = ""
    var latitudeShop = ""
    var longitudeShop = ""
    private var list = ArrayList<GetChatMessagesResponse.Message>()
    private fun handleChatListObjectResponse(args: JSONObject) {
        activityScope.launch {
            val gson = GsonBuilder().create()
            val response = gson.fromJson(args.toString(), GetChatMessagesResponse::class.java)
//            val formattedDistance = String.format(Locale.US, "%.1f Km", response.room.distance)
            user_latitude = response.room.user_latitude ?: ""
            user_longitude = response.room.user_longitude ?: ""
            latitudeShop = response.room.shop_latitude ?: ""
            longitudeShop = response.room.shop_longitude ?: ""
            val formattedDistance = response.room.distance?.toDoubleOrNull()?.let {
                getString(R.string.approx)+ " "+ String.format(Locale.US, "%.1f Km", it)
            } ?: ""


            binding.tvDistance.text=  formattedDistance
            list.clear()
            list.addAll(response.list)

            if (list.isEmpty()) {
                binding.rvChat.visibility = View.GONE
                binding.llNoNewRequest.visibility = View.VISIBLE
            } else {
                room_id = list[0].room_id.toString()

                val lastItem = list.lastOrNull()
                lastItem?.let {
                    val lastSenderID = it.sender_id.toString()
                    if (prefs?.getString("userId")?.toInt() != lastSenderID.toInt()) {
                        callReadMessageSocket()
                    }
                }

                binding.rvChat.visibility = View.VISIBLE
                binding.llNoNewRequest.visibility = View.GONE
                list.reverse()
                messageAdapter.updateList(list)

                binding.rvChat.scrollToPosition(list.size - 1)
            }

            progressDialog.hide()
        }
    }

    override fun onResponse(event: String, args: JSONObject) {
        // Handle individual JSON object responses if needed

        when (event) {
            SocketManager.get_message_list -> handleChatListObjectResponse(args)

            SocketManager.send_message -> {
                activityScope.launch {
                    progressDialog.hide()
                    val response =
                        Gson().fromJson(
                            args.toString(),
                            GetChatMessagesResponse.Message::class.java
                        )
                    val senderID = response.sender_id

                    if (otherUserId.toInt() == senderID) {
                        callReadMessageSocket()
                    }
                    if (prefs?.getString("userId")!!
                            .toInt() == senderID || otherUserId.toInt() == senderID
                    ) {
                        list.add(response)
                        if (list.isNullOrEmpty() || list.size == 0) {
                            binding.rvChat.visibility = View.GONE
                            binding.llNoNewRequest.visibility = View.VISIBLE
                        } else {
                            binding.rvChat.visibility = View.VISIBLE
                            binding.llNoNewRequest.visibility = View.GONE

                        }

                        // Notify only the newly added item
                        messageAdapter.notifyItemInserted(list.size - 1)

                        // Scroll to the latest message
                        binding.rvChat.scrollToPosition(list.size - 1)

//                        if (list.isNullOrEmpty() || list.size == 0) {
//                            binding.rvChat.visibility = View.GONE
//                            binding.llNoNewRequest.visibility = View.VISIBLE
//                        } else {
//                            binding.rvChat.visibility = View.VISIBLE
//                            binding.llNoNewRequest.visibility = View.GONE
////                            list.reverse()
//                            messageAdapter.updateList(list)
//
//                            if (list.size > 0) {
//                                binding.rvChat.scrollToPosition(list.size - 1)
//                            }
//                        }

                    }

                }
            }

            SocketManager.read_chat -> {
                activityScope.launch {
                    //mark as read all the messages
                    list.forEach {
                        it.is_read = 1
                    }

                    messageAdapter.updateList(list)


                }
            }

        }

    }

    private fun callReadMessageSocket() {
        //mark as read all the messages
        val jsonObject = JSONObject().apply {
            put("room_id", room_id)
            put("sender_id", prefs?.getString("userId").toString())
            put("receiver_id", otherUserId.toString())
        }
        socketManager?.read_chat(jsonObject)
    }

    override fun onResume() {
        super.onResume()
        initializeSockets()
        fetchChatList()
        prefs?.saveString("STATUS_CHAT", "true" + otherUserName)
    }

    override fun onStop() {
        super.onStop()
        prefs?.saveString("STATUS_CHAT", "false")
    }

    override fun onDestroy() {
        super.onDestroy()
        prefs?.saveString("STATUS_CHAT", "false")
    }

    override fun onError(event: String, vararg args: Array<*>) {
        // Handle errors
    }

    override fun onBlockError(event: String, args: String) {
        // Handle block errors
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
        authViewModel.onUploadProfileResponse().observe(this) { response ->
            response?.let {
                if (it.code == 200) {
                    val userId = prefs?.getString("userId")
                    val jsonObject = JSONObject()
                    jsonObject.put("message", it.body.media.toString())
                    jsonObject.put("receiver_id", otherUserId.toInt())
                    jsonObject.put(
                        "message_type",
                        type.toInt()
                    )  //1- simple message,2,image,3 audio
                    jsonObject.put("sender_id", userId?.toInt())
                    if (shopId.isNotEmpty()) {
                        jsonObject.put("shop_id", shopId.toInt())
                    }
                    socketManager.send_message(jsonObject)

                }
            }
        }
        authViewModel.onCheckOrderResponse().observe(this) { response ->
            response?.let {
                if (it.code == 200) {
                    if (it.body.status == 1) {
                        startActivity(
                            Intent(this, AddOrderActivity::class.java)
                                .putExtra("userId", otherUserId)
                                .putExtra("shopId", shopId)
                                .putExtra("shopName", shopName)
                                .putExtra("user_latitude", user_latitude)
                                .putExtra("user_longitude", user_longitude)
                                .putExtra("latitudeShop", latitudeShop)
                                .putExtra("longitudeShop", longitudeShop)
                        )
                    } else {
                        checkOrder()
                    }
                }
            }
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
                    type = "3"
                    val imagePart: MultipartBody.Part =
                        prepareFilePart("media", File(audioPath))
                    authViewModel.uploadFiles(imagePart)

                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
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
                                        type = "3"
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
                this@ChatActivity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RecordAudioActivity.RequestPermissionCode
            )

        } else {

            ActivityCompat.requestPermissions(
                this@ChatActivity,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO
                ),
                RecordAudioActivity.RequestPermissionCode
            )
        }

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

    private fun checkOrder() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.checkorder_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window!!.setGravity(Gravity.CENTER)
//        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimationPhone

        dialog.show()

        val tvYesL = dialog.findViewById<TextView>(R.id.btnYes)
        val tvNoL = dialog.findViewById<TextView>(R.id.btnNo)


        tvYesL.setOnClickListener {

            dialog.dismiss()
            startActivity(
                Intent(this, AddOrderActivity::class.java)
                    .putExtra("userId", otherUserId)
                    .putExtra("shopId", shopId)
                    .putExtra("shopName", shopName)
                    .putExtra("user_latitude", user_latitude)
                    .putExtra("user_longitude", user_longitude)
                    .putExtra("latitudeShop", latitudeShop)
                    .putExtra("longitudeShop", longitudeShop)
            )
        }
        tvNoL.setOnClickListener {
            dialog.dismiss()
        }
    }

}
