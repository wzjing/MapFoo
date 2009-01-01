package com.infinitytech.mapfoo

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.content.Loader
import android.util.Log
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
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.onUiThread
import pl.droidsonroids.gif.GifDrawable
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MapFragment : Fragment(), LoaderManager.LoaderCallbacks<Boolean> {

    private val i = { msg: String -> Log.i(MapFragment::class.simpleName, msg) }
    private val d = { msg: String -> Log.d(MapFragment::class.simpleName, msg) }

    private val ktag = "MyTest"

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d(ktag, "onCreateView")
        return inflater!!.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        Log.d(ktag, "ViewCreated")
        super.onViewCreated(view, savedInstanceState)

        val map = mapView.map
        map.mapType = AMap.MAP_TYPE_NORMAL

        val icons = ArrayList(
                with(GifDrawable(resources, R.drawable.gif_walk)) {
                    (0..numberOfFrames).map {
                        seekToFrameAndGet(it)
                    }.map {
                        BitmapDescriptorFactory.fromBitmap(Bitmap.createBitmap(it, 0, 0, it.width, it.height, null, true))
                    }
                })

        var index = 0
        val nextFrame = { count: Int ->
            index += count
            if (index >= icons.size) {
                index -= (icons.size - 1)
            }
            icons[index]
        }

        // Bike Marker
        val marker = map.addMarker(MarkerOptions().apply {
            icon(icons.firstOrNull())
            anchor(0.5f, 0.9f)
            period(1)
            position(LatLng(39.9, 116.4))
        })

//        // Walk Marker
//        map.addMarker(MarkerOptions().apply {
//            icons(ArrayList(
//                    with(GifDrawable(resources, R.drawable.gif_walk)) {
//                        (0..numberOfFrames).map {
//                            seekToFrameAndGet(it)
//                        }.map {
//                            BitmapDescriptorFactory.fromBitmap(Bitmap.createBitmap(it, 0, 0, it.width, it.height, null, true))
//                        }
//                    }))
//            period(20)
//            anchor(0.5f, 0.9f)
//            position(LatLng(39.9, 116.39))
//        })
//
//        // Car Marker
//        map.addMarker(MarkerOptions().apply {
//            icons(ArrayList(
//                    with(GifDrawable(resources, R.drawable.gif_car)) {
//                        (0..numberOfFrames).map {
//                            seekToFrameAndGet(it)
//                        }.map {
//                            BitmapDescriptorFactory.fromBitmap(Bitmap.createBitmap(it, 0, 0, it.width, it.height, null, true))
//                        }
//                    }))
//            period(20)
//            anchor(0.5f, 0.9f)
//            position(LatLng(39.9, 116.41))
//        })

        map.addPolyline(PolylineOptions().apply {
            color(Color.RED)
            width(5f)
            var latitude = 39.9
            var longitude = 116.4
            val point = LatLng(latitude, longitude)
            add(point)
            for (i in 0..800) {
                latitude += 0.1 * (Math.random() - 0.5)
                longitude += 0.01
                add(LatLng(latitude, longitude))
            }
        })

        map.setCustomRenderer(object : CustomRenderer {
            private var lastFrameTime = 0L
            private val timeStamp = 30

            override fun OnMapReferencechanged() {}

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                lastFrameTime = System.currentTimeMillis()
            }

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            }

            override fun onDrawFrame(gl: GL10?) {
                val realStamp: Int = (System.currentTimeMillis() - lastFrameTime).toInt()
                if (realStamp > timeStamp) {
                    i("Refreshing")
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
        BottomSheetBehavior.from(bottomSheet).setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> map.animateCamera(CameraUpdateFactory.newLatLngBounds(small, 15))
                    BottomSheetBehavior.STATE_COLLAPSED -> map.animateCamera(CameraUpdateFactory.newLatLngBounds(big, 15))
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}

        })

        backBtn.setOnClickListener { fragmentManager.popBackStack() }

    }

    override fun onResume() {
        Log.d(ktag, "onResume")
        super.onResume()
        mapView.onResume()
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation {
        Log.d(ktag, "onCreateAnimation")
        return try {
            val anim = AnimationUtils.loadAnimation(context, nextAnim)
            anim.setAnimationListener(object : Animation.AnimationListener {

                override fun onAnimationStart(animation: Animation?) {
                    Log.d(ktag, "onAnimationStart")
                }

                override fun onAnimationEnd(animation: Animation?) {
                    Log.d(ktag, "onAnimationEnd")
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
        Log.d(ktag, "onPause")
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroyView() {
        Log.d(ktag, "onDestroyView")
        mapView.onDestroy()
        super.onDestroyView()
    }

    /* --------------------Data----------------------- */
    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Boolean> {
        return MapLoader(context)
    }

    override fun onLoaderReset(loader: Loader<Boolean>?) {
    }

    override fun onLoadFinished(loader: Loader<Boolean>?, data: Boolean?) {
    }

    companion object {
        fun newInstance(): MapFragment {
            return MapFragment()
        }
    }
}

class MapLoader(context: Context) : AsyncTaskLoader<Boolean>(context) {
    override fun loadInBackground(): Boolean {
        return true
    }

}
