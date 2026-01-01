package com.genralstaff.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.genralstaff.R
import com.genralstaff.base.imageURL
import com.genralstaff.base.profileBaseUrl
import com.genralstaff.databinding.FragmentMessagesBinding
import com.genralstaff.databinding.RowChatUserListBinding
import com.genralstaff.home.ui.ChatActivity
import com.genralstaff.responseModel.ChatItem
import com.genralstaff.utils.GoogleMapsAPI
import com.genralstaff.utils.MyApplication
import com.genralstaff.utils.getDistanceTo
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale


class MessageListingAdapter(
    private val mContext: Context,
    var arrayList: ArrayList<ChatItem>,
    var arrayList1: ArrayList<ChatItem>, private val coroutineScope: CoroutineScope
) : RecyclerView.Adapter<MessageListingAdapter.MyViewHolder>() {

    inner class MyViewHolder(val binding: RowChatUserListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            RowChatUserListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    fun updateList(newList: ArrayList<ChatItem>) {
        arrayList.clear()
        arrayList1.clear()
        arrayList.addAll(newList)
        arrayList1.addAll(newList)
        notifyDataSetChanged()
    }

    fun filter(charText: String, binding: FragmentMessagesBinding) {
        val charText = charText.toLowerCase()

        val nList: ArrayList<ChatItem> = ArrayList<ChatItem>()
        if (charText.length === 0) {
            nList.addAll(arrayList1)
        } else {
            for (wp in arrayList1) {
                val value: String =
                    if (MyApplication.prefs?.getString("userId") == wp.receiver_detail?.id.toString()) {
                        wp.sender_detail?.name + "-" + wp.shop_detail?.name ?: ""
                    } else {
                        wp.receiver_detail?.name + "-" + wp.shop_detail?.name ?: ""
                    }
//                val value: String = wp.receiver_name
                if (value.toLowerCase().contains(charText.toLowerCase())) {
                    nList.add(wp)
                }
            }
        }



        arrayList = nList
        notifyDataSetChanged()

        if (arrayList.isEmpty()) {
            binding.tvNoDataFound.visibility = View.VISIBLE
        } else {
            binding.tvNoDataFound.visibility = View.GONE
        }

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val chatItem = arrayList[position]
        bindData(holder, chatItem)
        setClickListener(holder, chatItem)

    }

    private fun bindData(holder: MyViewHolder, chatItem: ChatItem) {
        var otherUserImage: String? = ""
        var otherUserName: String? = ""
        var otherUserId: String? = ""
        var latitudeShop: String? = ""
        var longitudeShop: String? = ""
        var latitudeUser: String? = ""
        var longitudeUser: String? = ""
        var shopId: String = "" // Initialize shopId here

        var driverLatLng: LatLng? = null
        var shopLatLng: LatLng? = null
        val formattedDistance = chatItem.distance?.toDoubleOrNull()?.let {
            mContext.getString(R.string.approx) + " " + String.format(Locale.US, "%.1f Km", it)
        } ?: ""


        holder.binding.tvDistance.text = formattedDistance

        if (MyApplication.prefs?.getString("userId") != chatItem.last_message_detail?.sender_id.toString()) {
            if (chatItem.last_message_detail?.is_read == 0) {
                holder.binding.tvMessage.setTextColor(
                    ContextCompat.getColor(
                        mContext,
                        R.color.black
                    )
                )
                holder.binding.tvMessage.typeface = Typeface.create("Poppins-Bold", Typeface.BOLD)
                // strangerLastMessageLabel.textSize = 12f
                holder.binding.ivRed.visibility = View.VISIBLE
                // strangerLastMessageLabel.textSize = 12f
            } else {
                holder.binding.ivRed.visibility = View.GONE

                holder.binding.tvMessage.setTextColor(
                    ContextCompat.getColor(
                        mContext,
                        android.R.color.darker_gray
                    )
                )
                holder.binding.tvMessage.typeface =
                    Typeface.create("Poppins-Regular", Typeface.NORMAL)
                // strangerLastMessageLabel.textSize = 12f
            }
        } else {
            holder.binding.ivRed.visibility = View.GONE
            holder.binding.tvMessage.typeface = Typeface.create("Poppins-Regular", Typeface.NORMAL)

            holder.binding.tvMessage.setTextColor(
                ContextCompat.getColor(
                    mContext,
                    android.R.color.darker_gray
                )
            )
        }


        if (chatItem.shop_detail == null) {
//            holder.binding.tvDistance.visibility = View.GONE

            otherUserId =
                if (MyApplication.prefs?.getString("userId") == chatItem.receiver_detail?.id.toString()) {
                    chatItem.sender_id.toString()
                } else {
                    chatItem.receiver_id.toString()
                }
            otherUserName =
                if (MyApplication.prefs?.getString("userId") == chatItem.receiver_detail?.id.toString()) {
                    chatItem.sender_detail?.name ?: ""
                } else {
                    chatItem.receiver_detail?.name ?: ""
                }

            otherUserImage =
                if (MyApplication.prefs?.getString("userId") == chatItem.receiver_detail?.id.toString()) {
                    profileBaseUrl + (chatItem.sender_detail?.profile_pic)
                } else {
                    profileBaseUrl + (chatItem.receiver_detail?.profile_pic)
                }
        } else {
//            holder.binding.tvDistance.visibility = View.VISIBLE
            otherUserId =
                if (MyApplication.prefs?.getString("userId") == chatItem.receiver_detail?.id.toString()) {
                    chatItem.sender_id.toString()
                } else {
                    chatItem.receiver_id.toString()
                }
            val userId = MyApplication.prefs?.getString("userId")
            val senderDetail = chatItem.sender_detail
            val receiverDetail = chatItem.receiver_detail
            // Check if userId matches receiver_detail's ID and ensure latitude/longitude are not null
//            latitudeUser = if (userId == receiverDetail?.id?.toString()) {
//                // Safely handle null for sender's latitude
//                senderDetail?.latitude?.toString() ?: ""
//            } else {
//                // Safely handle null for receiver's latitude
//                receiverDetail?.latitude?.toString() ?: ""
//            }
//
//            longitudeUser = if (userId == receiverDetail?.id?.toString()) {
//                // Safely handle null for sender's longitude
//                senderDetail?.longitude?.toString() ?: ""
//            } else {
//                // Safely handle null for receiver's longitude
//                receiverDetail?.longitude?.toString() ?: ""
//            }


//            if (chatItem.shop_detail.latitude != null) {
//                latitudeShop = chatItem.shop_detail.latitude.toString()
//                longitudeShop = chatItem.shop_detail.longitude.toString()
//            }
//
//            if (latitudeUser != "") {
//                driverLatLng = LatLng(latitudeUser.toDouble(), longitudeUser.toDouble())
//            }
//            if (latitudeShop != "") {
//                shopLatLng = LatLng(latitudeShop!!.toDouble(), longitudeShop!!.toDouble())
//
//            }
//            Log.e("bindData: ",latitudeUser )
//            Log.e("bindData: ",longitudeUser )
//
//            Log.e("bindDataShop: ",latitudeShop.toString() )
//            Log.e("bindDataShop: ",longitudeShop.toString() )
//
//            if (driverLatLng != null && shopLatLng != null) {
//
//
//                GoogleMapsAPI.getTravelDistance(
//                    Pair(driverLatLng.latitude, driverLatLng.longitude),
//                    Pair(shopLatLng.latitude, shopLatLng.longitude),
//                    mContext
//                ) { result ->
//                    (mContext as Activity).runOnUiThread {
//                        result.onSuccess { distance ->
//                            holder.binding.tvDistance.text = "$distance"
//                        }.onFailure {
//                            holder.binding.tvDistance.text = "Distance: N/A"
//                        }
//                    }
//                }
//
////                coroutineScope.launch {
////                    val distanceResult = driverLatLng?.getDistanceTo(
////                        shopLatLng!!,
////                        mContext.getString(R.string.googlePlaceKey_live)
////                    )
////                    distanceResult?.fold(
////                        onSuccess = { distanceInMiles ->
////                            Log.d("Distance", "Distance to shop: $distanceInMiles miles")
////                            val distanceInKm = distanceInMiles * 1.60934
////
////                            val formattedDistance = if (distanceInKm < 1) {
////                                val distanceInMeters = (distanceInKm * 1000).toInt()
////                                val roundedMeters = (distanceInMeters + 5) / 10 * 10  // Round to the nearest 10 meters
////                                "$roundedMeters m"
////                            } else {
////                                String.format(Locale.US,"%.1f km", distanceInKm)
////                            }
////
////                            holder.binding.tvDistance.text = "($formattedDistance)"
////
////                        },
////                        onFailure = { error ->
////                            Log.e("Error", "Error fetching distance: ${error.message}")
////                        }
////                    )
////                }
//            }
            otherUserName =
                if (MyApplication.prefs?.getString("userId") == chatItem.receiver_detail?.id.toString()) {
                    chatItem.sender_detail?.name + "-" + chatItem.shop_detail?.name ?: ""
                } else {
                    chatItem.receiver_detail?.name + "-" + chatItem.shop_detail?.name ?: ""
                }
            otherUserImage =
                if (MyApplication.prefs?.getString("userId") == chatItem.receiver_detail?.id.toString()) {
                    profileBaseUrl + (chatItem.sender_detail?.profile_pic)
                } else {
                    profileBaseUrl + (chatItem.receiver_detail?.profile_pic)
                }
            shopId = chatItem.shop_detail.id.toString() // Initialize shopId here


        }
        holder.binding.tvName.text = otherUserName

        Log.e("bindData: ", otherUserImage)
        Glide.with(mContext).load(otherUserImage)
            .placeholder(R.drawable.place_holder).error(R.drawable.place_holder)
            .into(holder.binding.ivImage)
        //1- simple message,2,image,3 audio
        when (chatItem.last_message_detail?.message_type) {
            2 -> {
                holder.binding.tvMessage.text = mContext.getString(R.string.photo)
                holder.binding.ivMusic.setImageResource(R.drawable.ic_baseline_insert_photo_24)
                holder.binding.ivMusic.visibility = View.VISIBLE
            }

            3 -> {
                holder.binding.tvMessage.text = mContext.getString(R.string.audio)
                holder.binding.ivMusic.setImageResource(R.drawable.baseline_headphones_24)
                holder.binding.ivMusic.visibility = View.VISIBLE
            }

            else -> {
                holder.binding.tvMessage.text = chatItem.last_message_detail?.message

                holder.binding.ivMusic.visibility = View.GONE
            }
        }
    }

    private fun setClickListener(holder: MyViewHolder, chatItem: ChatItem) {
        holder.itemView.setOnClickListener {
            val otherUserId: String
            val otherUserName: String
            val otherUserImage: String
            var latitudeShop: String? = ""
            var longitudeShop: String? = ""
            var latitudeUser: String? = ""
            var longitudeUser: String? = ""
            var phone_no: String
            var userType: String = ""
            var shopId: String = "" // Initialize shopId here
            var shopName: String = "" // Initialize shopId here
            var driverLatLng: LatLng? = null
            var shopLatLng: LatLng? = null
            if (chatItem.shop_detail == null) {
                otherUserId =
                    if (MyApplication.prefs?.getString("userId") == chatItem.receiver_detail?.id.toString()) {
                        chatItem.sender_id.toString()
                    } else {
                        chatItem.receiver_id.toString()
                    }
                otherUserName =
                    if (MyApplication.prefs?.getString("userId") == chatItem.receiver_detail?.id.toString()) {
                        chatItem.sender_detail?.name ?: ""
                    } else {
                        chatItem.receiver_detail?.name ?: ""
                    }
                phone_no =
                    if (MyApplication.prefs?.getString("userId") == chatItem.receiver_detail?.id.toString()) {
                        "${chatItem.sender_detail?.country_code ?: ""}${chatItem.sender_detail?.phone_no ?: ""}"
                    } else {
                        "${chatItem.receiver_detail?.country_code ?: ""}${chatItem.receiver_detail?.phone_no ?: ""}"
                    }
                otherUserImage =
                    if (MyApplication.prefs?.getString("userId") == chatItem.receiver_detail?.id.toString()) {
                        profileBaseUrl + (chatItem.sender_detail?.profile_pic)
                    } else {
                        profileBaseUrl + (chatItem.receiver_detail?.profile_pic)
                    }
            } else {
                otherUserId =
                    if (MyApplication.prefs?.getString("userId") == chatItem.receiver_detail?.id.toString()) {
                        chatItem.sender_id.toString()
                    } else {
                        chatItem.receiver_id.toString()
                    }
                otherUserName =
                    if (MyApplication.prefs?.getString("userId") == chatItem.receiver_detail?.id.toString()) {
                        chatItem.sender_detail?.name ?: ""
                    } else {
                        chatItem.receiver_detail?.name ?: ""
                    }
                userType =
                    if (MyApplication.prefs?.getString("userId") == chatItem.receiver_detail?.id.toString()) {
                        chatItem.sender_detail?.type ?: ""
                    } else {
                        chatItem.receiver_detail?.type ?: ""
                    }
                otherUserImage =
                    if (MyApplication.prefs?.getString("userId") == chatItem.receiver_detail?.id.toString()) {
                        profileBaseUrl + (chatItem.sender_detail?.profile_pic)
                    } else {
                        profileBaseUrl + (chatItem.receiver_detail?.profile_pic)
                    }
                phone_no = "${chatItem.shop_detail?.country_code ?: ""}${chatItem.shop_detail?.phone ?: ""}"
                shopId = chatItem.shop_detail.id.toString() // Initialize shopId here
                shopName = chatItem.shop_detail.name.toString() // Initialize shopId here
                val userId = MyApplication.prefs?.getString("userId")

                val senderDetail = chatItem.sender_detail
                val receiverDetail = chatItem.receiver_detail
                // Check if userId matches receiver_detail's ID and ensure latitude/longitude are not null
                latitudeUser = if (userId == receiverDetail?.id?.toString()) {
                    // Safely handle null for sender's latitude
                    senderDetail?.latitude?.toString() ?: ""
                } else {
                    // Safely handle null for receiver's latitude
                    receiverDetail?.latitude?.toString() ?: ""
                }

                longitudeUser = if (userId == receiverDetail?.id?.toString()) {
                    // Safely handle null for sender's longitude
                    senderDetail?.longitude?.toString() ?: ""
                } else {
                    // Safely handle null for receiver's longitude
                    receiverDetail?.longitude?.toString() ?: ""
                }
                if (chatItem.shop_detail.latitude != null) {
                    latitudeShop = chatItem.shop_detail.latitude.toString()
                    longitudeShop = chatItem.shop_detail.longitude.toString()
                }

                Log.e("bindData: -----------------------", "$latitudeUser---$longitudeUser")
                Log.e("bindData2: -----------------------", "$latitudeShop---$longitudeShop")
                if (latitudeUser != "") {
                    driverLatLng = LatLng(latitudeUser.toDouble(), longitudeUser.toDouble())
                }
                if (latitudeShop != "") {
                    shopLatLng = LatLng(latitudeShop!!.toDouble(), longitudeShop!!.toDouble())

                }
            }

            val intent = Intent(mContext, ChatActivity::class.java).apply {
                putExtra("otherUserId", otherUserId)
                putExtra("otherUserName", otherUserName)
                putExtra("otherUserImage", otherUserImage)
                putExtra("driverLatLng", driverLatLng)
                putExtra("shopLatLng", shopLatLng)
                putExtra("phone_no", phone_no)
                putExtra("shopId", shopId)
                putExtra("shopName", shopName)
                putExtra("userType", userType.toString())

                // Check if shop_detail.phone is not null before adding it to the Intent
                if (chatItem.shop_detail != null) {
                    putExtra("shopPhone", chatItem.shop_detail.phone)

                }

            }

            mContext.startActivity(intent)

            Log.e("setClickListener: ", "$otherUserId $otherUserName $otherUserImage $shopId")
        }
    }

    override fun getItemCount(): Int = arrayList.size
}
