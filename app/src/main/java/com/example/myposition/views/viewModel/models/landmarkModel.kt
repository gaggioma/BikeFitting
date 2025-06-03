package com.example.myposition.views.viewModel.models

import androidx.compose.ui.geometry.Offset

data class landmarkModel(
    val name: String = "",
    val prev: Int? = null, //index of previous node
    val current: Int, //index of current node
    val next: Int? = null, //index of next node
    val maxAngle: Float? = null, //Max span angle threshold for analysis
    val minAngle: Float? = null, //Min span angle threshold for analysis
    val currentAngle: Float? = null, //Angle to evaluate if [min, max]
    val currentOffset: Offset? = null, //(x,y) position of current node
)
