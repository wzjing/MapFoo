package com.infinitytech.mapfoo

import android.os.Bundle
import com.infinitytech.mapfoo.items.AddressActivity
import com.infinitytech.mapfoo.items.MapActivity
import com.infinitytech.mapfoo.utils.onClick
import com.infinitytech.mapfoo.utils.startActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapBtn.onClick {
            startActivity<MapActivity>()
        }

        locationBtn.onClick {
            startActivity<AddressActivity>()
        }
    }
}
