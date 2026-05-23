package com.genralstaff.home.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isGone
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.genralstaff.R
import com.genralstaff.adapter.OrderItemsAdapter
import com.genralstaff.databinding.ActivityOrderHistoryBinding
import com.genralstaff.home.HomeActivity
import com.genralstaff.network.ErrorType
import com.genralstaff.responseModel.OrderHistoryResponse
import com.genralstaff.sockets.SocketManager
import com.genralstaff.utils.CustomProgressDialog
import com.genralstaff.utils.MyApplication
import com.genralstaff.utils.Utils
import com.genralstaff.utils.makePhoneCall
import com.genralstaff.utils.sessionExpire
import com.genralstaff.viewmodel.AuthViewModel
import com.google.android.gms.maps.model.LatLng

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject


class OrderHistoryActivity : AppCompatActivity(), SocketManager.Observer {
    var limit = 100
    var page = 1
    var ordersList = ArrayList<OrderHistoryResponse.Body.Data>()
    var type = "0"
    var user_id = ""
    private lateinit var authViewModel: AuthViewModel
private val progressDialog by lazy { CustomProgressDialog() }
    private lateinit var socketManager: SocketManager

    lateinit var binding: ActivityOrderHistoryBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeSockets()
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
        binding.ivFilter.setOnClickListener {
            showFilterPopup(binding.ivFilter)
        }


    }

    override fun onBackPressed() {
        if (intent.getStringExtra("types") != null) {
            val intent = Intent(this@OrderHistoryActivity, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish() // Finish the current activity to ensure it's not in the back stack
        } else {
            finish()
        }
        super.onBackPressed()

    }

    override fun onResume() {
        super.onResume()
        adapter = OrderItemsAdapter(this, ordersList, ordersList)
        binding.rvHistory.adapter = adapter
        viewModelSetupAndResponse()
    }

    var adapter: OrderItemsAdapter? = null

    @SuppressLint("SuspiciousIndentation")
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
        authViewModel.onOrderHistoryResponse().observe(this) { response ->
            response?.let {
                if (it.code == 200) {
                    ordersList.clear()
                    ordersList.addAll(it.body.data)
                    if (ordersList.isEmpty()) {
                        binding.llNoNewRequest.visibility = View.VISIBLE
                        binding.rvHistory.visibility = View.GONE
                    } else {
                        binding.llNoNewRequest.visibility = View.GONE
                        binding.rvHistory.visibility = View.VISIBLE
                        adapter?.notifyDataSetChanged()
                        adapter?.onItemClickListener = { pos, type ->

                            val id = adapter!!.ordersList[pos].id.toString()

                            when (type) {
                                "cancel" -> {
                                    checkOrder(id)
                                }
                                "detail" -> {
                                    startActivity(Intent(this@OrderHistoryActivity,OrderDetailActivity::class.java)
                                        .putExtra("id",id.toString())
                                    )
                                }
                                "reOrder" -> {

                                    if (Utils.internetAvailability(this)) {
//                                        progressDialog.show(this)
                                        val jsonObjects = JSONObject().apply {
                                            put("order_id", id.toString())
                                        }

                                        socketManager.re_order(jsonObjects)

                                    } else {
                                        Utils.showToast(this, getString(R.string.no_internet_connection))
                                    }

                                }
                                "modifier" -> {
                                    val order = adapter!!.ordersList[pos]
                                    startActivity(
                                        Intent(this@OrderHistoryActivity, AddOrderActivity::class.java)
                                            .putExtra("userId", order.user_id.toString())
                                            .putExtra("shopId", order.shop_id.toString())
                                            .putExtra("shopName", order.shop.name)
                                            .putExtra("user_latitude", order.latitude ?: "")
                                            .putExtra("user_longitude", order.longitude ?: "")
                                            .putExtra("latitudeShop", order.shop.latitude ?: "")  // ← shop.latitude
                                            .putExtra("longitudeShop", order.shop.longitude ?: "")  // ← shop.longitude
                                            .putExtra("order_id_to_cancel", order.id.toString())
                                    )
                                }
                                "changeStatus" -> {
                                    val order = adapter!!.ordersList[pos]
                                    val currentStatus = order.status ?: 0

                                    when (currentStatus) {
                                        0 -> {
                                            androidx.appcompat.app.AlertDialog.Builder(this)
                                                .setTitle("Changer le statut")
                                                .setItems(arrayOf("✅ Accepter")) { _, which ->
                                                    when (which) {
                                                        0 -> changeOrderStatus(order.id.toString(), "1")
                                                    }
                                                }
                                                .setNegativeButton("Annuler") { d, _ -> d.dismiss() }
                                                .show()
                                        }
                                        1 -> {
                                            androidx.appcompat.app.AlertDialog.Builder(this)
                                                .setTitle("Changer le statut")
                                                .setItems(arrayOf("⏳ En attente", "✅ Livré")) { _, which ->
                                                    when (which) {
                                                        0 -> changeOrderStatus(order.id.toString(), "0")
                                                        1 -> changeOrderStatus(order.id.toString(), "2")
                                                    }
                                                }
                                                .setNegativeButton("Annuler") { d, _ -> d.dismiss() }
                                                .show()
                                        }
                                    }
                                }
                                else -> {
                                    contactDialog(pos, type)
                                }
                            }

//                            contactDialog(pos, type)


                        }
                        binding.edSearch.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence?, start: Int, count: Int, after: Int
                            ) {
                            }

                            override fun onTextChanged(
                                s: CharSequence?, start: Int, before: Int, count: Int
                            ) {
                            }

                            override fun afterTextChanged(s: Editable) {
                                try {
                                    if (adapter != null) {
                                        adapter!!.filter(s.toString().trim(), binding)
                                    }
                                } catch (e: java.lang.Exception) {
                                }
                            }
                        })
                    }

                }
            }
        }

        callOrdersApi()
    }


    private fun callOrdersApi() {
        val map = HashMap<String, String>()
        type = intent.getStringExtra("type").toString()
        if (type == "current_orders") {
//            binding.ivFilter.visibility=View.GONE
            binding.tvTitle.text = getString(R.string.current_orders)
            map["status"] = "0"

        }

        if (intent.getStringExtra("userId")!=null){
            user_id=  intent.getStringExtra("userId").toString()
            map["user_id"] = user_id.toString()

        }

        map["page"] = page.toString()
        map["limit"] = limit.toString()

        authViewModel.orders(map)
    }

    private fun filterOrders(filterType: String) {
        val filteredList: List<OrderHistoryResponse.Body.Data> = when (filterType) {
            "all_orders" -> ordersList // Show all orders
            "pending" -> ordersList.filter { it.status == 0 }
            "accepted" -> ordersList.filter { it.status == 1 }
            "picked" -> ordersList.filter { it.status == 1 && it.driver_status == 2 }
            "delivered" -> ordersList.filter {
                (it.status == 2 && it.driver_status == 3) || it.status == 3
            }

            else -> ordersList // Default: Show all orders
        }

        // Update the adapter's list
        adapter?.updateList(filteredList as ArrayList<OrderHistoryResponse.Body.Data>)

        // Notify the adapter of data changes
        adapter?.notifyDataSetChanged()
    }

    private fun showFilterPopup(anchor: ImageView) {
        // Create PopupMenu
        val popupMenu = PopupMenu(this, anchor)
        popupMenu.menuInflater.inflate(R.menu.filter_menu, popupMenu.menu) // Inflate from resource

        // Handle menu item clicks
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.all_orders -> {
                    filterOrders("all_orders")
                    true
                }

                R.id.pending -> {
                    filterOrders("pending")
                    true
                }

                R.id.accepted -> {
                    filterOrders("accepted")
                    true
                }

                R.id.picked -> {
                    filterOrders("picked")
                    true
                }

                R.id.delivered -> {
                    filterOrders("delivered")
                    true
                }

                else -> false
            }
        }

        // Show the popup menu
        popupMenu.show()
    }

    private fun contactDialog(pos: Int, type: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.cantact_picker)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val window = dialog.window
        window!!.setGravity(Gravity.BOTTOM)
        window.setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        val tvCall = dialog.findViewById<TextView>(R.id.tvCall)
        val tvMessage = dialog.findViewById<TextView>(R.id.tvMessage)
        val tvCancel = dialog.findViewById<TextView>(R.id.tvCancel)
        val tvWhatsApp = dialog.findViewById<TextView>(R.id.tvWhatsApp)
        tvCancel.setOnClickListener { dialog.dismiss() }

        val order = adapter!!.ordersList[pos]
        val hasDriver = order.driver_detail != null

        tvMessage.setOnClickListener {
            dialog.dismiss()
            if (type == "driver") {
                // ✅ Si driver WhatsApp uniquement → ouvrir WhatsApp directement
                if (!hasDriver) {
                    val waNumber = order.whatsapp_number
                    if (!waNumber.isNullOrEmpty()) {
                        val number = waNumber.replace("+", "")
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$number")))
                    } else {
                        Utils.showToast(this, "Aucun contact disponible pour ce driver")
                    }
                    return@setOnClickListener
                }
                startActivity(
                    Intent(this, ChatActivity::class.java)
                        .putExtra("otherUserId", order.driver_id.toString())
                        .putExtra("otherUserName", order.driver_detail.name)
                        .putExtra("otherUserImage", order.driver_detail.profile_pic)
                        .putExtra("phone_no", order.driver_detail.phone_no.toString())
                        .putExtra("userType", "2")
                        .putExtra("shopName", "null")
                )
            } else {
                var driverLatLng: LatLng? = null
                var shopLatLng: LatLng? = null
                try {
                    driverLatLng = LatLng(
                        order.user_detail.latitude.toDouble(),
                        order.user_detail.longitude.toDouble()
                    )
                    shopLatLng = LatLng(
                        order.shop.latitude.toDouble(),
                        order.shop.longitude.toDouble()
                    )
                } catch (e: Exception) { }

                startActivity(
                    Intent(this, ChatActivity::class.java)
                        .putExtra("otherUserId", order.user_id.toString())
                        .putExtra("otherUserName", order.user_detail.name)
                        .putExtra("otherUserImage", order.user_detail.profile_pic)
                        .putExtra("phone_no", order.user_detail.phone_no.toString())
                        .putExtra("userType", "3")
                        .putExtra("shopName", order.shop.name)
                        .putExtra("shopId", order.shop_id.toString())
                        .putExtra("shopPhone", order.shop.phone.toString())
                        .putExtra("driverLatLng", driverLatLng)
                        .putExtra("shopLatLng", shopLatLng)
                )
            }
        }

        tvWhatsApp.setOnClickListener {
            dialog.dismiss()
            if (type == "driver") {
                val waNumber = order.whatsapp_number
                if (!waNumber.isNullOrEmpty()) {
                    val number = waNumber.replace("+", "")
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$number")))
                } else if (hasDriver) {
                    openWhatsApps(order.driver_detail.phone_no)
                } else {
                    Utils.showToast(this, "Aucun numéro WhatsApp disponible")
                }
            } else {
                val waNumber = order.user_whatsapp
                if (!waNumber.isNullOrEmpty()) {
                    val number = waNumber.replace("+", "")
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$number")))
                } else {
                    openWhatsApps(order.user_detail.phone_no)
                }
            }
        }

        tvCall.setOnClickListener {
            dialog.dismiss()
            if (type == "driver") {
                if (hasDriver) {
                    makePhoneCall(order.driver_detail.phone_no, this)
                } else {
                    Utils.showToast(this, "Aucun numéro disponible pour ce driver")
                }
            } else {
                makePhoneCall(order.user_detail.phone_no, this)
            }
        }

        dialog.show()
    }

//    fun openWhatsApps(number: String) {
//        val urlString = "https://wa.me/$number"
//        val whatsappUri = Uri.parse(urlString)
//        val intent = Intent(Intent.ACTION_VIEW, whatsappUri)
//        if (intent.resolveActivity(packageManager) != null) {
//            startActivity(intent)
//        } else {
//            // WhatsApp not installed or no activity to handle the intent
//            Toast.makeText(this, "Unable to open WhatsApp", Toast.LENGTH_SHORT).show()
//        }
//    }

    private fun openWhatsApps(phone: String) {
//        val installed = appInstalledOrNot("com.whatsapp")
//        if (installed) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("http://api.whatsapp.com/send?phone=$phone")
        startActivity(intent)
//        } else {
//            Toast.makeText(
//                this@OrderHistoryActivity,
//                "Whats app not installed on your device",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
    }

    //Create method appInstalledOrNot
    private fun appInstalledOrNot(url: String): Boolean {
        val packageManager = packageManager
        val app_installed: Boolean
        app_installed = try {
            packageManager.getPackageInfo(url, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
        return app_installed
    }

    private fun checkOrder(id: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.cancel_order_dialog)
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
            cancelOrderSocket(id)
        }
        tvNoL.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun filter() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.filter_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window!!.setGravity(Gravity.BOTTOM)
//        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimationPhone

        dialog.show()

        val btWeekly = dialog.findViewById<AppCompatButton>(R.id.btWeekly)
        val btMonthly = dialog.findViewById<AppCompatButton>(R.id.btMonthly)
        val btYear = dialog.findViewById<AppCompatButton>(R.id.btYear)
//        Weekly type: 1
//        Monthluy: 2
//        yearly: 3
        btWeekly.setOnClickListener {
            type = "1"
            callOrdersApi()
            dialog.dismiss()

        }
        btMonthly.setOnClickListener {
            type = "2"

            callOrdersApi()

            dialog.dismiss()
        }
        btYear.setOnClickListener {
            type = "3"

            callOrdersApi()

            dialog.dismiss()
        }
    }

    private fun cancelOrderSocket(id: String) {
        if (Utils.internetAvailability(this)) {
//            progressDialog.show(this)
            val jsonObjects = JSONObject().apply {
                put("order_id", id.toString())


            }
            Log.e("jsonObjects: ", jsonObjects.toString())

            socketManager.cancelOrderSocket(jsonObjects)
            lifecycleScope.launch {
                delay(1000)
                callOrdersApi()
            }
        } else {
            Utils.showToast(this, getString(R.string.no_internet_connection))
        }
    }

    private fun changeOrderStatus(orderId: String, newStatus: String) {
        val map = HashMap<String, String>()
        map["order_id"] = orderId
        map["status"] = newStatus
        authViewModel.changeOrderStatus(map)
        lifecycleScope.launch {
            delay(1000)
            callOrdersApi()
        }
    }
    private fun initializeSockets() {
        socketManager = MyApplication.mInstance?.getSocketManager()!!
        socketManager.init()
        socketManager.onRegister(this)
        socketManager.onaddorderListener()
        socketManager.reOrderListener()


    }

    override fun onResponseArray(event: String, args: JSONArray) {

    }

    override fun onResponse(event: String, args: JSONObject) {
        // Handle individual JSON object responses if needed
        when (event) {
            SocketManager.add_order_listner -> {
                lifecycleScope.launch {
                    progressDialog.hide()

                    args.let { jsonObject ->
                        val notAdded = jsonObject.optInt(
                            "notAdded",
                            -1
                        ) // Default to -1 if key is missing or not an int

                        if (notAdded != 1) {
                            callOrdersApi()
                            Utils.showSuccessnewDialog(
                                this@OrderHistoryActivity,
                                getString(R.string.reorder_successfully)
                            )
                        } else {
                            Utils.showErrorDialog(
                                this@OrderHistoryActivity,
                                getString(R.string.order_already_accepted)
                            )
                            callOrdersApi()
                        }
                    } ?: Log.e("SocketError", "Received null response from socket")


                }}
//            SocketManager.re_order -> {
//                lifecycleScope.launch {
//                    progressDialog.hide()
//
//                }
//            }

        }
    }

    override fun onError(event: String, vararg args: Array<*>) {
    }

    override fun onBlockError(event: String, args: String) {
    }
}