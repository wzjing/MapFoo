package com.infinitytech.mapfoo

import android.os.Bundle
import com.infinitytech.mapfoo.items.AddressFragment
import com.infinitytech.mapfoo.items.MapFragment
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapBtn.onClick {
            push(R.id.fragmentLayout, MapFragment.newInstance())
        }

        locationBtn.setOnClickListener {
            push(R.id.fragmentLayout, AddressFragment.newInstance())
        }
    }
}
