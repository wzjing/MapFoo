package com.infinitytech.mapfoo

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.CustomRenderer
import com.amap.api.maps.model.*
import kotlinx.android.synthetic.main.fragment_map.*
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.doAsync
import pl.droidsonroids.gif.GifDrawable
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MapFragment : SwipeBackFragment() {


    private lateinit var marker: Marker
    private lateinit var nextFrame: (count: Int) -> BitmapDescriptor

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        d("onCreateView")
        return attachToSwipeBack(inflater.inflate(R.layout.fragment_map, container, false))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        d("ViewCreated")
        super.onViewCreated(view, savedInstanceState)
        val map = mapView.map
        map.mapType = AMap.MAP_TYPE_NORMAL

        val icons = ArrayList(
                with(GifDrawable(resources, R.drawable.gif_walk)) {
                    (0..numberOfFrames).map {
                        seekToFrameAndGet(it).let {
                            BitmapDescriptorFactory.fromBitmap(Bitmap.createBitmap(it, 0, 0,
                                    it.width, it.height, null, true))
                        }

                    }
                })

        var index = 0
        nextFrame = { count: Int ->
            index += count
            if (index >= icons.size) {
                index -= icons.size
            }
            icons[index]
        }

        // Bike Marker
        marker = map.addMarker(MarkerOptions().apply {
            icon(icons.firstOrNull())
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

        map.setCustomRenderer(object : CustomRenderer {
            private var lastFrameTime = 0L
            @Suppress("MayBeConstant")
            private val timeStamp = 40

            @Suppress("FunctionName")
            override fun OnMapReferencechanged() {
            }

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                TriangleLib.init()
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                TriangleLib.resize(width, height)
                lastFrameTime = System.currentTimeMillis()
            }

            override fun onDrawFrame(gl: GL10?) {
                TriangleLib.step()
                val realStamp: Int = (System.currentTimeMillis() - lastFrameTime).toInt()
                if (realStamp > timeStamp) {
                    i("Refreshing")
                    d("Projection: ${map.projectionMatrix.joinToString { it.toString() }}")
                    d("Camera:     Bearing ${map.cameraPosition.bearing} Tilt ${map.cameraPosition.tilt} Zoom ${map.cameraPosition.zoom}")
                    doAsync @Synchronized {
                        marker.setIcon(nextFrame(realStamp / timeStamp))
                    }
                    lastFrameTime = System.currentTimeMillis()
                }
            }
        })

        val small = LatLngBounds(LatLng(39.84, 116.36), LatLng(39.92, 116.44))
        val big = LatLngBounds(LatLng(39.88, 116.38), LatLng(39.92, 116.42))
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(big, 15))
        BottomSheetBehavior.from(bottomSheet).setBottomSheetCallback(
                object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {}

                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        when (newState) {
                            BottomSheetBehavior.STATE_EXPANDED ->
                                map.animateCamera(CameraUpdateFactory.newLatLngBounds(small, 15))
                            BottomSheetBehavior.STATE_COLLAPSED ->
                                map.animateCamera(CameraUpdateFactory.newLatLngBounds(big, 15))
                        }
                    }
                })

        backBtn.setOnClickListener { fragmentManager?.popBackStack() }
        headerTv.setOnClickListener {
            fragmentManager!!.beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
                    .add(R.id.mapLayout, TextFragment.newInstance(), "textFragment")
                    .addToBackStack("textFragment")
                    .commit()
        }
    }

    override fun onResume() {
        d("onResume")
        super.onResume()
        mapView.onResume()
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation {
        d("onCreateAnimation")
        return try {
            val anim = AnimationUtils.loadAnimation(context, nextAnim)
            anim.setAnimationListener(object : Animation.AnimationListener {

                override fun onAnimationStart(animation: Animation?) {
                    d("onAnimationStart")
                }

                override fun onAnimationEnd(animation: Animation?) {
                    d("onAnimationEnd")
                    mapView.onCreate(null)
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })
            anim
        } catch (e: Resources.NotFoundException) {
            mapView.onCreate(null)
            AnimationUtils.loadAnimation(context, R.anim.fragment_exit)
        }
    }

    override fun onPause() {
        d("onPause")
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroyView() {
        d("onDestroyView")
        mapView.onDestroy()
        super.onDestroyView()
    }

    companion object {
        fun newInstance(): MapFragment {
            return MapFragment()
        }
    }
}
