package com.infinitytech.mapfoo

import android.annotation.TargetApi
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.amap.api.maps.AMap
import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.transforms.Transform

class MainActivity : AppCompatActivity() {
    private val tag = "MainActivity";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView.onCreate(savedInstanceState)
        val map = mapView.map
        map.mapType = AMap.MAP_TYPE_NORMAL

        map.addMarker(MarkerOptions().apply {
            val gif = GifDrawable(resources, R.drawable.car)
            val icons = ArrayList<BitmapDescriptor>()
            val matrix = Matrix()
            matrix.postScale(1.5f, 1.5f)
            for (i in 0..gif.numberOfFrames) {
                Log.d(tag, "index:$i")
                icons.add(BitmapDescriptorFactory.fromBitmap(gif.seekToFrameAndGet(i).let {
                    Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, true)
                }))
            }
            icons(icons)
            period(4)
            anchor(0.5f, 0.9f)
//            icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
            position(LatLng(39.9, 116.4))
        })
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
