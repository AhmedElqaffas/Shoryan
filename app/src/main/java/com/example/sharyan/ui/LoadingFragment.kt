package com.example.sharyan.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.sharyan.R

class LoadingFragment(private val loadingFragmentHolder: LoadingFragmentHolder) : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_loading, container, false)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        loadingFragmentHolder.onLoadingFragmentDismissed()
    }
}