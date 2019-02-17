package com.infinitytech.mapfoo

import android.annotation.SuppressLint
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment


@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {

    fun push(@IdRes container: Int, fragment: Fragment, tag: String = fragment::class.java.simpleName) {
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
                .apply {
                    if (supportFragmentManager.fragments.isEmpty())
                        add(container, fragment, tag)
                    else
                        replace(container, fragment, tag)
                }
                .addToBackStack(tag)
                .commit()
    }
}