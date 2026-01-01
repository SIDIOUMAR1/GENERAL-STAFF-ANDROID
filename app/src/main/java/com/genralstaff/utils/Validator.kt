package com.genralstaff.utils

import android.app.Activity
import com.genralstaff.R
import com.genralstaff.databinding.ActivityLoginBinding
import com.genralstaff.utils.Utils.showErrorDialog


object Validator {

    fun isValidMobile(phone: String): Boolean {
        return if (phone == null || phone.length < 8 ) {
            false;
        } else {
            android.util.Patterns.PHONE.matcher(phone).matches();
        }
    }

    fun userLoginValidation(context: Activity, binding: ActivityLoginBinding): Boolean {
        var check = false
        when {

            binding.edNumber.text.toString().trim().isEmpty() -> {
                showErrorDialog(context, context.getString(R.string.please_enter_phone_number))
            }

            !isValidMobile(binding.edNumber.text.toString().trim()) -> {
                showErrorDialog(
                    context, context.getString(R.string.please_enter_valid_phone_number)
                )
            }
            binding.edPass.text.toString().trim().isEmpty() -> {
                showErrorDialog(context, context.getString(R.string.please_enter_password))
            }
            else -> {
                check = true
            }
        }

        return check
    }

}