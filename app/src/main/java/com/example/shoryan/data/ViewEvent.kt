package com.example.shoryan.data

sealed class ViewEvent{
    data class ShowSnackBar(val stringResource: Int): ViewEvent()
    data class ShowTryAgainSnackBar(val text: String = "فشل في الاتصال بالشبكة"): ViewEvent()
    data class ErrorHandler(val error: ServerError): ViewEvent()
    object DismissFragment: ViewEvent()
}
