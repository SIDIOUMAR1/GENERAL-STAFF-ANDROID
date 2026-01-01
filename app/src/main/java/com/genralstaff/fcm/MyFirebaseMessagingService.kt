package com.genralstaff.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.genralstaff.R
import com.genralstaff.home.HomeActivity
import com.genralstaff.home.ui.ChatActivity
import com.genralstaff.utils.MyApplication.Companion.prefs
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


import org.json.JSONObject

public class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "FireBasePush"
    var message: String? = ""
    var type: String? = ""
    var title: String? = ""

    val channelId = "channel-01"
    var name = ""
    val channelName = "Channel Name"
    val importance = NotificationManager.IMPORTANCE_HIGH
    val notificationId = 1
    private var i = 0
    var objectBody: JSONObject? = null

    override fun onNewToken(refreshedToken: String) {
        super.onNewToken(refreshedToken)
        Log.e(TAG, "Refreshed token: $refreshedToken")
//        saveDeviceTokenPrefrence("deviceToken", refreshedToken)
    }

    //    if type == "2"{
//        do {
//            let decoder = JSONDecoder()
//            let jsonData = try JSONSerialization.data(withJSONObject: data)
//                let model = try decoder.decode(OfferModel.self, from: jsonData) as OfferModel
//                    if ((model.createdAt ?? "").getDate("yyyy-MM-dd'T'HH:mm:ss.SSSZ")?.getSeconds() ?? 0)<61{
//                        self.navigateToOfferScreenVC(model)
//                    }else{
//                        Toast.shared.showAlert(type: .validationFailure, message: "Sorry! Time to accept the offer is over", time: 999999)
//                    }
//                }catch{
//                    print("Error Catched")
//                }
//            }
    var shop_id = ""
    var profilePic = ""
    var senderName = ""
    var shop_phone = ""
    var shop_name = ""
    var phone_no = ""
    var otherUserLong = ""
    var otherUserLat = ""
    var shopLat = ""
    var shopLong = ""

    var sender_type = ""
    var senderId = ""
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.e("Notification", "Notification response: ${remoteMessage.data}")
        message = remoteMessage.data["body"].toString()
        title = remoteMessage.data["title"].toString()
        type = remoteMessage.data["notification_type"].toString()
        if (type == "send_message_user_driver") {

            val data = remoteMessage.data
            val objString = data["obj"].toString()
            type = "1"
// Parse the objString to get the required values
            val objJson = JSONObject(objString)
            phone_no = objJson.getString("phone_no")
            profilePic = objJson.getString("profile_pic")
            senderName = objJson.getString("sender_name")
            shop_phone = objJson.getString("shop_phone")
            shop_name = objJson.getString("shop_name")

            otherUserLat = objJson.getString("otherUserLat")
            otherUserLong = objJson.getString("otherUserLong")

            shopLat = objJson.getString("shopLat")
            shopLong = objJson.getString("shopLong")
            sender_type = objJson.getString("sender_type").toString()
            shop_id = objJson.getString("shop_id").toString()
            senderId = objJson.getInt("sender_id").toString()
            if (prefs?.getString("STATUS_CHAT") != "true$senderName") {
                makePush()

            }

//            message push
        } else {
            type = "0"

            makePush()

            sendBroadcastForScreenRefresh()
//            new order push
        }


        Log.e("NotificationTitle", "NotificationTitle---: $title")
    }

    var intent: Intent? = null


    private fun makePush() {
        // Create a unique request code for each notification
        val uniqueId = System.currentTimeMillis().toInt()

        intent = when (type) {
            "1" -> {
                Intent(this, HomeActivity::class.java)
                    .putExtra("type", type)
                    .putExtra("otherUserId", senderId)
                    .putExtra("otherUserName", senderName)
                    .putExtra("shop_name", shop_name)
                    .putExtra("shopPhone", shop_phone)
                    .putExtra("otherUserImage", profilePic)
                    .putExtra("phone_no", phone_no)
                    .putExtra("shop_id", shop_id.toString())
                    .putExtra("sender_type", sender_type.toString())
                    .putExtra("otherUserLat", otherUserLat)
                    .putExtra("otherUserLong", otherUserLong)
                    .putExtra("shopLat", shopLat)
                    .putExtra("shopLong", shopLong)
            }
            else -> {
                Intent(this, HomeActivity::class.java)
                    .putExtra("type", type)
                    .putExtra("otherUserId", senderId)
                    .putExtra("otherUserName", senderName)
                    .putExtra("shop_name", shop_name)
                    .putExtra("shopPhone", shop_phone)
                    .putExtra("otherUserImage", profilePic)
                    .putExtra("phone_no", phone_no)
                    .putExtra("shop_id", shop_id.toString())
                    .putExtra("sender_type", sender_type.toString())
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mNotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val mChannel = NotificationChannel(channelId, channelName, importance)
            mChannel.enableVibration(true)
            mNotificationManager.createNotificationChannel(mChannel)

            val mBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(notificationIcon)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.app_logo))
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setContentText(message)
                .setAutoCancel(true)

            val stackBuilder = TaskStackBuilder.create(this)
            stackBuilder.addNextIntent(intent)
            val resultPendingIntent = stackBuilder.getPendingIntent(
                uniqueId,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

            mBuilder.setContentIntent(resultPendingIntent)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)

            mNotificationManager.notify(uniqueId, mBuilder.build())

        } else {
            intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(
                this,
                uniqueId,
                intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
            val context = baseContext
            val mBuilder = NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(notificationIcon)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.app_logo))
                .setPriority(importance)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(pendingIntent)

            val mNotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.notify(uniqueId, mBuilder.build())
        }
    }

    private val notificationIcon: Int
        get() {
            val useWhiteIcon = Build.VERSION.SDK_INT > Build.VERSION_CODES.S
            return if (useWhiteIcon) R.drawable.app_logo else R.drawable.app_logo
        }

    private fun sendBroadcastForScreenRefresh() {
        Log.e("sendBroadcastForScreenRefresh: ", "SEND")

        //sending broadcast to other screens for refreshing api when notification receive
        val i = Intent("msg") //action: "msg"
        i.setPackage(packageName)
        i.putExtra("type", type)
        applicationContext.sendBroadcast(i)
    }
}