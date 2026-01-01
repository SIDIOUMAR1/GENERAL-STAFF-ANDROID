package com.genralstaff.utils

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.genralstaff.adapter.CategoriesAdapter
import java.util.Collections

class ItemMoveCallback(
    private val adapter: CategoriesAdapter
) : ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled(): Boolean = false
    override fun isItemViewSwipeEnabled(): Boolean = false

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val fromPos = viewHolder.adapterPosition
        val toPos = target.adapterPosition

        Collections.swap(adapter.categoriesListResponse, fromPos, toPos)
        adapter.notifyItemMoved(fromPos, toPos)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // not needed
    }
}
