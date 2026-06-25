package com.genralstaff.adapter

import android.content.Context
import androidx.fragment.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.genralstaff.R
import com.genralstaff.responseModel.ShopItemsResponse
import com.genralstaff.utils.StaffCartLine
import java.util.Locale

class MenuCartAdapter(
    private val mContext: Context,
    private var items: List<ShopItemsResponse.Body.Data>,
    private val onQuantityChanged: (total: Double) -> Unit
) : RecyclerView.Adapter<MenuCartAdapter.VH>() {

    private var allLoadedItems = mutableListOf<ShopItemsResponse.Body.Data>()
    private var filteredItems = items.toMutableList()

    // ✅ Chaque ligne représente un produit + ses options sélectionnées (peut y avoir plusieurs lignes du même produit avec options différentes)
    private val cartLinesList = mutableListOf<StaffCartLine>()

    // ✅ Callback pour ouvrir le sheet d'options (géré par ChatActivity)
    var onRequestOptions: ((ShopItemsResponse.Body.Data) -> Unit)? = null

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

    private fun quantityForProduct(productId: Int?): Int {
        return cartLinesList.filter { it.product.id == productId }.sumOf { it.quantity }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = filteredItems[position]
        holder.tvName.text = item.name
        holder.tvPrice.text = "${item.price} MRU"
        val qty = quantityForProduct(item.id)
        holder.tvQty.text = qty.toString()

        val hasOptions = !item.option_groups.isNullOrEmpty()
        holder.tvMinus.visibility = if (hasOptions) View.GONE else View.VISIBLE

        holder.tvPlus.setOnClickListener {
            if (hasOptions) {
                onRequestOptions?.invoke(item)
            } else {
                addOne(item)
                notifyItemChanged(position)
                onQuantityChanged(calculateTotal())
            }
        }

        holder.tvMinus.setOnClickListener {
            removeOne(item.id)
            notifyItemChanged(position)
            onQuantityChanged(calculateTotal())
        }
    }

    // ✅ Ajouter une ligne SANS options (incrémente si une ligne sans options existe déjà)
    fun addOne(product: ShopItemsResponse.Body.Data) {
        val existing = cartLinesList.find { it.product.id == product.id && it.selectedOptions.isEmpty() }
        if (existing != null) {
            existing.quantity++
        } else {
            cartLinesList.add(StaffCartLine(product, 1, emptyList()))
            if (allLoadedItems.none { it.id == product.id }) allLoadedItems.add(product)
        }
    }

    // ✅ Ajouter une ligne AVEC options (toujours une nouvelle ligne)
    fun addLineWithOptions(product: ShopItemsResponse.Body.Data, options: List<com.genralstaff.utils.SelectedOptionGroup>, quantity: Int) {
        cartLinesList.add(StaffCartLine(product, quantity, options))
        if (allLoadedItems.none { it.id == product.id }) allLoadedItems.add(product)
        notifyDataSetChanged()
        onQuantityChanged(calculateTotal())
    }

    fun removeOne(productId: Int?) {
        val existing = cartLinesList.find { it.product.id == productId && it.selectedOptions.isEmpty() }
        if (existing != null) {
            if (existing.quantity > 1) existing.quantity--
            else cartLinesList.remove(existing)
        }
    }

    fun removeLine(line: StaffCartLine) {
        cartLinesList.remove(line)
        notifyDataSetChanged()
        onQuantityChanged(calculateTotal())
    }

    fun updateLine(oldLine: StaffCartLine, newLine: StaffCartLine) {
        val index = cartLinesList.indexOf(oldLine)
        if (index != -1) {
            cartLinesList[index] = newLine
            notifyDataSetChanged()
            onQuantityChanged(calculateTotal())
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

    private fun calculateTotal(): Double = cartLinesList.sumOf { it.totalPrice }

    fun getCartSummary(isArabic: Boolean = false): String {
        val lines = mutableListOf<String>()
        cartLinesList.filter { it.quantity > 0 }.forEach { line ->
            var text = "• ${line.product.name} x${line.quantity}"
            if (line.optionsSummary.isNotEmpty()) {
                text += " (${line.optionsSummary})"
            }
            text += " = ${line.totalPrice.toInt()} MRU"
            lines.add(text)
        }
        val total = calculateTotal()
        return if (lines.isEmpty()) ""
        else if (isArabic) {
            "🛒 طلب مقترح :\n${lines.joinToString("\n")}\n─────────────────\n💰 المجموع : ${total.toInt()} MRU"
        } else {
            "🛒 Commande proposée :\n${lines.joinToString("\n")}\n─────────────────\n💰 Total : ${total.toInt()} MRU"
        }
    }

    fun hasItems(): Boolean = cartLinesList.any { it.quantity > 0 }

    fun getCartCount(): Int = cartLinesList.sumOf { it.quantity }

    fun getCartLines(): List<StaffCartLine> = cartLinesList.filter { it.quantity > 0 }

    fun getCurrentTotal(): Double = calculateTotal()

    // ✅ Sauvegarder/restaurer le panier entre changements de catégorie
    fun getSavedLines(): List<StaffCartLine> = cartLinesList.toList()

    fun setSavedLines(savedLines: List<StaffCartLine>) {
        cartLinesList.clear()
        cartLinesList.addAll(savedLines)
        notifyDataSetChanged()
        onQuantityChanged(calculateTotal())
    }

    // ✅ Pré-remplissage simplifié (sans options) à partir d'un message précédent
    fun prefillFromMessage(message: String) {
        val regex = Regex("(.+) x(\\d+)(?:\\s*\\(.*?\\))? =")
        regex.findAll(message).forEach { match ->
            val productName = match.groupValues[1].trim().replace("• ", "").replace("•", "").trim()
            val qty = match.groupValues[2].toIntOrNull() ?: 0
            if (qty > 0) {
                val item = items.firstOrNull {
                    it.name?.trim()?.lowercase() == productName.lowercase()
                } ?: items.firstOrNull {
                    it.name?.trim()?.lowercase()?.contains(productName.lowercase()) == true
                }
                item?.let {
                    val existing = cartLinesList.find { l -> l.product.id == it.id && l.selectedOptions.isEmpty() }
                    if (existing != null) existing.quantity = qty
                    else cartLinesList.add(StaffCartLine(it, qty, emptyList()))
                }
            }
        }
        notifyDataSetChanged()
        onQuantityChanged(calculateTotal())
    }
}