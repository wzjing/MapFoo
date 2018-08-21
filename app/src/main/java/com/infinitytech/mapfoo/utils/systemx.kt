package com.infinitytech.mapfoo.utils

import android.os.Build

fun minApi(api: Int, init: () -> Unit) {
    if (Build.VERSION.SDK_INT >= api) init()
}