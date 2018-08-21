package com.infinitytech.mapfoo

import android.annotation.SuppressLint
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import me.yokeyword.fragmentation.SupportActivity


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