package com.example.shoryan.ui

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.shoryan.R
import com.example.shoryan.interfaces.LoadingFragmentHolder

class LoadingFragment(private val loadingFragmentHolder: LoadingFragmentHolder) : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setDimAmount(0.8f)
        return inflater.inflate(R.layout.fragment_loading, container, false)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        loadingFragmentHolder.onLoadingFragmentDismissed()
    }
}