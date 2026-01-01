package com.genralstaff.home.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.genralstaff.R
import com.genralstaff.adapter.MessageListingAdapter
import com.genralstaff.base.profileBaseUrl
import com.genralstaff.databinding.FragmentHomeBinding
import com.genralstaff.home.ui.OrderHistoryActivity
import com.genralstaff.home.ui.TrackOrderActivity
import com.genralstaff.network.ErrorType
import com.genralstaff.responseModel.ChatItem
import com.genralstaff.responseModel.OrderHistoryResponse
import com.genralstaff.sockets.SocketManager
import com.genralstaff.utils.CustomProgressDialog
import com.genralstaff.utils.MyApplication
import com.genralstaff.utils.Utils
import com.genralstaff.utils.sessionExpire
import com.genralstaff.viewmodel.AuthViewModel
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class HomeFragment : Fragment(), SocketManager.Observer {
    private lateinit var socketManager: SocketManager

    private var _binding: FragmentHomeBinding? = null
    var limit = 100
    var page = 1
    var type = 1

    private lateinit var authViewModel: AuthViewModel
    private val progressDialog by lazy { CustomProgressDialog() }


    private fun initializeSockets() {
        socketManager = MyApplication.mInstance?.getSocketManager()!!
        socketManager.init()
        socketManager.onRegister(this)
        callGetChatList()
    }

    private fun callGetChatList() {
        if (Utils.internetAvailability(requireActivity())) {
            val userId = MyApplication.prefs?.getString("userId")
            Log.e("userId", userId.toString())
            val jsonObjects = JSONObject().apply {
                put("sender_id", userId)
            }
            socketManager.getChatUserList(jsonObjects)
//            progressDialog.show()
        } else {
            Utils.showToast(requireActivity(), getString(R.string.no_internet_connection))
        }
    }



    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {


        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvName.text = getString(R.string.welcome) + MyApplication.prefs!!.getString("name").toString()


        Glide.with(requireActivity())
            .load(profileBaseUrl + MyApplication.prefs!!.getString("IMAGE").toString()).placeholder(
                R.drawable.place_holder
            ).into(binding.civProfile)



        binding.rlCurrentOrder.setOnClickListener {
            startActivity(
                Intent(requireActivity(), OrderHistoryActivity::class.java)
                    .putExtra("type", "current_orders")
            )
        }
        binding.rlPastOrder.setOnClickListener {
            startActivity(
                Intent(requireActivity(), OrderHistoryActivity::class.java)
                    .putExtra("type", "orders")
            )
        }
    }

    override fun onResume() {
        super.onResume()
        initializeSockets()

        viewModelSetupAndResponse()
    }

    private fun viewModelSetupAndResponse() {

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        authViewModel.getError().observe(this) {
            Utils.showToast(requireActivity(), it)
        }
        authViewModel.progressDialogData().observe(this) { isShowProgress ->
//            if (isShowProgress) {
//                progressDialog.show(requireActivity())
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

        authViewModel.onDashboardResponse().observe(this) { response ->
            response?.let {
                if (it.code == 200) {
                    binding.tvTotalShop.text = it.body.current_orders.toString()
                    binding.tvOrder.text = it.body.total_orders.toString()

                }
            }
        }
        authViewModel.dashboardApi()

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (::socketManager.isInitialized) {
            socketManager.unRegister(this)
        }
    }

    override fun onResponseArray(event: String, args: JSONArray) {
        when (event) {
            SocketManager.get_user_list -> {
                activityScope.launch {
                    try {
                        progressDialog.hide()

                        if (args.length() == 0) {
//                            showError("Received empty chat list.")
                            return@launch
                        }

                        val gson = GsonBuilder().create()

                        val chatItemList: Array<ChatItem>? = try {
                            gson.fromJson(args.toString(), Array<ChatItem>::class.java)
                        } catch (e: JsonSyntaxException) {
                            Log.e("ChatListError", "JSON Parsing error", e)
                            showError("Error parsing chat data. Please try again.")
                            return@launch
                        }

                        if (chatItemList == null) {
                            showError("Failed to load chat list.")
                            return@launch
                        }

                        val chatItemArrayList = ArrayList(chatItemList.toList())

                        if (isAdded && context != null) {
                            binding.llNoRequest.visibility = if (chatItemArrayList.isEmpty()) View.VISIBLE else View.GONE
                            val messageAdapter = MessageListingAdapter(
                                requireActivity(), chatItemArrayList, chatItemArrayList, viewLifecycleOwner.lifecycleScope
                            )
                            binding.rvChat.adapter = messageAdapter
                        }
                    } catch (e: Exception) {
                        Log.e("ChatListError", "Unexpected error", e)
                        showError("Something went wrong: ${e.localizedMessage}")
                    }
                }
            }
        }
    }

    /**
     * Show error dialog safely
     */
    private fun showError(message: String) {
        activity?.let {
            Utils.showErrorDialog(it, message) // Show error using Activity context if available
        } ?: run {
            Utils.showErrorDialog(requireActivity().applicationContext, message) // Use Application Context
        }
    }


    private val activityScope = CoroutineScope(Dispatchers.Main)

    override fun onResponse(event: String, args: JSONObject) {


    }

    override fun onDestroy() {
        super.onDestroy()
//        socketManager.unRegister(this)
    }

    override fun onError(event: String, vararg args: Array<*>) {
    }

    override fun onBlockError(event: String, args: String) {
    }
}