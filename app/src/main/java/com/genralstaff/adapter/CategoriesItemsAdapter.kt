package com.genralstaff.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.genralstaff.base.LANGUAGE
import com.genralstaff.databinding.RowCategoriesDetailBinding
import com.genralstaff.responseModel.CategoriesResponseNew
import com.genralstaff.utils.MyApplication


class CategoriesItemsAdapter(
    var requireContext: Context,
    var categoriesList: ArrayList<CategoriesResponseNew.Body>
) :
    RecyclerView.Adapter<CategoriesItemsAdapter.MyViewHolder>() {

    var onItemClickListener: ((pos: Int) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {

        val binding =
            RowCategoriesDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    @SuppressLint("SuspiciousIndentation", "SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (categoriesList[position].isSelected) {
            holder.binding.tvName.setTextColor(Color.parseColor("#000000"))
            holder.binding.view.visibility = View.VISIBLE

        } else {
            holder.binding.tvName.setTextColor(Color.parseColor("#74676767"))
            holder.binding.view.visibility = View.GONE

        }

        val lang = MyApplication.prefs!!.getPrefrenceLanguage(LANGUAGE, "ar").toString()
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
               holder.binding.tvName.text = categoriesList[position].name
            }

            1 -> {
                // fr
                val name_fr=categoriesList[position].name_fr?:""

                if (name_fr.isEmpty()){
                   holder.binding.tvName.text = categoriesList[position].name
                }
                else{
                   holder.binding.tvName.text = categoriesList[position].name_fr
                }
            }

            else -> {
                // ar
                val name_ar=categoriesList[position].name_ar?:""
                if (name_ar.isEmpty()){
                   holder.binding.tvName.text = categoriesList[position].name

                }
                else{
                   holder.binding.tvName.text = categoriesList[position].name_ar
                }


            }
        }
        holder.itemView.setOnClickListener {

                categoriesList.forEach {
                    it.isSelected = false
                }

                categoriesList[position].isSelected = true
                onItemClickListener!!.invoke(position)

            notifyDataSetChanged()
        }


    }

    override fun getItemCount(): Int {
        return categoriesList.size
    }

    class MyViewHolder(val binding: RowCategoriesDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {}


}
