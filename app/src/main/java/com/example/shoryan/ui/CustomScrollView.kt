package com.example.shoryan.ui

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.core.widget.NestedScrollView


class CustomScrollView(context: Context, attrs: AttributeSet?) : NestedScrollView(context, attrs) {

        override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    Log.i("CustomScrollView", "onInterceptTouchEvent: DOWN super false" )
                    super.onTouchEvent(ev)
                }
                MotionEvent.ACTION_MOVE -> return false // Don't intercept the motion
                MotionEvent.ACTION_CANCEL -> {
                    Log.i("CustomScrollView", "onInterceptTouchEvent: CANCEL super false")
                    super.onTouchEvent(ev)
                }
                MotionEvent.ACTION_UP -> {
                    Log.i("CustomScrollView", "onInterceptTouchEvent: UP false")
                    return false
                }
                else -> {
                }
            }
            return false
        }

        override fun onTouchEvent(ev: MotionEvent): Boolean {
            super.onTouchEvent(ev)
            return true
        }
    }