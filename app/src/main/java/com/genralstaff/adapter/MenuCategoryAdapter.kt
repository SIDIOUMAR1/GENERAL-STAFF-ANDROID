package com.genralstaff.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.genralstaff.R
import com.genralstaff.responseModel.CategoriesListResponse

class MenuCategoryAdapter(
    private val mContext: Context,
    private val categories: List<CategoriesListResponse.Body>,
    private val onCategoryClick: (categoryId: String) -> Unit
) : RecyclerView.Adapter<MenuCategoryAdapter.VH>() {

    private var selectedPosition = 0

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(R.id.tvCategoryName)
        val vUnderline: View = view.findViewById(R.id.vUnderline)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(mContext).inflate(R.layout.item_menu_category, parent, false))

    override fun getItemCount() = categories.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val cat = categories[position]
        holder.tvCategory.text = cat.name ?: ""
        val isSelected = position == selectedPosition
        holder.tvCategory.setTextColor(
            ContextCompat.getColor(
                mContext,
                if (isSelected) R.color.app_theme_color else R.color.grey
            )
        )
        holder.vUnderline.visibility = if (isSelected) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos == RecyclerView.NO_ID.toInt()) return@setOnClickListener
            val prev = selectedPosition
            selectedPosition = pos
            notifyItemChanged(prev)
            notifyItemChanged(pos)
            onCategoryClick(categories[pos].id.toString())
        }
    }
}