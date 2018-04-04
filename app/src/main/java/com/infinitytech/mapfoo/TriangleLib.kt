package com.infinitytech.mapfoo

class TriangleLib {
    companion object {
        init {
            System.loadLibrary("triangle-lib")
        }

        @JvmStatic external fun init(): Boolean

        @JvmStatic external fun resize(width: Int, height: Int)

        @JvmStatic external fun step()
    }
}