package com.infinitytech.mapfoo.utils

import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

private const val TAG = "viewx"

// Events

fun View.onClick(init: (v: View) -> Unit) = setOnClickListener { init(it) }

fun View.onFocus(init: (focused: Boolean) -> Unit) = setOnFocusChangeListener { _, hasFocus -> init(hasFocus) }

fun View.onTouch(init: (e: MotionEvent) -> Boolean) {
    setOnTouchListener { _, event ->
        init(event)
    }
}

// keyboard

fun EditText.showKeyBoard() {
    Log.d(TAG, "showKeyboard")
    val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (!inputManager.isActive(this)) inputManager.showSoftInput(this, 0)
}

fun EditText.hideKeyBoard() {
    val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    Log.d(TAG, "hideKeyboard ${if (inputManager.isActive(this)) "Active" else "UnActive"}")
    inputManager.hideSoftInputFromWindow(windowToken, 0)
}

// Layout

var View.startMargin: Int
    get() = (layoutParams as ViewGroup.MarginLayoutParams).marginStart
    set(value) {
        (layoutParams as ViewGroup.MarginLayoutParams).marginStart = value
    }

var View.endMargin: Int
    get() = (layoutParams as ViewGroup.MarginLayoutParams).marginEnd
    set(value) {
        (layoutParams as ViewGroup.MarginLayoutParams).marginEnd = value
    }

var View.topMargin: Int
    get() = (layoutParams as ViewGroup.MarginLayoutParams).topMargin
    set(value) {
        (layoutParams as ViewGroup.MarginLayoutParams).topMargin = value
    }

var View.bottomMargin: Int
    get() = (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
    set(value) {
        (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = value
    }