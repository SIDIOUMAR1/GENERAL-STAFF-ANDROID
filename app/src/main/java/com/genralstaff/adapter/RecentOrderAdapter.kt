package com.genralstaff.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.genralstaff.R
import com.genralstaff.base.imageURL
import com.genralstaff.databinding.RowRecentOrderBinding
import com.genralstaff.home.ui.ShopDetailActivity
import com.genralstaff.responseModel.OrderHistoryResponse
import com.genralstaff.utils.printDate

class RecentOrderAdapter(
    var requireContext: Context) :
    RecyclerView.Adapter<RecentOrderAdapter.MyViewHolder>() {

    var onItemClickListener: ((pos: Int) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {

        val binding =
            RowRecentOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    @SuppressLint("SuspiciousIndentation", "SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//        if (ordersList[position].product.product_medias==null||ordersList[position].product.product_medias.isNotEmpty()){
//        Glide.with(requireContext).load(imageURL + ordersList[position].product.product_medias[0].media).placeholder(
//            R.drawable.place_holder
//        ).into(holder.binding.rivOrder)}
//        holder.binding.tvDateTimev.text = printDate(ordersList[position].created_at)
//        holder.binding.tvEarning.text = "Earning: $${ordersList[position].delivery_charge}"
//        holder.binding.tvTrackOrder.text = "Delivered"
//        holder.binding.tvName.text = ordersList[position]?.shop?.name
        holder.itemView.setOnClickListener {
            requireContext.startActivity(Intent(requireContext,ShopDetailActivity::class.java))
        }
    }

    override fun getItemCount(): Int {
        return 10
    }

    class MyViewHolder(val binding: RowRecentOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {}


}
