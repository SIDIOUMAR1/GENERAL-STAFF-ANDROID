package com.genralstaff.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.genralstaff.network.ErrorEmitter
import com.genralstaff.network.ErrorType
import com.genralstaff.network.UIEventManager



open class BaseViewModel() : ViewModel(), UIEventManager, ErrorEmitter {

    private val progressDialog = MutableLiveData<Boolean>()
    fun progressDialogData(): LiveData<Boolean> = progressDialog

    private val showError = MutableLiveData<String>()
    fun getError(): LiveData<String> = showError

    private val showErrorCode = MutableLiveData<ErrorType>()
    fun onShowErrorCode(): LiveData<ErrorType> = showErrorCode

    override fun showProgress() {
        progressDialog.value = true
    }

    override fun hideProgress() {
        progressDialog.value = false
    }

    override fun showErrorMessage(error: String): String {
        showError.value = error
        return error
    }

    override fun onError(msg: String) {
        showError.value = msg
    }

    override fun onError(errorType: ErrorType) {
        showErrorCode.value = errorType
    }

}
