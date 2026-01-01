package com.genralstaff.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.viewpager2.widget.ViewPager2
import com.genralstaff.R
import com.genralstaff.adapter.WalkthroughViewPager
import com.genralstaff.databinding.ActivityWalkthroughBinding
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle

class WalkthroughActivity : AppCompatActivity() {
    var bannerImage = ArrayList<WelcomeModel>()

    data class WelcomeModel(var image: Int)

    private val homeViewHolder by lazy { WalkthroughViewPager(this, bannerImage) }
    lateinit var binding: ActivityWalkthroughBinding
    var auto = 0

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = ActivityWalkthroughBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setAdapters()

        binding.btnNext.setOnClickListener {

            // login activity
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        binding.viewPaser.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                binding.indicatorView.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (auto == 0) {
                    when (position) {
                        0 -> {
//                            binding.tvSkip.visibility = View.VISIBLE
                            binding.tvOne.text = getString(R.string.fastest)
                            binding.tvTwo.text = getString(R.string.delivery)
                        }

                        else -> {
                            binding.tvOne.text = getString(R.string.get_your_order)
                            binding.tvTwo.text = getString(R.string.online)

                        }
                    }

                }
                binding.indicatorView.onPageSelected(position)

            }
        })
    }

    private fun setAdapters() {
        // home viewpager adapter
        bannerImage.clear()
        bannerImage.add(WelcomeModel(R.drawable.walkthrough_one))
        bannerImage.add(WelcomeModel(R.drawable.walkthrough_two))

        binding.viewPaser.adapter = homeViewHolder
        binding.indicatorView.setupWithViewPager(binding.viewPaser)

        binding.indicatorView.apply {
            setSliderColor(Color.parseColor("#d3d3d4"), Color.parseColor("#FF4A59"))
            setSliderWidth(resources.getDimension(com.intuit.sdp.R.dimen._8sdp))
            setSliderHeight(resources.getDimension(com.intuit.sdp.R.dimen._8sdp))
            setSlideMode(IndicatorSlideMode.NORMAL)
            setIndicatorStyle(IndicatorStyle.ROUND_RECT)
            setPageSize(binding.viewPaser.adapter!!.itemCount)
            notifyDataChanged()
        }
    }
}