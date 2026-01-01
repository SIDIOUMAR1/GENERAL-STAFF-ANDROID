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
import com.genralstaff.databinding.RowProductsBinding
import com.genralstaff.home.AddProductActivity
import com.genralstaff.home.ui.AddShopActivity
import com.genralstaff.responseModel.ShopItemsResponse

class ProductsAdapter(
    var context: Context,
    var shopItems: ArrayList<ShopItemsResponse.Body.Data>
) :
    RecyclerView.Adapter<ProductsAdapter.MyViewHolder>() {

    var onItemClickListener: ((pos: Int) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {

        val binding =
            RowProductsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    @SuppressLint("SuspiciousIndentation", "SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (shopItems[position].product_medias.isNotEmpty()) {
            Glide.with(context).load(imageURL + shopItems[position].product_medias[0].media)
                .placeholder(
                    R.drawable.place_holder
                ).into(holder.binding.rivOrder)
        }
        holder.binding.tvName.text = shopItems[position].name
        holder.binding.tvDateTime.text = shopItems[position].description
        holder.binding.ivActiveShop.text =  shopItems[position].price+"UM"

        holder.binding.ivDelete.setOnClickListener {
            onItemClickListener!!.invoke(position)
        }
        holder.binding.ivEdit.setOnClickListener {
            context.startActivity(
                Intent(context, AddProductActivity::class.java)
                    .putExtra("type", "edit")
                    .putExtra("id", shopItems[position].shop_id.toString())
                    .putExtra("name", shopItems[position].name)
                    .putExtra("id_cat", shopItems[position].type_id)
                    .putExtra("price", shopItems[position].price.toString())
                    .putExtra("description", shopItems[position].description.toString())
                    .putExtra("productId", shopItems[position].id.toString())
                    .putExtra("product_medias", shopItems[position].product_medias)
            )
        }
    }

    override fun getItemCount(): Int {
        return shopItems.size
    }

    class MyViewHolder(val binding: RowProductsBinding) :
        RecyclerView.ViewHolder(binding.root) {}


}
