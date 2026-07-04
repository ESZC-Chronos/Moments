package com.example.ui.components

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix

object ImageFilters {
    val filters = listOf("none", "sepia", "bw", "polaroid")

    fun getFilter(name: String): ColorFilter? {
        return when (name) {
            "sepia" -> ColorFilter.colorMatrix(ColorMatrix(
                floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f,     0f,     0f,     1f, 0f
                )
            ))
            "bw" -> ColorFilter.colorMatrix(ColorMatrix().apply {
                setToSaturation(0f)
            })
            "polaroid" -> ColorFilter.colorMatrix(ColorMatrix(
                floatArrayOf(
                    1.438f, -0.062f, -0.062f, 0f, 0f,
                    -0.122f, 1.378f, -0.122f, 0f, 0f,
                    -0.016f, -0.016f, 1.483f, 0f, 0f,
                    0f,      0f,      0f,     1f, 0f
                )
            ))
            else -> null
        }
    }
}
