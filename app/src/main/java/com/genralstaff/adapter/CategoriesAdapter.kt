package com.genralstaff.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.genralstaff.R
import com.genralstaff.base.LANGUAGE
import com.genralstaff.databinding.RowCategoriesBinding
import com.genralstaff.responseModel.CategoriesListResponse
import com.genralstaff.utils.MyApplication

class CategoriesAdapter(
    var context: Context,
    var categoriesListResponse: ArrayList<CategoriesListResponse.Body>
) :
    RecyclerView.Adapter<CategoriesAdapter.MyViewHolder>() {
    var onItemClickListener: ((pos: Int,type: String) -> Unit)? = null


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {

        val binding =
            RowCategoriesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }
    var dragStartListener: OnStartDragListener? = null

    interface OnStartDragListener {
        fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
    }

    @SuppressLint("SuspiciousIndentation", "SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.apply {

            ivReorder.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    dragStartListener?.onStartDrag(holder)
                }
                false
            }

            val lang = MyApplication.prefs!!.getPrefrenceLanguage(LANGUAGE, "en").toString()
         val   langu = when (lang) {
                "en" -> {
                    0
                }

                "fr" -> {
                    1

                }

                else -> {
                    2

                }
            }
            when (langu) {
                0 -> {
                    // en
                    tvCategories.text = categoriesListResponse[position].name
                }

                1 -> {
                    // fr
                    if (categoriesListResponse[position].name_fr.isEmpty()){
                        tvCategories.text = categoriesListResponse[position].name

                    }
                    else{
                        tvCategories.text = categoriesListResponse[position].name_fr
                    }
                }

                else -> {
                    // ar
                    if (categoriesListResponse[position].name_ar.isEmpty()){
                        tvCategories.text = categoriesListResponse[position].name

                    }
                    else{
                        tvCategories.text = categoriesListResponse[position].name_ar
                    }

                }
            }

            if (categoriesListResponse[position].isselect) {
                ivCheckUncheck.setImageResource(R.drawable.baseline_check_box_24)
            } else {
                ivCheckUncheck.setImageResource(R.drawable.unchec)
            }
            ivDelete.setOnClickListener {
                onItemClickListener?.invoke(position,"delete")
            }
            ivEdit.setOnClickListener {
                onItemClickListener?.invoke(position,"edit")
            }
        }

        holder.itemView.setOnClickListener {
            if (categoriesListResponse[position].isselect) {
                categoriesListResponse[position].isselect = false

            } else {
                for (i in 0 until categoriesListResponse.size) {
                    categoriesListResponse[i].isselect = false
                }
                categoriesListResponse[position].isselect = true

            }
            notifyDataSetChanged()
        }


    }

    override fun getItemCount(): Int {
        return categoriesListResponse.size
    }

    class MyViewHolder(val binding: RowCategoriesBinding) :
        RecyclerView.ViewHolder(binding.root) {}


}
