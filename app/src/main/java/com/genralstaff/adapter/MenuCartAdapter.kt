package com.genralstaff.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.genralstaff.R
import com.genralstaff.base.imageURL
import com.genralstaff.responseModel.ShopItemsResponse

class MenuCartAdapter(
    private val mContext: Context,
    private val items: List<ShopItemsResponse.Body.Data>,
    private val onQuantityChanged: (total: Double) -> Unit
) : RecyclerView.Adapter<MenuCartAdapter.VH>() {

    private val quantities = HashMap<Int, Int>()

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivProduct: ImageView = view.findViewById(R.id.ivProduct)
        val tvName: TextView = view.findViewById(R.id.tvProductName)
        val tvPrice: TextView = view.findViewById(R.id.tvProductPrice)
        val tvMinus: TextView = view.findViewById(R.id.tvMinus)
        val tvPlus: TextView = view.findViewById(R.id.tvPlus)
        val tvQty: TextView = view.findViewById(R.id.tvQuantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(mContext).inflate(R.layout.item_menu_product, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvName.text = item.name
        holder.tvPrice.text = "${item.price} MRU"
        Glide.with(mContext).load(imageURL + item.image)
            .placeholder(R.drawable.place_holder)
            .into(holder.ivProduct)

        val qty = quantities[item.id] ?: 0
        holder.tvQty.text = qty.toString()

        holder.tvPlus.setOnClickListener {
            val newQty = (quantities[item.id] ?: 0) + 1
            quantities[item.id] = newQty
            holder.tvQty.text = newQty.toString()
            onQuantityChanged(calculateTotal())
        }

        holder.tvMinus.setOnClickListener {
            val current = quantities[item.id] ?: 0
            if (current > 0) {
                quantities[item.id] = current - 1
                holder.tvQty.text = (current - 1).toString()
                onQuantityChanged(calculateTotal())
            }
        }
    }

    private fun calculateTotal(): Double {
        var total = 0.0
        items.forEach { item ->
            val qty = quantities[item.id] ?: 0
            total += qty * (item.price?.toDoubleOrNull() ?: 0.0)
        }
        return total
    }

    fun getCartSummary(): String {
        val lines = mutableListOf<String>()
        items.forEach { item ->
            val qty = quantities[item.id] ?: 0
            if (qty > 0) {
                val subtotal = qty * (item.price?.toDoubleOrNull() ?: 0.0)
                lines.add("• ${item.name} x$qty = ${subtotal.toInt()} MRU")
            }
        }
        val total = calculateTotal()
        return if (lines.isEmpty()) ""
        else "🛒 Commande proposée :\n${lines.joinToString("\n")}\n─────────────────\n💰 Total : ${total.toInt()} MRU"
    }

    fun hasItems(): Boolean = quantities.values.any { it > 0 }
}