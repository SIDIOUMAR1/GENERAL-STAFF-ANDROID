package com.genralstaff.network

enum class ErrorType {
    NETWORK, // IO
    TIMEOUT, // Socket
    UNKNOWN, //Anything else
    UNAUTHORIZED
}
