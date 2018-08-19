package com.infinitytech.mapfoo

import android.os.Bundle
import com.infinitytech.mapfoo.items.AddressActivity
import com.infinitytech.mapfoo.items.MapFragment
import com.infinitytech.mapfoo.utils.onClick
import com.infinitytech.mapfoo.utils.startActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapBtn.onClick {
            push(R.id.fragmentLayout, MapFragment.newInstance())
        }

        locationBtn.onClick {
            startActivity<AddressActivity>()
        }
    }
}
