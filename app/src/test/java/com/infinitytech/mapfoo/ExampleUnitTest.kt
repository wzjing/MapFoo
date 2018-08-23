package com.infinitytech.mapfoo

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    var list = ArrayList<String>()
        set(value) {
            field = value
        }
        get() {
            return field
        }

    @Test
    fun collection_modify() {
        val set = setOf("Alpha", "Beta", "Charlie", "Delta")
        val arrayList = ArrayList(set)
        arrayList[3] = "Echo"
        set.forEach { print("$it ") }
        arrayList.forEach { print("$it ") }
    }
}
