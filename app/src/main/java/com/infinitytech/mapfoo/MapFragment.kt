package com.infinitytech.mapfoo

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
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
import com.amap.api.maps.CustomRenderer
import com.amap.api.maps.model.*
import kotlinx.android.synthetic.main.fragment_map.*
import pl.droidsonroids.gif.GifDrawable
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MapFragment : Fragment(), LoaderManager.LoaderCallbacks<Boolean> {

    private val i = { msg: String -> Log.i(MapFragment::class.simpleName, msg) }
    private val d = { msg: String -> Log.d(MapFragment::class.simpleName, msg) }

    private val ktag = "MyTest"

    private var mListener: OnFragmentInteractionListener? = null

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


        val gif = GifDrawable(resources, R.drawable.car)
        val matrix = Matrix().apply { postScale(1.5f, 1.5f) }
        val icons = ArrayList(
                (0..gif.numberOfFrames).map {
                    gif.seekToFrameAndGet(it)
                }.map {
                    BitmapDescriptorFactory.fromBitmap(Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, true))
                })
        var index = 0
        val nextFrame = {
            if (index >= icons.size) {
                index = 0
            }
            val result = icons[index]
            index ++
            result
        }
        val marker = map.addMarker(MarkerOptions().apply {
            icon(icons.firstOrNull())
            period(4)
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
            for (i in 0..800) {
                latitude += 0.1 * (Math.random() - 0.5)
                longitude += 0.01
                add(LatLng(latitude, longitude))
            }
        })

        map.setCustomRenderer(object : CustomRenderer {
            private var lastFrameTime = 0L

            override fun OnMapReferencechanged() {}

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            }

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                lastFrameTime = System.currentTimeMillis()
            }

            override fun onDrawFrame(gl: GL10?) {
                if (System.currentTimeMillis() - lastFrameTime > 40) {
                    i("Refreshing")
                    marker.setIcon(nextFrame.invoke())
                    lastFrameTime = System.currentTimeMillis()
                }
            }
        })
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

    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

    override fun onAttach(context: Context?) {
        Log.d(ktag, "onAttach")
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        Log.d(ktag, "onDetach")
        super.onDetach()
        mListener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
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
