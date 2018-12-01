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

var Context.adddresses: Array<Address>
    get() {
        val sp = getSharedPreferences("Addresses", Context.MODE_PRIVATE)
        return sp.getStringSet("addresses", emptySet())?.map { Address.fromString(it) }?.toTypedArray()
                ?: emptyArray()
    }
    set(value) {
        val sp = getSharedPreferences("Addresses", Context.MODE_PRIVATE)
        val editor = sp.edit()
        val old = sp.getStringSet("Addresses", emptySet()) ?: emptySet()

        val new = ArrayList(old)
//        new.add(value.map { it.toString() })
//        editor.putStringSet("Addresses", result)
        editor.apply()
    }