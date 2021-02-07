package com.example.shoryan.ui

import androidx.databinding.BindingAdapter
import com.facebook.shimmer.ShimmerFrameLayout

class BindingAdapters {

    companion object{
        @JvmStatic
        @BindingAdapter("isShimmerActive")
        fun controlShimmerOperation(shimmerView: ShimmerFrameLayout, shouldBeActive: Boolean){
            if(shouldBeActive) shimmerView.startShimmer() else shimmerView.stopShimmer()
        }
    }
}