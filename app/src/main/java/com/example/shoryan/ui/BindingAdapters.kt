package com.example.shoryan.ui

import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.facebook.shimmer.ShimmerFrameLayout

class BindingAdapters {

    companion object{
        @JvmStatic
        @BindingAdapter("isShimmerActive")
        fun controlShimmerOperation(shimmerView: ShimmerFrameLayout, shouldBeActive: Boolean){
            if(shouldBeActive) shimmerView.startShimmer() else shimmerView.stopShimmer()
        }

        @JvmStatic
        @BindingAdapter("isVisible")
        fun controlViewVisibility(view: View, isVisible: Boolean){
            if(isVisible)
                view.visibility = View.VISIBLE
            else
                view.visibility = View.GONE
        }
    }
}