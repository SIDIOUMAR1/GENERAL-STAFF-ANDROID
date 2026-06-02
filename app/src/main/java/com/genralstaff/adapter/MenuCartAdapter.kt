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
import java.util.Locale

class MenuCartAdapter(
    private val mContext: Context,
    private var items: List<ShopItemsResponse.Body.Data>,
    private val onQuantityChanged: (total: Double) -> Unit
) : RecyclerView.Adapter<MenuCartAdapter.VH>() {

    private var allLoadedItems = mutableListOf<ShopItemsResponse.Body.Data>()

    private val quantities = HashMap<Int, Int>()
    private var filteredItems = items.toMutableList()

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvProductName)
        val tvPrice: TextView = view.findViewById(R.id.tvProductPrice)
        val tvMinus: TextView = view.findViewById(R.id.tvMinus)
        val tvPlus: TextView = view.findViewById(R.id.tvPlus)
        val tvQty: TextView = view.findViewById(R.id.tvQuantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(mContext).inflate(R.layout.item_menu_product, parent, false))

    override fun getItemCount() = filteredItems.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = filteredItems[position]
        holder.tvName.text = item.name
        holder.tvPrice.text = "${item.price} MRU"
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

    // ✅ Mise à jour des items (changement de catégorie)
    fun updateItems(newItems: List<ShopItemsResponse.Body.Data>) {
        items = newItems
        filteredItems = newItems.toMutableList()
        newItems.forEach { item ->
            if (allLoadedItems.none { it.id == item.id }) {
                allLoadedItems.add(item)
            }
        }

        notifyDataSetChanged()
        onQuantityChanged(calculateTotal())
    }

    // ✅ Recherche par nom
    fun filterGlobal(query: String, allItems: List<ShopItemsResponse.Body.Data>) {
        filteredItems = if (query.isEmpty()) {
            items.toMutableList()
        } else {
            allItems.filter {
                it.name?.lowercase(Locale.getDefault())
                    ?.contains(query.lowercase(Locale.getDefault())) == true
            }.toMutableList()
        }
        notifyDataSetChanged()
    }
    fun addOne(itemId: Int?) {
        if (itemId == null) return
        quantities[itemId] = (quantities[itemId] ?: 0) + 1
        notifyDataSetChanged()
        onQuantityChanged(calculateTotal())
    }

    fun removeOne(itemId: Int?) {
        if (itemId == null) return
        val current = quantities[itemId] ?: 0
        if (current > 0) {
            quantities[itemId] = current - 1
            notifyDataSetChanged()
            onQuantityChanged(calculateTotal())
        }
    }

    private fun calculateTotal(): Double {
        var total = 0.0
        allLoadedItems.forEach { item ->
            val qty = quantities[item.id] ?: 0
            total += qty * (item.price?.toDoubleOrNull() ?: 0.0)
        }
        return total
    }

    // ✅ Message avec support arabe
    fun getCartSummary(isArabic: Boolean = false): String {
        val lines = mutableListOf<String>()
        allLoadedItems.forEach { item ->
            val qty = quantities[item.id] ?: 0
            if (qty > 0) {
                val subtotal = qty * (item.price?.toDoubleOrNull() ?: 0.0)
                lines.add("• ${item.name} x$qty = ${subtotal.toInt()} MRU")
            }
        }
        val total = calculateTotal()
        return if (lines.isEmpty()) ""
        else if (isArabic) {
            "🛒 طلب مقترح :\n${lines.joinToString("\n")}\n─────────────────\n💰 المجموع : ${total.toInt()} MRU"
        } else {
            "🛒 Commande proposée :\n${lines.joinToString("\n")}\n─────────────────\n💰 Total : ${total.toInt()} MRU"
        }
    }

    fun hasItems(): Boolean = quantities.values.any { it > 0 }

    fun getCartCount(): Int = quantities.values.sum()

    fun getCartItems(): List<Pair<ShopItemsResponse.Body.Data, Int>> {
        return quantities.entries
            .filter { it.value > 0 }
            .mapNotNull { (id, qty) ->
                val item = allLoadedItems.firstOrNull { it.id == id }
                if (item != null) Pair(item, qty) else null
            }
    }
    fun getCurrentTotal(): Double = calculateTotal()

    fun getQuantities(): HashMap<Int, Int> = HashMap(quantities)

    fun setQuantities(savedQuantities: HashMap<Int, Int>) {
        quantities.clear()
        quantities.putAll(savedQuantities)
        notifyDataSetChanged()
        onQuantityChanged(calculateTotal())
    }
    fun prefillFromMessage(message: String) {
        val regex = Regex("(.+) x(\\d+) =")
        regex.findAll(message).forEach { match ->
            val productName = match.groupValues[1]
                .trim()
                .replace("• ", "")
                .replace("•", "")
                .trim()
            val qty = match.groupValues[2].toIntOrNull() ?: 0
            if (qty > 0) {
                // ✅ Matching flexible (insensible à la casse et aux espaces)
                val item = items.firstOrNull {
                    it.name?.trim()?.lowercase() == productName.lowercase()
                } ?: items.firstOrNull {
                    it.name?.trim()?.lowercase()?.contains(productName.lowercase()) == true
                }
                item?.let {
                    if (it.id != null) quantities[it.id] = qty
                }
            }
        }
        notifyDataSetChanged()
        onQuantityChanged(calculateTotal())
    }

}