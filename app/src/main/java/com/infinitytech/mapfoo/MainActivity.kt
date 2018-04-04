package com.infinitytech.mapfoo

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import me.yokeyword.fragmentation.SupportActivity
import org.jetbrains.anko.sdk25.coroutines.onClick

class MainActivity : SupportActivity() {

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
