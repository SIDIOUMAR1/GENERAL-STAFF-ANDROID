package com.genralstaff

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.genralstaff.adapter.MessageListingAdapter
import com.genralstaff.databinding.FragmentMessagesBinding
import com.genralstaff.responseModel.ChatItem
import com.genralstaff.sockets.SocketManager
import com.genralstaff.utils.CustomProgressDialog
import com.genralstaff.utils.MyApplication
import com.genralstaff.utils.MyApplication.Companion.prefs
import com.genralstaff.utils.Utils
import com.google.gson.GsonBuilder

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class MessagesFragment : Fragment(), SocketManager.Observer {
    private lateinit var socketManager: SocketManager
    private var _binding: FragmentMessagesBinding? = null
    private val progressDialog by lazy { CustomProgressDialog() }
    private val activityScope = CoroutineScope(Dispatchers.Main)

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e( "onResume: ", MyApplication.prefs?.getString("userId").toString())
//        initializeSockets()
        binding.ivFilter.setOnClickListener {
            showFilterPopup(binding.ivFilter)
        }
    }
    private fun showFilterPopup(anchor: ImageView) {
        // Create PopupMenu
        val popupMenu = PopupMenu(requireActivity(), anchor)
        popupMenu.menuInflater.inflate(R.menu.new_menu, popupMenu.menu) // Inflate from resource

        // Handle menu item clicks
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.all -> {
                    callGetChatList()
//                    messageAdapter?.updateList(chatItemArrayList) // Show all messages
                    true
                }

//                R.id.seen -> {
//                    val seenMessages = chatItemArrayList.filter { it.last_message_detail.is_read != 0 }
//                    messageAdapter?.updateList(ArrayList(seenMessages)) // Update list with seen messages
//                    true
//                }
                R.id.unseen -> {
                    val unseenMessages = chatItemArrayList.filter {
                                MyApplication.prefs?.getString("userId") != it.last_message_detail?.sender_id.toString()
                    }
                    messageAdapter?.updateList(ArrayList(unseenMessages)) // Update list with unseen messages
                    true
                }






                else -> false
            }
        }

        // Show the popup menu
        popupMenu.show()
    }

    override fun onResume() {
        super.onResume()
        initializeSockets()
        callGetChatList()
    }
    private fun initializeSockets() {
        socketManager = MyApplication.mInstance?.getSocketManager()!!
        socketManager.init()
        socketManager.onRegister(this)
    }

    private fun callGetChatList() {
        if (Utils.internetAvailability(requireContext())) {
            val userId = prefs?.getString("userId")
            Log.e("userId", userId.toString())
            val jsonObjects = JSONObject().apply {
                put("sender_id", userId)
            }
            socketManager.getChatUserList(jsonObjects)
//            progressDialog.show(requireActivity())
        } else {
            Utils.showToast(requireContext(), getString(R.string.no_internet_connection))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    var chatItemArrayList = ArrayList<ChatItem>()
    var messageAdapter : MessageListingAdapter?=null
    override fun onResponseArray(event: String, args: JSONArray) {
        when (event) {
            SocketManager.get_user_list -> {
                activityScope.launch {
                    try {
                        val gson = GsonBuilder().create()
                        val chatItemList =
                            gson.fromJson(args.toString(), Array<ChatItem>::class.java)
                         chatItemArrayList = ArrayList(chatItemList.toList())
                        binding.tvCount.text = getString(R.string.chats, chatItemArrayList.size)

                        if (chatItemArrayList.isEmpty()) {
                            binding.llNoNewRequest.visibility = View.VISIBLE
                        } else {
                            binding.llNoNewRequest.visibility = View.GONE

                        }
                         messageAdapter =
                            MessageListingAdapter(
                                requireActivity(),
                                chatItemArrayList,
                                chatItemArrayList,viewLifecycleOwner.lifecycleScope
                            )
                        binding.rvChat.adapter = messageAdapter

                        binding.edSearch.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                            }

                            override fun afterTextChanged(s: Editable) {
                                try {
                                    if (messageAdapter != null) {
                                        messageAdapter?.filter(s.toString().trim(), binding)
                                    }
                                } catch (e: java.lang.Exception) {
                                }
                            }
                        })
                        progressDialog.hide()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Handle exception or display error message
                        Utils.showToast(requireContext(), "Error fetching chat list")
                    }
                }
            }
        }
    }

    override fun onResponse(event: String, args: JSONObject) {
        // Handle response if needed
    }

    override fun onError(event: String, vararg args: Array<*>) {
        // Handle error if needed
    }

    override fun onBlockError(event: String, args: String) {
        // Handle block error if needed
    }
}
