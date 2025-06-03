package com.example.myposition.views.viewModel.models

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class archModel(
    val startAngle: Float? = null,
    val sweepAngle: Float? = null,
    val topLeft: Offset? = null,
    val archSize: Float,
    val color: Color = Color.Green
)
