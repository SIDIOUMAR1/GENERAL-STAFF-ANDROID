package com.genralstaff.home.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.genralstaff.R
import com.genralstaff.databinding.ActivityFullScreenImageBinding

class FullScreenImageActivity : AppCompatActivity() {
    lateinit var binding: ActivityFullScreenImageBinding
    var url = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullScreenImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        url = intent.getStringExtra("url").toString()
        Glide.with(this).load(url)
            .placeholder(R.drawable.place_holder).into(binding.ivImage)
        binding.ivCross.setOnClickListener {
            finish()
        }
    }
}