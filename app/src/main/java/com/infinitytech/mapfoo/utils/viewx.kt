package com.infinitytech.mapfoo.utils

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

fun View.onClick(init: (v: View) -> Unit) = setOnClickListener { init(it) }

fun View.onFocus(init: (focused: Boolean) -> Unit) = setOnFocusChangeListener { _, hasFocus -> init(hasFocus) }

fun View.onTouch(init: (e: MotionEvent) -> Boolean) {
    setOnTouchListener { _, event ->
        init(event)
    }
}

fun EditText.showKeyBoard() {
    val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (!inputManager.isActive(this)) inputManager.showSoftInput(this, 0)
}

fun EditText.hideKeyBoard() {
    val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (inputManager.isActive(this)) inputManager.hideSoftInputFromWindow(windowToken, 0)
}