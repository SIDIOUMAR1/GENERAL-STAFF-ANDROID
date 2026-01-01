package com.genralstaff.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.genralstaff.R
import com.genralstaff.base.imageURL
import com.genralstaff.databinding.ActivityOrderHistoryBinding
import com.genralstaff.databinding.RowOrderHistoryBinding
import com.genralstaff.responseModel.GetShopsResponse
import com.genralstaff.responseModel.OrderHistoryResponse
import com.genralstaff.utils.printDate

class OrderItemsAdapter(
    var context: Context,
    var ordersList: ArrayList<OrderHistoryResponse.Body.Data>,
    var ordersList1: ArrayList<OrderHistoryResponse.Body.Data>
) :
    RecyclerView.Adapter<OrderItemsAdapter.MyViewHolder>() {

    var onItemClickListener: ((pos: Int, type: String) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {

        val binding =
            RowOrderHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }
    // Method to update the list in the adapter
    fun updateList(newList: ArrayList<OrderHistoryResponse.Body.Data>) {
        ordersList = newList
    }
    @SuppressLint("SuspiciousIndentation", "SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        Glide.with(context).load(imageURL + ordersList[position].shop.image)
            .placeholder(
                R.drawable.place_holder
            ).into(holder.binding.rivOrder)
        holder.binding.tvAmount.text =  ordersList[position].delivery_charge+"UM"
        holder.binding.tvName.text = ordersList[position].shop.name
        holder.binding.tvCusterName.text = context.getString(R.string.customer_name)+" "+ordersList[position].user_detail.name
        holder.binding.tvCusterPhone.text = context.getString(R.string.phone_number)+": "+ordersList[position].user_detail.country_code+ordersList[position].user_detail.phone_no
        holder.binding.tvLocation.text = ordersList[position].location

        holder.binding.tvDateTime.text = printDate(ordersList[position].created_at)
        holder.binding.tvCancel.setOnClickListener {
            onItemClickListener?.invoke(position, "cancel")
        }
        holder.binding.tvContactOrder.setOnClickListener {
            onItemClickListener?.invoke(position, "driver")
        }
        holder.binding.tvContactUser.setOnClickListener {
            onItemClickListener?.invoke(position, "user")
        }
        holder.binding.btnReOrder.setOnClickListener {
            onItemClickListener?.invoke(position, "reOrder")
        }
        holder.binding.tvDetail.setOnClickListener {
            onItemClickListener?.invoke(position, "detail")
        }

        // 0- pending, 1- accepted, 2- completed
        val status = ordersList[position].status ?: 0
        when (status) {
            0 -> {
                holder.binding.btnReOrder.visibility=View.VISIBLE
                holder.binding.tvContactOrder.visibility = View.GONE

                holder.binding.tvTrackOrder.text = context.getString(R.string.pending)
            }

            1 -> {
                holder.binding.btnReOrder.visibility=View.GONE
                when (ordersList[position].driver_status) {
                    0 -> {
                        holder.binding.tvTrackOrder.text = context.getString(R.string.accepted)
                        holder.binding.tvWeight.text = context.getString(R.string.driver_accepted)

                    }
                    1 -> {
                        holder.binding.tvTrackOrder.text = context.getString(R.string.accepted)
                        holder.binding.tvWeight.text = context.getString(R.string.driver_accepted)

                    }
                    2 -> {
                        holder.binding.tvTrackOrder.text = context.getString(R.string.picked)
                        holder.binding.tvWeight.text = context.getString(R.string.driver_picked)
                    }
                    else -> {
                        holder.binding.tvTrackOrder.text = context.getString(R.string.on_route)
                        holder.binding.tvWeight.text = context.getString(R.string.driver_picked)
                    }
                }

            }
            2 -> {
                holder.binding.btnReOrder.visibility=View.GONE

                if (ordersList[position].driver_status == 3) {
                    holder.binding.tvWeight.visibility = View.GONE
                    holder.binding.tvCancel.visibility = View.GONE
                    holder.binding.tvContactOrder.visibility = View.GONE
                    holder.binding.tvTrackOrder.text = context.getString(R.string.delivered)
                } else {
                    holder.binding.tvTrackOrder.text =  context.getString(R.string.picked)
                    holder.binding.tvWeight.text = context.getString(R.string.driver_picked)
                }

            }

            3 -> {
                holder.binding.btnReOrder.visibility=View.GONE

                holder.binding.tvWeight.visibility = View.GONE
                holder.binding.tvCancel.visibility = View.GONE
                holder.binding.tvContactOrder.visibility = View.GONE
                holder.binding.tvTrackOrder.text =context.getString(R.string.cancel)
            }

            else -> {
                holder.binding.btnReOrder.visibility=View.GONE

                // Handle other cases if needed
            }
        }


    }

    fun filter(charText: String, binding: ActivityOrderHistoryBinding) {
        val searchText = charText.lowercase().trim() // Cleaned up input text

        // Create a new list for filtered results
        val filteredList: ArrayList<OrderHistoryResponse.Body.Data> = ArrayList()

        if (searchText.isEmpty()) {
            // If no search text, show all items
            filteredList.addAll(ordersList1)
        } else {
            // Filter the list based on search text
            for (order in ordersList1) {
                val name = order.user_detail.name.lowercase().trim()
                val phone = order.user_detail.phone_no.lowercase().trim()

                // Check if name or phone number contains the search text
                if (name.contains(searchText) || phone.contains(searchText)) {
                    filteredList.add(order)
                }
            }
        }

        // Update the adapter's data list and notify changes
        ordersList = filteredList
        notifyDataSetChanged()

        // Show or hide the 'no results' view based on whether the filtered list is empty
        if (ordersList.isEmpty()) {
            binding.llNoNewRequest.visibility = View.VISIBLE
        } else {
            binding.llNoNewRequest.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return ordersList.size
    }

    class MyViewHolder(val binding: RowOrderHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {}


}
