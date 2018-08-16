package com.infinitytech.mapfoo

import android.os.Bundle
import com.infinitytech.mapfoo.items.LocationSelectorFragment
import com.infinitytech.mapfoo.items.MapFragment
import kotlinx.android.synthetic.main.activity_main.*
import me.yokeyword.fragmentation.SupportActivity
import org.jetbrains.anko.sdk25.coroutines.onClick

class MainActivity : SupportActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapBtn.onClick {
            if (supportFragmentManager.fragments.isEmpty()) {
                supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
                        .add(R.id.fragmentLayout, MapFragment.newInstance(), "mapFragment")
                        .addToBackStack("mapFragment")
                        .commit()
            }
        }

        locationBtn.setOnClickListener {
            if (supportFragmentManager.fragments.isEmpty()) {
                supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
                        .add(R.id.fragmentLayout, LocationSelectorFragment.newInstance(), "mapFragment")
                        .addToBackStack("mapFragment")
                        .commit()
            }
        }
    }
}
