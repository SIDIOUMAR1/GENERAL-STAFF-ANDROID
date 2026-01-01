package com.genralstaff.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.genralstaff.R
import com.genralstaff.base.imageURL
import com.genralstaff.databinding.ActivityOrderHistoryBinding
import com.genralstaff.databinding.ActivityShopsBinding
import com.genralstaff.databinding.RowShopsBinding
import com.genralstaff.home.ui.AddShopActivity
import com.genralstaff.home.ui.ShopDetailActivity
import com.genralstaff.responseModel.GetShopsResponse
import java.io.Serializable

class ShopsAdapter(
    var context: Context,
    var ordersList: ArrayList<GetShopsResponse.Body.Data>,
    var ordersList1: ArrayList<GetShopsResponse.Body.Data>

) :
    RecyclerView.Adapter<ShopsAdapter.MyViewHolder>() {

    var onItemClickListener: ((pos: Int, type: String, id: String) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {

        val binding =
            RowShopsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    @SuppressLint("SuspiciousIndentation", "SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(imageURL + ordersList[position].image).placeholder(
            R.drawable.place_holder
        ).into(holder.binding.rivOrder)
        holder.binding.tvName.text = ordersList[position].name
        holder.binding.tvLocation.text = ordersList[position].location
        if (ordersList[position].status == "1") {
            holder.binding.ivActiveShop.setImageResource(R.drawable.active_shop_toggle)
        } else {
            holder.binding.ivActiveShop.setImageResource(R.drawable.unactive_shop_toggle)
        }
        holder.binding.ivActiveShop.setOnClickListener {
            if (ordersList[position].status == "1") {
                ordersList[position].status = "0"
            } else {
                ordersList[position].status = "1"
            }
            notifyDataSetChanged()
            onItemClickListener?.invoke(position,ordersList[position].status,ordersList[position].id.toString())
        }

        holder.itemView.setOnClickListener {
            context.startActivity(
                Intent(context, ShopDetailActivity::class.java)
                    .putExtra("id", ordersList[position].id.toString())
                    .putExtra("name", ordersList[position].name)
                    .putExtra("image", ordersList[position].image)
                    .putExtra("location", ordersList[position].location)
            )
        }
        holder.binding.ivEdit.setOnClickListener {
            context.startActivity(
                Intent(context, AddShopActivity::class.java)
                    .putExtra("type", "edit")
                    .putExtra("id", ordersList[position].id.toString())
                    .putExtra("name", ordersList[position].name)
                    .putExtra("phone_no", ordersList[position].phone?:"")
                    .putExtra("name_ar", ordersList[position].name_ar?:"")
                    .putExtra("name_fr", ordersList[position].name_fr?:"")
                    .putExtra("country_code", ordersList[position].country_code?:"")
//                    .putExtra("open_time", ordersList[position].open_time?:"")
//                    .putExtra("close_time", ordersList[position].close_time?:"")
                        .putExtra("shop_timings", ordersList[position].shop_timings as Serializable)

                .putExtra("image", ordersList[position].image)
                    .putExtra("location", ordersList[position].location)
                    .putExtra("latitude", ordersList[position].latitude.toString())
                    .putExtra("longitude", ordersList[position].longitude.toString())
                    .putExtra("description", ordersList[position].description.toString())
                    .putExtra("category_id", ordersList[position].category_id.toString())
            )
        }


    }

    fun filter(charText: String, binding: ActivityShopsBinding) {
        val charText = charText.lowercase()

        val nList: java.util.ArrayList<GetShopsResponse.Body.Data> =
            java.util.ArrayList<GetShopsResponse.Body.Data>()
        if (charText.length === 0) {
            nList.addAll(ordersList1)
        } else {
            for (wp in ordersList1) {
                val value: String = wp.name
                if (value.lowercase().contains(charText.lowercase())) {
                    nList.add(wp)
                }
            }
        }
        ordersList = nList
        notifyDataSetChanged()

        if (ordersList.isEmpty()) {
            binding.llNoNewRequest.visibility = View.VISIBLE
        } else {
            binding.llNoNewRequest.visibility = View.GONE
        }

    }

    override fun getItemCount(): Int {
        return ordersList.size
    }

    class MyViewHolder(val binding: RowShopsBinding) :
        RecyclerView.ViewHolder(binding.root) {}


}
