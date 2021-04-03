package com.example.shoryan.data

import com.google.gson.annotations.SerializedName

data class PendingRequestResponse(val pendingRequest: String?,
                                  val error: ErrorResponse?)
