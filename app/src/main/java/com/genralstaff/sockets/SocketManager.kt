package com.genralstaff.sockets

import android.util.Log
import com.genralstaff.base.BASE_URL
import com.genralstaff.base.SOCKET_URL
import com.genralstaff.utils.MyApplication.Companion.prefs


import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException
import java.util.*

class SocketManager {
    // sockets listener
    private val errorMessage = "error_message"

    companion object {
        //for chat
        //emitter

        private val connectUser = "connect_user"//emitter
        val connect_listener = "connect_user"//listner

        private val get_user_list_emitter = "get_user_list"//emitter
        val get_user_list = "get_user_list"//listner

        private val send_message_emitter = "send_message"//emitter
        val send_message = "send_message"//listner

        private val get_message_list_emitter = "get_message_list"//emitter
        val get_message_list = "get_message_list"//listner

        val add_order = "add_order"//listner
        val driver_accept_reject = "driver_accept_reject"//listner

        val driver_change_statusemiter = "driver_change_status"//emiter
        val driver_change_status = "driver_change_status"//listner


        val update_locationemiter = "update_location"//emiter
        val update_location = "update_location"//listner

        val add_order_emitter = "add_order"//emiter
        val add_order_listner = "add_order"//listner

        val cancel_order_emitter = "cancel_order"//emiter
        val cancel_order_listner = "cancel_order"//listner

        val read_chat = "read_chat"//emitter listner

        val re_order = "re_order"//emitter listner


    }

    private var mSocket: Socket? = null
    private var observerList: MutableList<Observer>? = null
    fun getmSocket(): Socket? = mSocket

    private fun getSocket(): Socket? {
        try {
            val options = IO.Options().apply {
                reconnection = true
                reconnectionAttempts = Int.MAX_VALUE
                reconnectionDelay = 1000
                timeout = 20000
            }
            mSocket = IO.socket(SOCKET_URL, options)
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
        return mSocket
    }
    fun onRegister(observer: Observer) {
        if (observerList != null && !observerList!!.contains(observer)) {
            observerList!!.clear()
            observerList!!.add(observer)
        } else {
            observerList = ArrayList()
            observerList!!.clear()
            observerList!!.add(observer)
        }
    }

    fun unRegister(observer: Observer) {
        observerList?.let { list ->
            for (i in 0 until list.size - 1) {
                val model = list[i]
                if (model === observer) {
                    observerList?.remove(model)
                }
            }
        }
    }

    fun init() {
        if (mSocket == null || !mSocket!!.connected()) {
            initializeSocket()
        }
    }
    private fun initializeSocket() {
        if (mSocket == null) {
            mSocket = getSocket()
        }
        if (observerList == null) {
            observerList = ArrayList()
        }

        disconnect()
        mSocket!!.connect()
        mSocket!!.on(Socket.EVENT_CONNECT, onConnect)
        mSocket!!.on(Socket.EVENT_DISCONNECT, onDisconnect)
        mSocket!!.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
        mSocket!!.on(errorMessage, onErrorMessage)
    }

    fun disconnect() {
        mSocket?.let {
            it.off(Socket.EVENT_CONNECT, onConnect)
            it.off(Socket.EVENT_DISCONNECT, onDisconnect)
            it.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
            it.off()
            it.disconnect()
        }
    }

    private val onConnect = Emitter.Listener {
        if (isConnected()) {
            try {
                val jsonObject = JSONObject()
                val userId = prefs?.getString("userId").toString()
                if (userId != null) {
                    if (userId != "") {
                        jsonObject.put("user_id", userId.toInt())
                        mSocket!!.off(connect_listener, onConnectListener)
                        mSocket!!.on(connect_listener, onConnectListener)
                        mSocket!!.emit(connectUser, jsonObject)
                        Log.e("jsonObject", jsonObject.toString())
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            initializeSocket()
        }
    }

    fun isConnected(): Boolean {
        return mSocket != null && mSocket!!.connected()
    }

    private val onConnectListener = Emitter.Listener { args ->
        try {
            Log.e("TAG", "SOCKET Connected SuccessFully")
            // val data = args[1] as JSONObject
            // val data = args[1] as JSONObject
        } catch (ex: Exception) {
            ex.localizedMessage
        }
    }

    private val onDisconnect = Emitter.Listener { args ->
        try {
            Log.e("Socket", "DISCONNECTED :::$args")
        } catch (ex: Exception) {
            ex.localizedMessage
        }
    }

    private val onConnectError = Emitter.Listener { args ->
        try {
            Log.e("Socket", "CONNECTION ERROR :::$args")
        } catch (ex: Exception) {
            ex.localizedMessage
        }
    }


    fun getChatUserList(jsonObject: JSONObject?) {

        if (jsonObject != null) {
            try {
                if (!mSocket!!.connected()) {
                    mSocket!!.connect()
                    mSocket!!.off(get_user_list)
                    mSocket!!.on(get_user_list, onGetUserListListener)
                    mSocket!!.emit(get_user_list_emitter, jsonObject)
                } else {
                    mSocket!!.off(get_user_list)
                    mSocket!!.on(get_user_list, onGetUserListListener)
                    mSocket!!.emit(get_user_list_emitter, jsonObject)
                }
            } catch (ex: Exception) {
                ex.localizedMessage
            }

            Log.i("Socket", "getChatUserList Called")
        }
    }

    private val onGetUserListListener = Emitter.Listener { args ->
        try {
            Log.e("TAG", "onGetUserListListener" + args)

            val data = args[0] as JSONArray
            Log.e("Socket", "onGetUserListListener :::$data")
            for (observer in observerList!!) {
                observer.onResponseArray(get_user_list, data)
            }

        } catch (ex: Exception) {
            ex.localizedMessage
        }
    }

    fun getChatList(jsonObject: JSONObject?) {

        if (jsonObject != null) {
            try {
                if (!mSocket!!.connected()) {
                    mSocket!!.connect()
                    mSocket!!.off(get_message_list)
                    mSocket!!.on(get_message_list, onGetChatListListener)
                    mSocket!!.emit(get_message_list_emitter, jsonObject)
                    Log.e("jsonObjects: ", jsonObject.toString())

                } else {
                    mSocket!!.off(get_message_list)
                    mSocket!!.on(get_message_list, onGetChatListListener)
                    mSocket!!.emit(get_message_list_emitter, jsonObject)
                    Log.e("jsonObjects----: ", jsonObject.toString())

                }
            } catch (ex: Exception) {
                ex.localizedMessage
            }

            Log.i("Socket", "getChatList Called")
        }
    }
    fun read_chat(jsonObject: JSONObject?) {
        if (jsonObject != null) {
            try {
                if (!mSocket!!.connected()) {
                    mSocket!!.connect()
                    mSocket!!.emit(read_chat, jsonObject)
                } else {
                    mSocket!!.emit(read_chat, jsonObject)
                }
                Log.e("Socketjsonread_chat", jsonObject.toString())

            } catch (ex: Exception) {
                ex.localizedMessage
            }
            Log.i("Socket", "read_chat Called")
        }
    }
    fun re_order(jsonObject: JSONObject?) {
        if (jsonObject != null) {
            try {
                if (!mSocket!!.connected()) {
                    mSocket!!.connect()
                    mSocket!!.emit(re_order, jsonObject)
                } else {
                    mSocket!!.emit(re_order, jsonObject)
                }
                Log.e("Socketjsonre_order", jsonObject.toString())

            } catch (ex: Exception) {
                ex.localizedMessage
            }
            Log.i("Socket", "re_order Called")
        }
    }

    private val onGetChatListListener = Emitter.Listener { args ->
        try {
            Log.e("TAG", "onGetChatListListener" + args)

            val data = args[0] as JSONObject
            Log.e("Socket", "onGetChatListListener :::$data")
            for (observer in observerList!!) {
                observer.onResponse(get_message_list, data)
            }

        } catch (ex: Exception) {
            ex.localizedMessage
        }
    }
    fun readMessageListener() {

        try {
            if (!mSocket!!.connected()) {
                mSocket!!.connect()
                mSocket!!.off(read_chat)
                mSocket!!.on(read_chat, onReadMessageListener)
            } else {
                mSocket!!.off(read_chat)
                mSocket!!.on(read_chat, onReadMessageListener)

            }

        } catch (ex: Exception) {

            ex.localizedMessage

        }

        Log.i("Socket", "readMessageListener Called")

    }
    val onReadMessageListener = Emitter.Listener { args ->
        try {
            val data = args[0] as JSONObject

            Log.e("TAG", "onReadMessageListener")
            Log.e("Socket", "onReadMessageListener :::$data")
            for (observer in observerList!!) {
                observer.onResponse(read_chat, data)
            }


        } catch (ex: Exception) {
            ex.localizedMessage
        }
    }
    fun reOrderListener() {

        try {
            if (!mSocket!!.connected()) {
                mSocket!!.connect()
                mSocket!!.off(re_order)
                mSocket!!.on(re_order, onReOrderListener)
            } else {
                mSocket!!.off(re_order)
                mSocket!!.on(re_order, onReOrderListener)

            }

        } catch (ex: Exception) {

            ex.localizedMessage

        }

        Log.i("Socket", "readMessageListener Called")

    }
    val onReOrderListener = Emitter.Listener { args ->
        try {
            val data = args[0] as JSONObject

            Log.e("TAG", "onReOrderListener")
            Log.e("Socket", "onReOrderListener :::$data")
            for (observer in observerList!!) {
                observer.onResponse(re_order, data)
            }


        } catch (ex: Exception) {
            ex.localizedMessage
        }
    }
    fun driverStatusChange(jsonObject: JSONObject?) {

        if (jsonObject != null) {
            try {
                if (!mSocket!!.connected()) {
                    mSocket!!.connect()
                    mSocket!!.off(driver_change_status)
                    mSocket!!.on(driver_change_status, onChangeDriverStatusListener)
                    mSocket!!.emit(driver_change_statusemiter, jsonObject)
                    Log.e("jsonObjects: ", jsonObject.toString())

                } else {
                    mSocket!!.off(driver_change_status)
                    mSocket!!.on(driver_change_status, onChangeDriverStatusListener)
                    mSocket!!.emit(driver_change_statusemiter, jsonObject)
                    Log.e("jsonObjects----: ", jsonObject.toString())

                }
            } catch (ex: Exception) {
                ex.localizedMessage
            }

            Log.i("Socket", "driver_change_status Called")
        }
    }
    fun driverStatusChangeListener() {

            try {
                if (!mSocket!!.connected()) {
                    mSocket!!.connect()
                    mSocket!!.off(driver_change_status)
                    mSocket!!.on(driver_change_status, onChangeDriverStatusListener)


                } else {
                    mSocket!!.off(driver_change_status)
                    mSocket!!.on(driver_change_status, onChangeDriverStatusListener)


                }
            } catch (ex: Exception) {
                ex.localizedMessage
            }


    }

    private val onChangeDriverStatusListener = Emitter.Listener { args ->
        try {
            Log.e("TAG", "onChangeDriverStatusListener" + args)

            val data = args[0] as JSONObject
            Log.e("Socket", "onChangeDriverStatusListener :::$data")
            for (observer in observerList!!) {
                observer.onResponse(driver_change_status, data)
            }

        } catch (ex: Exception) {
            ex.localizedMessage
        }
    }

    fun updateLocation(jsonObject: JSONObject?) {

        if (jsonObject != null) {
            try {
                if (!mSocket!!.connected()) {
                    mSocket!!.connect()
                    mSocket!!.off(update_location)
                    mSocket!!.on(update_location, onUpdateLocationListener)
                    mSocket!!.emit(update_locationemiter, jsonObject)
                    Log.e("jsonObjects: ", jsonObject.toString())

                } else {
                    mSocket!!.off(update_location)
                    mSocket!!.on(update_location, onUpdateLocationListener)
                    mSocket!!.emit(update_locationemiter, jsonObject)
                    Log.e("jsonObjects----: ", jsonObject.toString())

                }
            } catch (ex: Exception) {
                ex.localizedMessage
            }

            Log.i("Socket", "update_location Called")
        }
    }

    private val onUpdateLocationListener = Emitter.Listener { args ->
        try {
            Log.e("TAG", "onUpdateLocationListener" + args)

            val data = args[0] as JSONObject
            Log.e("Socket", "onUpdateLocationListener :::$data")
            for (observer in observerList!!) {
                observer.onResponse(update_location, data)
            }

        } catch (ex: Exception) {
            ex.localizedMessage
        }
    }


    fun acceptReject(jsonObject: JSONObject?) {

        if (jsonObject != null) {
            try {
                if (!mSocket!!.connected()) {
                    mSocket!!.connect()

                    mSocket!!.emit(driver_accept_reject, jsonObject)
                    Log.e("jsonObjects: ", jsonObject.toString())

                } else {

                    mSocket!!.emit(driver_accept_reject, jsonObject)
                    Log.e("jsonObjects----: ", jsonObject.toString())

                }
            } catch (ex: Exception) {
                ex.localizedMessage
            }

            Log.i("Socket", "acceptReject Called")
        }
    }
    fun acceptRejectListener() {

            try {
                if (!mSocket!!.connected()) {
                    mSocket!!.connect()
                    mSocket!!.off(driver_accept_reject)
                    mSocket!!.on(driver_accept_reject, onAcceptRejectListener)


                } else {
                    mSocket!!.off(driver_accept_reject)
                    mSocket!!.on(driver_accept_reject, onAcceptRejectListener)


                }
            } catch (ex: Exception) {
                ex.localizedMessage
            }

        }


    private val onAcceptRejectListener = Emitter.Listener { args ->
        try {
            Log.e("TAG", "onAcceptRejectListener" + args)

            val data = args[0] as JSONObject
            Log.e("Socket", "onAcceptRejectListener :::$data")
            // Check if data is empty
            val isEmpty = data.length() == 0
            if (!isEmpty){
            for (observer in observerList!!) {
                observer.onResponse(driver_accept_reject, data)
            }}

        } catch (ex: Exception) {
            ex.localizedMessage
        }
    }

    fun getMessageListner() {

        try {
            if (!mSocket!!.connected()) {
                mSocket!!.connect()
                mSocket!!.off(send_message)
                mSocket!!.on(send_message, onSendMessagesListener)
            } else {
                mSocket!!.off(send_message)
                mSocket!!.on(send_message, onSendMessagesListener)

            }

        } catch (ex: Exception) {

            ex.localizedMessage

        }

        Log.i("Socket", "get Message Called")

    }

    fun getOrderListener() {

        try {
            if (!mSocket!!.connected()) {
                mSocket!!.connect()
                mSocket!!.off(add_order)
                mSocket!!.on(add_order, onNewOrderListener)
            } else {
                mSocket!!.off(add_order)
                mSocket!!.on(add_order, onNewOrderListener)

            }

        } catch (ex: Exception) {

            ex.localizedMessage

        }

        Log.i("Socket", "get Message Called")

    }
    fun onaddorderListener() {
        try {
            if (!mSocket!!.connected()) {
                mSocket!!.connect()
            }
            mSocket!!.off(add_order_listner, addOrderListener)
            mSocket!!.on(add_order_listner, addOrderListener)
        } catch (ex: Exception) {
            ex.localizedMessage
        }
    }
    val onSendMessagesListener = Emitter.Listener { args ->
        try {
            val data = args[0] as JSONObject
            if (data.has("success_message")) {
                //get Value of video
                val errorMessage = data.optString("success_message")

                Log.e("TAG", "get_messagesSuccessFully")
                Log.e("Socket", "get_messages :::$data")
                for (observer in observerList!!) {
                    observer.onBlockError(send_message, errorMessage)
                }
            } else {
                Log.e("TAG", "get_messagesSuccessFully")
                Log.e("Socket", "get_messages :::$data")
                for (observer in observerList!!) {
                    observer.onResponse(send_message, data)
                }
            }

        } catch (ex: Exception) {
            ex.localizedMessage
        }
    }
    val onNewOrderListener = Emitter.Listener { args ->
        try {
            Log.e("SocketjsonObject", args.toString())

            val data = args[0] as JSONObject
            Log.e("SocketjsonObject", data.toString())

            for (observer in observerList!!) {
                observer.onResponse(add_order, data)

            }

        } catch (ex: Exception) {
            ex.localizedMessage
        }
    }

    fun send_message(jsonObject: JSONObject?) {
        if (jsonObject != null) {
            try {
                if (!mSocket!!.connected()) {
                    mSocket!!.connect()
                    mSocket!!.emit(send_message, jsonObject)
                } else {
                    mSocket!!.emit(send_message, jsonObject)
                }
                Log.e("SocketjsonObject", jsonObject.toString())

            } catch (ex: Exception) {
                ex.localizedMessage
            }
            Log.i("Socket", "send_message Called")
        }
    }
    fun addOrderSocket(jsonObject: JSONObject?) {

        if (jsonObject != null) {
            try {
                if (!mSocket!!.connected()) {
                    mSocket!!.connect()
                    mSocket!!.off(add_order_listner)
                    mSocket!!.on(add_order_listner, addOrderListener)
                    mSocket!!.emit(add_order_emitter, jsonObject)
                    Log.e("jsonObjects: ", jsonObject.toString())

                } else {
                    mSocket!!.off(add_order_listner)
                    mSocket!!.on(add_order_listner, addOrderListener)
                    mSocket!!.emit(add_order_emitter, jsonObject)
                    Log.e("jsonObjects----: ", jsonObject.toString())

                }
            } catch (ex: Exception) {
                ex.localizedMessage
            }

            Log.i("Socket", "update_location Called")
        }
    }
    fun cancelOrderSocket(jsonObject: JSONObject?) {

        if (jsonObject != null) {
            try {
                if (!mSocket!!.connected()) {
                    mSocket!!.connect()
                    mSocket!!.off(cancel_order_listner)
                    mSocket!!.on(cancel_order_listner, cancelOrderListener)
                    mSocket!!.emit(cancel_order_emitter, jsonObject)
                    Log.e("jsonObjects: ", jsonObject.toString())

                } else {
                    mSocket!!.off(cancel_order_listner)
                    mSocket!!.on(cancel_order_listner, cancelOrderListener)
                    mSocket!!.emit(cancel_order_emitter, jsonObject)
                    Log.e("jsonObjects----: ", jsonObject.toString())

                }
            } catch (ex: Exception) {
                ex.localizedMessage
            }

            Log.i("Socket", "cancel_order_listner Called")
        }
    }

    private val cancelOrderListener = Emitter.Listener { args ->
        try {
            Log.e("TAG", "cancel_order_listner" + args)

            val data = args[0] as JSONObject
            Log.e("Socket", "add_order_listner :::$data")
            for (observer in observerList!!) {
                observer.onResponse(cancel_order_listner, data)
            }

        } catch (ex: Exception) {
            ex.localizedMessage
        }
    }
    private val addOrderListener = Emitter.Listener { args ->
        try {
            Log.e("TAG", "add_order_listner" + args)

            val data = args[0] as JSONObject
            Log.e("Socket", "add_order_listner :::$data")
            for (observer in observerList!!) {
                observer.onResponse(add_order_listner, data)
            }

        } catch (ex: Exception) {
            ex.localizedMessage
        }
    }
    private val onErrorMessage = Emitter.Listener { args ->
        for (observer in observerList!!) {
            try {
                val data = args[0] as JSONObject
                Log.e("Socket", "Error Message :::$data")
                observer.onError(connectUser, args)
            } catch (ex: Exception) {
                ex.localizedMessage
            }
        }
    }

    interface Observer {
        fun onResponseArray(event: String, args: JSONArray)
        fun onResponse(event: String, args: JSONObject)
        fun onError(event: String, vararg args: Array<*>)
        fun onBlockError(event: String, args: String)
    }
}