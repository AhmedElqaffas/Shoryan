package com.example.shoryan.data

data class ErrorResponse(val message: ServerError,
                         val status: Short? = null)
