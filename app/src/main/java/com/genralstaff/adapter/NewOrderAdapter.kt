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
import com.genralstaff.databinding.RowNewOrdersBinding
import com.genralstaff.databinding.RowRecentOrderBinding
import com.genralstaff.home.ui.TrackOrderActivity
import com.genralstaff.responseModel.OrderHistoryResponse
import com.genralstaff.utils.printDate

class NewOrderAdapter(
    var requireContext: Context,
    var ordersList: ArrayList<OrderHistoryResponse.Body.Data>
) :
    RecyclerView.Adapter<NewOrderAdapter.MyViewHolder>() {

    var onItemClickListener: ((pos: Int, type: String) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {

        val binding =
            RowNewOrdersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    @SuppressLint("SuspiciousIndentation", "SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (ordersList[position].shop==null||ordersList[position].shop.image.isNotEmpty()){
        Glide.with(requireContext).load(imageURL + ordersList[position].shop.image).placeholder(
            R.drawable.place_holder
        ).into(holder.binding.civProfil)}
        holder.binding.tvMenuName.text = ordersList[position].shop.name
        holder.binding.tvLocation.text = ordersList[position].shop.location
        holder.binding.tvNam.text = ordersList[position].product.name
        holder.binding.tvTime.text = printDate(ordersList[position].created_at)
        holder.binding.tvPrice.text = "$${ordersList[position].product.price}"
        holder.binding.btnAccept.setOnClickListener {
            onItemClickListener?.invoke(position, "accept")
        }
        holder.binding.btnReject.setOnClickListener {
            onItemClickListener?.invoke(position, "reject")
        }
        holder.itemView.setOnClickListener {
            requireContext.startActivity(
                Intent(requireContext, TrackOrderActivity::class.java).putExtra(
                    "id",
                    ordersList[position].id.toString()
                )
            )
        }

        // 0- pending, 1- accepted, 2- completed
        val status = ordersList[position].status ?: 0


    }

    override fun getItemCount(): Int {
        return ordersList.size
    }

    class MyViewHolder(val binding: RowNewOrdersBinding) :
        RecyclerView.ViewHolder(binding.root) {}


}
