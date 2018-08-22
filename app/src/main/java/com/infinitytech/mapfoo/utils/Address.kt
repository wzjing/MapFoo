package com.infinitytech.mapfoo.utils

data class Address(var name: String,
                   var address: String,
                   var latitude: Double,
                   var longitude: Double) {
    override fun toString(): String {
        return super.toString()
    }

    companion object {
        fun fromString(str: String): Address {
            return Address("", "", 0.0, 0.0)
        }
    }

}