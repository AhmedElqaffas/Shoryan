package com.example.shoryan.data

sealed class ViewEvent{
    data class ShowSnackBar(val stringResource: Int): ViewEvent()
    data class ShowTryAgainSnackBar(val text: String = "فشل في الاتصال بالشبكة"): ViewEvent()
    object DismissFragment: ViewEvent()
}
