package com.example.myfilimapp.utility

import android.graphics.Bitmap

class Utils {
    companion object{

        fun Long.formatTime(): String {
            val totalSeconds = this / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }

        fun Long.toTimeString(): String {
            val totalSeconds = this / 1000
            return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
        }

        object ClickDebounce {
            private var lastClickTime = 0L
            private const val defaultDelay = 1000L // 1 second

            fun isAllowed(delayMillis: Long = defaultDelay): Boolean {
                val currentTime = System.currentTimeMillis()
                return if (currentTime - lastClickTime >= delayMillis) {
                    lastClickTime = currentTime
                    true
                } else {
                    false
                }
            }
        }


    }
}