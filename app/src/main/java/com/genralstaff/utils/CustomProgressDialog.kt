package com.genralstaff.utils

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import com.genralstaff.databinding.ProgressDialogViewBinding


class CustomProgressDialog {
    private var dialog: Dialog? = null

    fun show(context: Context) {
        val binding = ProgressDialogViewBinding.inflate(LayoutInflater.from(context))

        dialog = Dialog(context)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent) // Important!!
        dialog?.setContentView(binding.root)
        dialog?.setCancelable(false)

        val rotationAnimator = ObjectAnimator.ofFloat(
            binding.loaderDots,
            View.ROTATION,
            0f,
            360f
        ).apply {
            duration = 1200L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
        }
        rotationAnimator.start()

        dialog?.show()
    }

    fun hide() {
        dialog?.dismiss()
    }
}
