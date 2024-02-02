package me.heid.heidtools.speedApp

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class SpeedDbFormat(
    var time: Long = 0,
    var lat: Double = 0.0,
    var lon: Double = 0.0,
    var height: Double = 0.0,
    var speed: Float = 0.0f,
    var head: Float = 0.0f,
    var accuracy: Float = 0.0f

)