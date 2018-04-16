@file:Suppress("unused")

package com.infinitytech.mapfoo

import android.util.Log

inline fun <reified T> T.v(message: String) = Log.d(T::class.simpleName, message)
inline fun <reified T> T.w(message: String) = Log.d(T::class.simpleName, message)
inline fun <reified T> T.d(message: String) = Log.d(T::class.simpleName, message)
inline fun <reified T> T.i(message: String) = Log.d(T::class.simpleName, message)
inline fun <reified T> T.e(message: String) = Log.d(T::class.simpleName, message)