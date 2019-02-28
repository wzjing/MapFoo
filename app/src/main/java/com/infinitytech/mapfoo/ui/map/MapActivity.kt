package com.infinitytech.mapfoo.ui.map

import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.infinitytech.mapfoo.BaseActivity
import com.infinitytech.mapfoo.R
import com.infinitytech.mapfoo.utils.d
import kotlinx.android.synthetic.main.activity_map.*

class MapActivity : BaseActivity() {


    private lateinit var marker: Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        setContentView(R.layout.activity_map)

        val map = mapView.map
        // basic configure
        map.apply {
            mapType = AMap.MAP_TYPE_NORMAL
            uiSettings.apply {
                isScaleControlsEnabled = false
                isZoomControlsEnabled = false
            }
        }

        // Bike Marker
        marker = map.addMarker(MarkerOptions().apply {
            icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_round))
            anchor(0.5f, 0.9f)
            position(LatLng(39.9, 116.4))
        })

        map.addPolyline(PolylineOptions().apply {
            color(Color.RED)
            width(5f)
            var latitude = 39.9
            var longitude = 116.4
            val point = LatLng(latitude, longitude)
            add(point)
            for (i in 0..400) {
                latitude += 0.1 * (Math.random() - 0.5)
                longitude += 0.01
                add(LatLng(latitude, longitude))
            }
        })

        map.setCustomRenderer(JNIRenderer())

        val small = LatLngBounds(LatLng(39.84, 116.36), LatLng(39.92, 116.44))
        val big = LatLngBounds(LatLng(39.88, 116.38), LatLng(39.92, 116.42))
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(big, 16))
        map.moveCamera(CameraUpdateFactory.changeTilt(50f))
        map.moveCamera(CameraUpdateFactory.zoomTo(18f))
        mapView.onCreate(null)

        BottomSheetBehavior.from(bottomSheet).setBottomSheetCallback(
                object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {}

                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        when (newState) {
                            BottomSheetBehavior.STATE_EXPANDED ->
                                map.animateCamera(CameraUpdateFactory.newLatLngBounds(small, 15))
                            BottomSheetBehavior.STATE_COLLAPSED ->
                                map.animateCamera(CameraUpdateFactory.newLatLngBounds(big, 15))
                            else -> return
                        }
                    }
                })

        backBtn.setOnClickListener { finish() }
    }

    override fun onResume() {
        d("onResume")
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        d("onPause")
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        d("onDestroyView")
        mapView.onDestroy()
        super.onDestroy()
    }

//    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation {
//        d("onCreateAnimation")
//        return try {
//            val anim = AnimationUtils.loadAnimation(context, nextAnim)
//            anim.setAnimationListener(object : Animation.AnimationListener {
//
//                override fun onAnimationStart(animation: Animation?) {
//                    d("onAnimationStart")
//                }
//
//                override fun onAnimationEnd(animation: Animation?) {
//                    d("onAnimationEnd")
//                    mapView.onCreate(null)
//                }
//
//                override fun onAnimationRepeat(animation: Animation?) {}
//            })
//            anim
//        } catch (e: Resources.NotFoundException) {
//            mapView.onCreate(null)
//            AnimationUtils.loadAnimation(context, R.anim.fragment_exit)
//        }
//    }
}
