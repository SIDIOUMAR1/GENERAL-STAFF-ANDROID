package com.genralstaff.home.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.akexorcist.googledirection.GoogleDirection
import com.akexorcist.googledirection.constant.AvoidType
import com.akexorcist.googledirection.constant.TransportMode
import com.akexorcist.googledirection.model.Direction
import com.akexorcist.googledirection.util.execute
import com.bumptech.glide.Glide
import com.genralstaff.MainActivity
import com.genralstaff.R
import com.genralstaff.base.imageURL
import com.genralstaff.base.profileBaseUrl
import com.genralstaff.databinding.ActivityTackOrderBinding
import com.genralstaff.home.HomeActivity
import com.genralstaff.network.ErrorType
import com.genralstaff.sockets.SocketManager
import com.genralstaff.utils.CustomProgressDialog
import com.genralstaff.utils.MyApplication
import com.genralstaff.utils.Utils
import com.genralstaff.utils.getCity
import com.genralstaff.utils.printDate
import com.genralstaff.utils.sessionExpire
import com.genralstaff.viewmodel.AuthViewModel
import com.genraluser.utils.LocationUpdateUtilityActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TrackOrderActivity : LocationUpdateUtilityActivity(), SocketManager.Observer,
    OnMapReadyCallback {
    lateinit var binding: ActivityTackOrderBinding
    private lateinit var socketManager: SocketManager
    private var mMap: GoogleMap? = null
    private lateinit var mapFragment: SupportMapFragment
    var id = ""
    var lat = ""
    var long = ""
    var types = ""
    var status = 0
    var driver_status = 0
    private lateinit var authViewModel: AuthViewModel
private val progressDialog by lazy { CustomProgressDialog() }

    override fun updatedLatLng(lat: Double, lng: Double) {
        MyApplication.prefs!!.saveString("lat", lat.toString())
        MyApplication.prefs!!.saveString("lng", lng.toString())
        MyApplication.prefs!!.saveString("address", getCity(lat.toString(), lng.toString(), this))
        this.lat = lat.toString()
        long = lng.toString()
        viewModelSetupAndResponse()

    }

    override fun onChangedLocation(lat: Double, lng: Double) {
        this.lat = lat.toString()
        long = lng.toString()
        MyApplication.prefs!!.saveString("lat", lat.toString())
        MyApplication.prefs!!.saveString("lng", lng.toString())
        MyApplication.prefs!!.saveString("address", getCity(lat.toString(), lng.toString(), this))
        updateLocationSocket()
        driverLatLng = LatLng(this.lat.toDouble(), long.toDouble())
        if (dropLatLng != null) {
            when (status) {
                0 -> {

                    getDirection(driverLatLng!!, dropLatLng!!, "shop")

                }

                1 -> {
//                            1-> navigate,2-> picked


                    if (types == "1") {
                        when (driver_status) {
                            0 -> {

                                getDirection(driverLatLng!!, dropLatLng!!, "user")
                                //  create direction from driver to drop means user

                            }


                            1 -> {

                                getDirection(driverLatLng!!, dropLatLng!!, "user")
                                //  create direction from driver to drop means user

                            }

                            else -> {


                                getDirection(driverLatLng!!, dropLatLng!!, "user")
                                //   create direction from driver to drop means user

                            }
                        }

                    } else {
                        when (driver_status) {
                            0 -> {

                                getDirection(driverLatLng!!, dropLatLng!!, "shop")
//                          create direction from driver to shop
                            }


                            1 -> {

                                getDirection(driverLatLng!!, dropLatLng!!, "user")
                                //  create direction from driver to drop means user

                            }

                            else -> {

                                getDirection(driverLatLng!!, dropLatLng!!, "user")
                                //   create direction from driver to drop means user

                            }
                        }

                    }


                }

            }
        }

    }

    private fun updateLocationSocket() {
        val userId = MyApplication.prefs?.getString("userId").toString()
        val jsonObject = JSONObject()
        jsonObject.put("address", "")
        jsonObject.put("latitude", lat)
        jsonObject.put("longitude", long)
        jsonObject.put("user_id", userId)
        jsonObject.put("other_user_id", otherUserId)
        socketManager.updateLocation(jsonObject)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTackOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.ivNav.setOnClickListener {
            mMap!!.setOnMapLoadedCallback {
                val currentLatLng = LatLng(this.lat.toDouble(), long.toDouble())
                mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 19f))
            }
        }
        progressDialog.show(this)
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this@TrackOrderActivity)
        id = intent.getStringExtra("id").toString()
        if (intent.getStringExtra("type") != null) {
            types = "1"
        }
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        initializeSockets()

        binding.btnPick.setOnClickListener {
            if (binding.btnPick.text.toString() == "Delivered") {
                val jsonObject = JSONObject()
                jsonObject.put("order_id", id)
                jsonObject.put("driver_status", 3)
                socketManager.driverStatusChange(jsonObject)

            } else {
                if (Utils.internetAvailability(this)) {
                    val jsonObject = JSONObject()
                    jsonObject.put("order_id", id)
                    jsonObject.put("driver_status", 1)
                    socketManager.driverStatusChange(jsonObject)
                }
            }
        }
        binding.btnNavigate.setOnClickListener {
            if (binding.btnNavigate.text.trim().toString() == "Navigate") {
                binding.btnNavigate.text = "Picked"
                if (Utils.internetAvailability(this)) {
                    val jsonObject = JSONObject()
                    jsonObject.put("order_id", id)
                    jsonObject.put("driver_status", 1)
                    socketManager.driverStatusChange(jsonObject)
                }
            } else if (binding.btnNavigate.text.trim().toString() == "Picked") {
                if (Utils.internetAvailability(this)) {
                    val jsonObject = JSONObject()
                    jsonObject.put("order_id", id)
                    jsonObject.put("driver_status", 2)
                    socketManager.driverStatusChange(jsonObject)
                }

            }

        }

    }

    var otherUserId = ""
    var driverLatLng: LatLng? = null
    var dropLatLng: LatLng? = null
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
        authViewModel.onOrderDetailResponse().observe(this) { response ->
            response?.let {
                if (it.code == 200) {
                    progressDialog.hide()
                    // 0- pending, 1- accepted, 2- completed
                    binding.llDetails.visibility = View.VISIBLE
                    otherUserId = it.body.user_id.toString()
                    status = it.body.status
                    driver_status = it.body.driver_status
                    when (status) {
                        0 -> {
                            Glide.with(this).load(profileBaseUrl + it.body.user_detail.profile_pic)
                                .placeholder(R.drawable.place_holder).into(binding.civProfile)
                            binding.tvName.text = it.body.user_detail.name
                            binding.tvProductName.text = it.body.product.name
                            binding.tvDateTime.text = printDate(it.body.created_at)
                            val driverPhone = it.body.user_detail.phone_no.toString()

                            binding.ivCall.setOnClickListener {
                                val intent = Intent(Intent.ACTION_DIAL)
                                intent.data = Uri.parse("tel:$driverPhone")
                                startActivity(intent)
                            }
                            val orderId = it.body.id.toString()
                            val otherUserName = it.body.user_detail.name ?: ""
                            val otherUserImage = profileBaseUrl + it.body.user_detail.profile_pic
                            val shopId =
                                it.body.shop_id.toString() // Initialize shopId here
                            binding.ivMessage.setOnClickListener {
                                startActivity(
                                    Intent(this, ChatActivity::class.java)
                                        .putExtra("otherUserId", otherUserId)
                                        .putExtra("otherUserName", otherUserName)
                                        .putExtra("otherUserImage", otherUserImage)

                                )
                            }
                            binding.btnAccept.setOnClickListener {
                                this.type = 1
                                if (Utils.internetAvailability(this)) {
                                    val userId = MyApplication.prefs?.getString("userId")
                                    val jsonObject = JSONObject()
                                    jsonObject.put("order_id", orderId)
                                    jsonObject.put("driver_id", userId?.toInt())
                                    jsonObject.put("status", 1)
                                    //(1 for accepted 2 for rejected)
                                    socketManager.acceptReject(jsonObject)

                                }
                            }
                            binding.btnReject.setOnClickListener {
                                this.type = 2
                                if (Utils.internetAvailability(this)) {
                                    val userId = MyApplication.prefs?.getString("userId")
                                    val jsonObject = JSONObject()
                                    jsonObject.put("order_id", orderId)
                                    jsonObject.put("driver_id", userId?.toInt())
                                    jsonObject.put("status", 2)
                                    //(1 for accepted 2 for rejected)
                                    socketManager.acceptReject(jsonObject)
                                }
                            }
                            //                                    create direction from driver to shop
                            driverLatLng = LatLng(lat.toDouble(), long.toDouble())
                            dropLatLng = LatLng(
                                it.body.shop.latitude.toDouble(),
                                it.body.shop.longitude.toDouble()
                            )
                            binding.tvShopNmaeAddress.text =
                                it.body.shop.name + "," + it.body.shop.location
                            getDirection(driverLatLng!!, dropLatLng!!, "shop")

                        }

                        1 -> {
//                            1-> navigate,2-> picked


                            if (types == "1") {
                                when (driver_status) {
                                    0 -> {
                                        binding.llBottom.visibility = View.GONE
                                        binding.btnNavigate.visibility = View.VISIBLE

                                        driverLatLng = LatLng(lat.toDouble(), long.toDouble())
                                        dropLatLng = LatLng(
                                            it.body.latitude.toDouble(),
                                            it.body.longitude.toDouble()
                                        )
                                        binding.tvShopNmaeAddress.text =
                                            it.body.shop.name + "," + it.body.shop.location
                                        getDirection(driverLatLng!!, dropLatLng!!, "user")
                                        //                                    create direction from driver to drop means user

                                    }


                                    1 -> {
                                        binding.llBottom.visibility = View.GONE
                                        binding.btnNavigate.visibility = View.VISIBLE

                                        driverLatLng = LatLng(lat.toDouble(), long.toDouble())
                                        dropLatLng = LatLng(
                                            it.body.latitude.toDouble(),
                                            it.body.longitude.toDouble()
                                        )
                                        getDirection(driverLatLng!!, dropLatLng!!, "user")
                                        //                                    create direction from driver to drop means user
                                        binding.tvShopNmaeAddress.text =
                                            it.body.shop.name + "," + it.body.shop.location
                                    }

                                    else -> {

                                        binding.llDetails.visibility = View.VISIBLE
                                        binding.llBottom.visibility = View.VISIBLE
                                        binding.llCommunication.visibility = View.GONE
                                        binding.llDate.visibility = View.GONE
                                        binding.btnNavigate.visibility = View.GONE
                                        binding.llFood.visibility = View.GONE
                                        binding.llButtons.visibility = View.INVISIBLE
                                        binding.btnPick.visibility = View.VISIBLE
                                        binding.btnPick.text = "Delivered"
                                        Glide.with(this)
                                            .load(profileBaseUrl + it.body.user_detail.profile_pic)
                                            .placeholder(R.drawable.place_holder)
                                            .into(binding.civProfile)
                                        binding.tvName.text = it.body.user_detail.name
                                        binding.tvShopNmaeAddress.text =
                                            it.body.shop.name + "," + it.body.shop.location
//                                        binding.tvDropOff.text = it.body.location
                                        driverLatLng = LatLng(lat.toDouble(), long.toDouble())
                                        dropLatLng = LatLng(
                                            it.body.latitude.toDouble(),
                                            it.body.longitude.toDouble()
                                        )
                                        getDirection(driverLatLng!!, dropLatLng!!, "user")
                                        //                                    create direction from driver to drop means user

                                    }
                                }

                            } else {
                                when (driver_status) {
                                    0 -> {
                                        binding.llCommunication.visibility = View.GONE
                                        binding.llDate.visibility = View.GONE
                                        binding.llFood.visibility = View.GONE
                                        binding.llButtons.visibility = View.INVISIBLE
                                        binding.btnPick.visibility = View.VISIBLE
                                        binding.tvName.text = it.body.shop.name
                                        binding.tvShopNmaeAddress.text =
                                            it.body.shop.name + "," + it.body.shop.location
//                                        binding.tvDropOff.text = it.body.location
                                        binding.civProfile.setImageResource(R.drawable.food_placeholder)
                                        Glide.with(this).load(imageURL + it.body.shop.image)
                                            .placeholder(R.drawable.place_holder)
                                            .into(binding.civProfile)
                                        driverLatLng = LatLng(lat.toDouble(), long.toDouble())
                                        dropLatLng = LatLng(it.body.shop.latitude.toDouble(), it.body.shop.longitude.toDouble())
                                        getDirection(driverLatLng!!, dropLatLng!!, "shop")
//                                    create direction from driver to shop
                                    }


                                    1 -> {
                                        binding.llBottom.visibility = View.GONE
                                        binding.btnNavigate.visibility = View.VISIBLE
                                        binding.tvShopNmaeAddress.text =
                                            it.body.shop.name + "," + it.body.shop.location
                                        driverLatLng = LatLng(lat.toDouble(), long.toDouble())
                                        dropLatLng = LatLng(
                                            it.body.latitude.toDouble(),
                                            it.body.longitude.toDouble()
                                        )
                                        getDirection(driverLatLng!!, dropLatLng!!, "user")
                                        //                                    create direction from driver to drop means user

                                    }
                                    else -> {
                                        binding.llDetails.visibility = View.VISIBLE
                                        binding.llBottom.visibility = View.VISIBLE
                                        binding.llCommunication.visibility = View.GONE
                                        binding.llDate.visibility = View.GONE
                                        binding.btnNavigate.visibility = View.GONE
                                        binding.llFood.visibility = View.GONE
                                        binding.llButtons.visibility = View.INVISIBLE
                                        binding.btnPick.visibility = View.VISIBLE
                                        binding.btnPick.text = "Delivered"
                                        Glide.with(this)
                                            .load(profileBaseUrl + it.body.user_detail.profile_pic)
                                            .placeholder(R.drawable.place_holder)
                                            .into(binding.civProfile)
                                        binding.tvName.text = it.body.user_detail.name
                                        binding.tvShopNmaeAddress.text =
                                            it.body.shop.name + "," + it.body.shop.location
//                                        binding.tvDropOff.text = it.body.location
                                        driverLatLng = LatLng(lat.toDouble(), long.toDouble())
                                        dropLatLng = LatLng(
                                            it.body.latitude.toDouble(),
                                            it.body.longitude.toDouble()
                                        )

                                        getDirection(driverLatLng!!, dropLatLng!!, "user")

                                        //                                    create direction from driver to drop means user

                                    }
                                }

                            }


                        }

                        2 -> {
                            stopLocationUpdates()
                            socketManager.unRegister(this)
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        }
                    }

                    updateLocationSocket()
                }
            }
        }
        val map = HashMap<String, String>()
        map["order_id"] = id
        authViewModel.orderDetail(map)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        socketManager.unRegister(this)
    }

    private var driverMarker: Marker? = null
    private var polyline: Polyline? = null

    private fun getDirection(driver: LatLng, dropOff: LatLng, s: String) {
        GoogleDirection.withServerKey(resources?.getString(R.string.googlePlaceKey_live)!!)
            .from(driver)
            .to(dropOff)
            .avoid(AvoidType.INDOOR)
            .transportMode(TransportMode.DRIVING)
            .execute(
                onDirectionSuccess = { direction: Direction? ->
                    if (direction != null && direction.isOK) {
                        val directionPositionList: ArrayList<LatLng> = ArrayList()

                        direction.routeList.forEach { route ->
                            route.legList.forEach { leg ->
                                directionPositionList.addAll(leg.directionPoint)
                            }
                        }
                        val totalDistanceMeters = direction.routeList[0].totalDistance.toDouble()
                        val distanceString = if (totalDistanceMeters < 1000) {
                            // Show distance in meters if less than or equal to 1000 meters
                            "${totalDistanceMeters.toInt()} meters"
                        } else {
                            // Convert to kilometers and show
                            "${metersToKilometers(totalDistanceMeters)} km"
                        }

                        Log.e("Distance", distanceString)

                        val time: Int = (direction.routeList[0].totalDuration / 60).toInt()
                        val hours = time / 60
                        val remainingMinutes = time % 60

                        calculateEstimatedTime(hours, remainingMinutes) { estimatedTimeString ->
                            binding.tvDropOff.text =
                                "Distance: $distanceString Estimated Time: $estimatedTimeString"
                        }

                        // Remove existing polyline if it exists
                        polyline?.remove()

                        // Remove existing driver marker if it exists
                        driverMarker?.remove()

                        // Add new polyline
                        polyline = mMap?.addPolyline(
                            PolylineOptions()
                                .addAll(directionPositionList)
                                .color(ContextCompat.getColor(this, R.color.black))
                                .width(4f)
                        )
                        if (s == "user") {
                            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(driver, 19f))

                        } else {

                            // Move camera to show both driver and drop-off locations with a margin around the driver's location
                            val padding =
                                resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._12sdp) // Adjust padding as needed
                            val boundsBuilder = LatLngBounds.Builder()
                            boundsBuilder.include(driver)
                            boundsBuilder.include(dropOff)
                            val bounds = boundsBuilder.build()

                            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                            mMap?.moveCamera(cameraUpdate)
                        }
                        // Add new driver marker
                        driverMarker = mMap?.addMarker(
                            MarkerOptions().position(driver)
                                .icon(
                                    BitmapDescriptorFactory.fromBitmap(
                                        getIcon(
                                            110,
                                            110,
                                            ResourcesCompat.getDrawable(
                                                resources,
                                                R.drawable.driver_loc_icon,
                                                null
                                            )
                                        )
                                    )
                                )
                                .flat(true)
                        )

                        // Add drop-off marker
                        val dropOffIcon =
                            if (s == "user") R.drawable.ic_lo else R.drawable.restaurant_tack_icon
                        mMap?.addMarker(
                            MarkerOptions().position(dropOff)
                                .icon(
                                    BitmapDescriptorFactory.fromBitmap(
                                        getIcon(
                                            90,
                                            90,
                                            ResourcesCompat.getDrawable(
                                                resources,
                                                dropOffIcon,
                                                null
                                            )
                                        )
                                    )
                                )
                        )


                    } else {
                        Utils.showErrorDialog(
                            this,
                            resources?.getString(R.string.something_went_wrong_while_getting_directions)!!
                        )
                    }
                },
                onDirectionFailure = {
                    Utils.showErrorDialog(
                        this,
                        resources?.getString(R.string.something_went_wrong_while_getting_directions)!!
                    )
                }
            )
    }


    private fun getIcon(height: Int, width: Int, bitmapDrawable: Drawable?): Bitmap {
        val bitMapDrawable = bitmapDrawable as BitmapDrawable
        val b = bitMapDrawable.bitmap
        return Bitmap.createScaledBitmap(b, width, height, false)

    }

    private fun initializeSockets() {
        socketManager = MyApplication.mInstance?.getSocketManager()!!
        socketManager.init()
        socketManager.onRegister(this)
        socketManager.acceptRejectListener()
        socketManager.driverStatusChangeListener()


    }

    // Function to convert meters to kilometers
    fun metersToKilometers(meters: Double): Double {
        return (meters / 1000).let { km ->
            (km * 10).toInt() / 10.0 // Round to 1 decimal place
        }
    }

    private fun calculateEstimatedTime(hours: Int, mins: Int, completion: (String) -> Unit) {
        // Create a Calendar instance and set it to the current time
        val calendar = Calendar.getInstance()
        val currentDate = Date()
        calendar.time = currentDate

        // Add travel time to the current time
        calendar.add(Calendar.HOUR_OF_DAY, hours)
        calendar.add(Calendar.MINUTE, mins)

        // Format the estimated time
        val dateFormatter = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        val estimatedTimeString = dateFormatter.format(calendar.time)

        completion(estimatedTimeString)
    }

    override fun onResume() {
        super.onResume()
        initializeSockets()

    }

    override fun onResponseArray(event: String, args: JSONArray) {

    }

    var type = 1
    private val activityScope = CoroutineScope(Dispatchers.Main)

    override fun onResponse(event: String, args: JSONObject) {
        when (event) {


            SocketManager.driver_accept_reject -> {
                activityScope.launch {
                    if (this@TrackOrderActivity.type == 2) {
                        startActivity(
                            Intent(this@TrackOrderActivity, MainActivity::class.java)
                        )
                        finishAffinity()
                    } else {
                        val map = HashMap<String, String>()
                        map["order_id"] = id
                        authViewModel.orderDetail(map)
                    }
                }
            }

            SocketManager.driver_change_status -> {
                activityScope.launch {
                    progressDialog.show(this@TrackOrderActivity)

                    val map = HashMap<String, String>()
                    map["order_id"] = id
                    authViewModel.orderDetail(map)
                }

            }

        }

    }

    override fun onError(event: String, vararg args: Array<*>) {
    }

    override fun onBlockError(event: String, args: String) {
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onMapReady(p0: GoogleMap) {
        p0?.let { map ->
            mMap = map
            // Your initialization code here

            mMap!!.uiSettings?.isMapToolbarEnabled = false
            mMap!!.uiSettings.isCompassEnabled = false
            mMap!!.uiSettings.isMyLocationButtonEnabled = false
            getLiveLocation(this)

        }
    }

}