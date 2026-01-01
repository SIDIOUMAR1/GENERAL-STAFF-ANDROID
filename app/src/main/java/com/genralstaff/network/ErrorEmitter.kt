package com.genralstaff.network

interface ErrorEmitter {
    fun onError(message: String)
    fun onError(errorType: ErrorType)
}