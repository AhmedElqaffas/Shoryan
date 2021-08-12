package com.example.shoryan.ui

import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shoryan.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.textfield.TextInputLayout
import java.util.*

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

        @JvmStatic
        @BindingAdapter("localeTextIcon")
        fun Button.setLocaleButtonTextAndIcon(locale: Locale){
            when(locale){
                Locale.ENGLISH -> {
                    this.text = "EN"
                    val drawable = ResourcesCompat.getDrawable(resources, R.mipmap.ic_uk_flag,null)
                    this.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
                }
                Locale.forLanguageTag("ar") -> {
                    this.text = "AR"
                    val drawable = ResourcesCompat.getDrawable(resources, R.mipmap.ic_egypt_flag, null)
                    this.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
                }
            }
        }
    }
}