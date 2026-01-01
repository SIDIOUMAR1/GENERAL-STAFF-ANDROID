package com.genralstaff.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.genralstaff.R
import com.genralstaff.base.imageURL
import com.genralstaff.base.profileBaseUrl
import com.genralstaff.databinding.RowNotificationBinding
import com.genralstaff.responseModel.NotificationListResponse
import com.genralstaff.utils.printTime

class NotificationListAdapter(
    var requireContext: Context,
    var notificationList: ArrayList<NotificationListResponse.Body.Data>
) :
    RecyclerView.Adapter<NotificationListAdapter.MyViewHolder>() {

    var onItemClickListener: ((pos: Int) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {

        val binding =
            RowNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    @SuppressLint("SuspiciousIndentation", "SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.binding.tvName.text = notificationList[position].body
        holder.binding.tvTime.text = printTime(notificationList[position].createdAt)

        Glide.with(requireContext)
            .load(imageURL + notificationList[position].sender_detail.profile_pic).placeholder(
                R.drawable.place_holder
            ).into(holder.binding.civProfile)


    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    class MyViewHolder(val binding: RowNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {}


}
