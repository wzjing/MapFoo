package com.infinitytech.mapfoo.ui.map

import android.util.Log
import com.amap.api.maps.CustomRenderer
import java.lang.Exception
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.opengles.GL10

/**
 * @author Jerry
 * @date 2019/2/18 22:04
 */
class JNIRenderer : CustomRenderer {
    private val TAG = "JNIRenderer"
    override fun OnMapReferencechanged() {
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        try {
            val egl10 = EGLContext.getEGL() as EGL10
            val display = egl10.eglGetCurrentDisplay()
            val r = findConfigAttrib(egl10, display, config!!, EGL10.EGL_RED_SIZE, 0)
            val g = findConfigAttrib(egl10, display, config, EGL10.EGL_GREEN_SIZE, 0)
            val b = findConfigAttrib(egl10, display, config, EGL10.EGL_BLUE_SIZE, 0)
            val a = findConfigAttrib(egl10, display, config, EGL10.EGL_ALPHA_SIZE, 0)
            val depth = findConfigAttrib(egl10, display, config, EGL10.EGL_DEPTH_SIZE, 0)
            val stencil = findConfigAttrib(egl10, display, config, EGL10.EGL_STENCIL_SIZE, 0)
            Log.i(TAG, "onSurfaceCreated: $r $g $b $a $depth $stencil")

        } catch (e: Exception) {
            Log.e(TAG, "unable to get alpha size")
            e.printStackTrace()
        }
        onSurfaceCreatedNative()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        onSurfaceChangedNative(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        onDrawFrameNative()
    }

    private fun findConfigAttrib(egl: EGL10, display: EGLDisplay,
                                 config: EGLConfig, attribute: Int, defaultValue: Int): Int {
        val mValue: IntArray = intArrayOf(0)
        return if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
            mValue[0]
        } else defaultValue
    }

    external fun onSurfaceCreatedNative()

    external fun onSurfaceChangedNative(width: Int, height: Int)

    external fun onDrawFrameNative()

    companion object {
        init {
            System.loadLibrary("jni-renderer")
        }
    }
}