package com.genralstaff.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.genralstaff.databinding.RowNotificationsBinding
import com.genralstaff.databinding.RowOrderHistoryBinding

class NotificationsAdapter(


) :
    RecyclerView.Adapter<NotificationsAdapter.MyViewHolder>() {

    var onItemClickListener: ((pos: Int) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {

        val binding =
            RowNotificationsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    @SuppressLint("SuspiciousIndentation", "SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
holder.itemView.setOnClickListener {
    onItemClickListener!!.invoke(position)
}

    }

    override fun getItemCount(): Int {
        return 5
    }

    class MyViewHolder(val binding: RowNotificationsBinding) :
        RecyclerView.ViewHolder(binding.root) {}


}
