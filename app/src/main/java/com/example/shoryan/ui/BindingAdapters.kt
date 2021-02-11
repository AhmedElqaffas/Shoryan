package com.example.shoryan.ui

import android.view.View
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.textfield.TextInputLayout

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


        @JvmStatic
        @BindingAdapter("setAdapter")
        fun RecyclerView.bindRecyclerViewAdapter(adapter: RecyclerView.Adapter<*>) {
            this.run {
                this.adapter = adapter
            }
        }

        @JvmStatic
        @BindingAdapter(value = ["errorMessage", "errorCondition"])
        fun TextInputLayout.observeError(errorMessage: String, errorCondition: Boolean) {
            if(errorCondition)
                this.error = errorMessage
            else
                this.error = ""
        }

        @JvmStatic
        @BindingAdapter("onTextChanged")
        fun EditText.onTextChangedListener(operation: (Any) -> Unit) {
            this.addTextChangedListener{
                operation(this)
            }
        }
    }
}