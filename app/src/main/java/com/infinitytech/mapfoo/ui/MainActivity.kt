package com.infinitytech.mapfoo.ui

import android.os.Bundle
import com.infinitytech.mapfoo.BaseActivity
import com.infinitytech.mapfoo.R
import com.infinitytech.mapfoo.ui.map.MapActivity
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
