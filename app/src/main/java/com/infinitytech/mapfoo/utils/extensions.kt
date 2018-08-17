@file:Suppress("unused")

package com.infinitytech.mapfoo.utils

import android.app.Activity
import android.transition.Transition
import android.support.v4.app.Fragment
import android.util.Log

inline fun <reified T: Activity> T.v(message: String) = Log.d(T::class.simpleName, message)
inline fun <reified T: Activity> T.w(message: String) = Log.d(T::class.simpleName, message)
inline fun <reified T: Activity> T.d(message: String) = Log.d(T::class.simpleName, message)
inline fun <reified T: Activity> T.i(message: String) = Log.d(T::class.simpleName, message)
inline fun <reified T: Activity> T.e(message: String) = Log.d(T::class.simpleName, message)

inline fun <reified T: Fragment> T.v(message: String) = Log.d(T::class.simpleName, message)
inline fun <reified T: Fragment> T.w(message: String) = Log.d(T::class.simpleName, message)
inline fun <reified T: Fragment> T.d(message: String) = Log.d(T::class.simpleName, message)
inline fun <reified T: Fragment> T.i(message: String) = Log.d(T::class.simpleName, message)
inline fun <reified T: Fragment> T.e(message: String) = Log.d(T::class.simpleName, message)

class TransitionListenerX {
    var onStart: ((Transition) -> Unit)? = null
    var onEnd: ((Transition) -> Unit)? = null
    var onCancel: ((Transition) -> Unit)? = null
    var onPause: ((Transition) -> Unit)? = null
    var onResume: ((Transition) -> Unit)? = null

    fun onStart(init: (Transition) -> Unit) {
        this.onStart = init
    }

    fun onEnd(init: (Transition) -> Unit) {
        this.onEnd = init
    }

    fun onCancel(init: (Transition) -> Unit) {
        this.onCancel = init
    }

    fun onPause(init: (Transition) -> Unit) {
        this.onPause = init
    }

    fun onResume(init: (Transition) -> Unit) {
        this.onResume = init
    }
}

fun Transition.addListener(init: TransitionListenerX.() -> Unit) {
    val listenerX = TransitionListenerX()
    listenerX.init()
    this.addListener(object : Transition.TransitionListener {
        override fun onTransitionStart(transition: Transition) = listenerX.onStart?.invoke(transition)
                ?: Unit

        override fun onTransitionEnd(transition: Transition) = listenerX.onEnd?.invoke(transition)
                ?: Unit

        override fun onTransitionPause(transition: Transition) = listenerX.onPause?.invoke(transition)
                ?: Unit

        override fun onTransitionResume(transition: Transition) = listenerX.onResume?.invoke(transition)
                ?: Unit

        override fun onTransitionCancel(transition: Transition) = listenerX.onCancel?.invoke(transition)
                ?: Unit
    })
}
