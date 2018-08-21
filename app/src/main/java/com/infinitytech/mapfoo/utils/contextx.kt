package com.infinitytech.mapfoo.utils

import android.app.Activity
import android.content.Context
import android.content.Intent

inline fun <reified T : Activity> Context.startActivity() {
    this.startActivity(Intent(this, T::class.java))
}

inline val Context.navigationbarHeight: Int
    get() {
        return resources.getDimensionPixelSize(resources.getIdentifier("navigation_bar_height",
                "dimen", "android"))
    }