package com.infinitytech.mapfoo.utils

import android.view.View

fun View.onclick(init: (v: View) -> Unit) = setOnClickListener { init(it) }

fun View.onFocus(init: (focused: Boolean) -> Unit) = setOnFocusChangeListener { _, hasFocus -> init(hasFocus) }