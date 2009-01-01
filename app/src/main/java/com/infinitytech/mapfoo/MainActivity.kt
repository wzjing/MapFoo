package com.infinitytech.mapfoo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        goBtn.onClick {
            if (supportFragmentManager.fragments.isEmpty()) {
                supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
                        .add(R.id.mapLayout, MapFragment.newInstance(), "mapFragment")
                        .addToBackStack("mapFragment")
                        .commit()
            }
        }
    }
}
