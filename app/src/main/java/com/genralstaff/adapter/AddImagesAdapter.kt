package com.genralstaff.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.genralstaff.R
import com.genralstaff.databinding.RowImagesBinding
class AddImagesAdapter(
    var context: Context,
    var list: ArrayList<String>
) :
    RecyclerView.Adapter<AddImagesAdapter.MyViewHolder>() {

    var onItemClickListener: ((pos: Int, type: Int) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {

        val binding =
            RowImagesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    @SuppressLint("SuspiciousIndentation", "SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        Glide.with(context).load(list[position]).placeholder(
            R.drawable.add_image_placeholder
        ).error(R.drawable.add_image_placeholder).into(holder.binding.rivImage)
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(position, 0)
        }
        if (list[position] == "") {
            holder.binding.ivDelete.visibility = View.GONE
        } else {
            holder.binding.ivDelete.visibility = View.VISIBLE
        }
        holder.binding.ivDelete.setOnClickListener {
            onItemClickListener?.invoke(position, 1)

        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(val binding: RowImagesBinding) :
        RecyclerView.ViewHolder(binding.root) {}


}
