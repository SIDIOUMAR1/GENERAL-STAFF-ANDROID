package com.genralstaff.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.genralstaff.R
import com.genralstaff.auth.WalkthroughActivity
import com.genralstaff.databinding.ItemsHomeViewpagerBinding


class WalkthroughViewPager(var context : Context ,var bannerImage:ArrayList<WalkthroughActivity.WelcomeModel>): RecyclerView.Adapter<WalkthroughViewPager.HomeViewHolder>() {
    inner class HomeViewHolder(private val binding: ItemsHomeViewpagerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pos: Int) {
//            when (pos) {
//                0 -> {
//                    binding.vBack.setBackgroundColor(Color.parseColor("#FFFCE9"))
//                }
//                1 -> {
//                    binding.vBack.setBackgroundColor(Color.parseColor("#E9F9FF"))
//                }
//                else -> {
//                    binding.vBack.setBackgroundColor(Color.parseColor("#FFE9E9"))
//                }
//            }


            Glide.with(context).load(bannerImage[pos]?.image).apply(
                        RequestOptions.placeholderOf(
                            R.drawable.place_holder
                        )
                    ).into(binding.ivSlider)

//            binding.btnNext.setOnClickListener {  }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder(
            ItemsHomeViewpagerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return bannerImage.size
    }
}
