package com.genralstaff.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.genralstaff.R
import com.genralstaff.base.LANGUAGE
import com.genralstaff.base.profileBaseUrl
import com.genralstaff.databinding.ActivityHomeBinding
import com.genralstaff.home.ui.ChatActivity
import com.genralstaff.sockets.SocketManager
import com.genralstaff.utils.MyApplication
import com.genralstaff.utils.getCity
import com.genraluser.utils.LocationUpdateUtilityActivity
import com.google.android.gms.maps.model.LatLng

class HomeActivity : LocationUpdateUtilityActivity() {

    private lateinit var binding: ActivityHomeBinding
    var status = 0
    var type = ""
    var profilePic = ""
    var senderName = ""
    var shopPhone = ""
    var senderId = ""
    var shop_id = ""
    var phone_no = ""
    var sender_type = ""
    var shopName = ""
    var langu = 0
    var otherUserLong = ""
    var otherUserLat = ""
    var shopLat = ""
    var shopLong = ""

    override fun updatedLatLng(lat: Double, lng: Double) {
        MyApplication.prefs!!.saveString("lat", lat.toString())
        MyApplication.prefs!!.saveString("lng", lng.toString())
        MyApplication.prefs!!.saveString("address", getCity(lat.toString(), lng.toString(), this))


    }

    override fun onChangedLocation(lat: Double, lng: Double) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getLiveLocation(this)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val lang = MyApplication.prefs!!.getPrefrenceLanguage(LANGUAGE, "en").toString()

        langu = when (lang) {
            "en" -> {
                0
            }

            "fr" -> {
                1

            }

            else -> {
                2

            }
        }
        initializeSockets()
        if (intent.getStringExtra("type") != null) {
            type = intent.getStringExtra("type").toString()
            if (type == "1") {
                phone_no = intent.getStringExtra("phone_no").toString()
                profilePic = intent.getStringExtra("otherUserImage").toString()
                senderName = intent.getStringExtra("otherUserName").toString()
                senderId = intent.getStringExtra("otherUserId").toString()
                sender_type = intent.getStringExtra("sender_type").toString()
                shopPhone = intent.getStringExtra("shopPhone").toString()
                shop_id = intent.getStringExtra("shop_id").toString()
                shopName = intent.getStringExtra("shop_name").toString()
                otherUserLong = intent.getStringExtra("otherUserLong").toString()
                otherUserLat = intent.getStringExtra("otherUserLat").toString()
                shopLat = intent.getStringExtra("shopLat").toString()
                shopLong = intent.getStringExtra("shopLong").toString()
                var driverLatLng: LatLng? = null
                var shopLatLng: LatLng? = null
                driverLatLng =
                    LatLng(
                        otherUserLat.toDouble(),
                        otherUserLong.toDouble()
                    )
                shopLatLng = LatLng(
                    shopLat.toDouble(),
                    shopLong.toDouble()
                )

                startActivity(
                    Intent(this, ChatActivity::class.java)
                        .putExtra("shopId", shop_id)
                        .putExtra("userType", sender_type)
                        .putExtra("shopPhone", shopPhone)
                        .putExtra("otherUserId", senderId)
                        .putExtra("otherUserName", senderName)
                        .putExtra("shopName", shopName)
                        .putExtra("phone_no", phone_no)
                        .putExtra("otherUserImage", profileBaseUrl + profilePic)
                        .putExtra("driverLatLng", driverLatLng)
                        .putExtra("shopLatLng", shopLatLng)
                )
            }
        }
        selectTab(status)

        val navController = findNavController(R.id.nav_host_fragment_activity_home)
        navController.navigate(R.id.navigation_home)

        binding.rlHome.setOnClickListener {
            status = 0
            selectTab(status)
            navController.navigate(R.id.navigation_home)
        }
        binding.rlMessage.setOnClickListener {
            status = 1
            selectTab(status)
            navController.navigate(R.id.messagesFragment)

        }
        binding.rlNotification.setOnClickListener {
            status = 2
            selectTab(status)
            navController.navigate(R.id.navigation_notifications)

        }


        binding.rlSettings.setOnClickListener {
            status = 3
            selectTab(status)
            navController.navigate(R.id.settingsFragment)

        }
        // Adding a callback to handle back press
        val callback = object : OnBackPressedCallback(true /* enabled by default */) {
            override fun handleOnBackPressed() {
                // Back is pressed... Finishing the activity
                if (status != 0) {
                    status = 0
                    navController.navigate(R.id.navigation_home)

                    when (langu) {
                        0 -> {
                            // en
                            binding.ivHome.setImageResource(R.drawable.ic_home_select_p)
                        }

                        1 -> {
                            // fr
                            binding.ivHome.setImageResource(R.drawable.home_french)
                        }

                        else -> {
                            // ar
                            binding.ivHome.setImageResource(R.drawable.home_arabic)
                        }
                    }
                    binding.ivNotification.setImageResource(R.drawable.notification_default)
                    binding.ivMessage.setImageResource(R.drawable.message_default)
                    binding.ivSettings.setImageResource(R.drawable.setting_default)

                } else {
                    finish()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

    }

    private lateinit var socketManager: SocketManager

    private fun initializeSockets() {
        val application = MyApplication.mInstance
        if (application != null) {
            socketManager = application.getSocketManager()!!
            socketManager.init()

        } else {
            // Handle the case where the application instance is null
            // This should not happen in a normal scenario
            throw IllegalStateException("Application instance is null")
        }
    }

    private fun selectTab(status: Int) {
        when (status) {
            0 -> {
                when (langu) {
                    0 -> {
                        // en
                        binding.ivHome.setImageResource(R.drawable.ic_home_select_p)
                    }

                    1 -> {
                        // fr
                        binding.ivHome.setImageResource(R.drawable.home_french)
                    }

                    else -> {
                        // ar
                        binding.ivHome.setImageResource(R.drawable.home_arabic)
                    }
                }
                binding.ivNotification.setImageResource(R.drawable.notification_default)
                binding.ivMessage.setImageResource(R.drawable.message_default)
                binding.ivSettings.setImageResource(R.drawable.setting_default)

            }

            1 -> {
                binding.ivHome.setImageResource(R.drawable.home_default_ic)
                binding.ivNotification.setImageResource(R.drawable.notification_default)
                when (langu) {
                    0 -> {
                        // en
                        binding.ivMessage.setImageResource(R.drawable.ic_messages_p)
                    }

                    1 -> {
                        // fr
                        binding.ivMessage.setImageResource(R.drawable.message_french)
                    }

                    else -> {
                        // ar
                        binding.ivMessage.setImageResource(R.drawable.message_arabic)
                    }
                }
                binding.ivSettings.setImageResource(R.drawable.setting_default)
            }

            2 -> {
                binding.ivHome.setImageResource(R.drawable.home_default_ic)

                when (langu) {
                    0 -> {
                        // en
                        binding.ivNotification.setImageResource(R.drawable.ic_notification_p)
                    }

                    1 -> {
                        // fr
                        binding.ivNotification.setImageResource(R.drawable.notification_french)
                    }

                    else -> {
                        // ar
                        binding.ivNotification.setImageResource(R.drawable.notification_arabic)
                    }
                }
                binding.ivMessage.setImageResource(R.drawable.message_default)
                binding.ivSettings.setImageResource(R.drawable.setting_default)

            }

            3 -> {

                binding.ivHome.setImageResource(R.drawable.home_default_ic)
                binding.ivNotification.setImageResource(R.drawable.notification_default)
                binding.ivMessage.setImageResource(R.drawable.message_default)
                when (langu) {
                    0 -> {
                        // en
                        binding.ivSettings.setImageResource(R.drawable.ic_setting_p)
                    }

                    1 -> {
                        // fr
                        binding.ivSettings.setImageResource(R.drawable.setting_french)
                    }

                    else -> {
                        // ar
                        binding.ivSettings.setImageResource(R.drawable.setting_arabic)
                    }
                }
            }


        }
    }

}