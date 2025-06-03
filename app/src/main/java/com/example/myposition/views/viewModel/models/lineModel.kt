package com.example.myposition.views.viewModel.models

import androidx.compose.ui.geometry.Offset

data class lineModel(
    val start: Offset? = null,
    val end: Offset? = null,
    val dashed: Boolean = false,
    //val distanceMeters: Int? = null,
    val nameStart: String? = null,
    val nameEnd: String? = null
)
