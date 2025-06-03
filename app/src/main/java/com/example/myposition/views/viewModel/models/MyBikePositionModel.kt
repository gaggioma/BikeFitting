package com.example.myposition.views.viewModel.models

import android.graphics.Bitmap
import com.example.myposition.modelHelpers.PoseLandmarkerHelper
import com.google.mediapipe.tasks.vision.core.RunningMode

data class MyBikePositionModel(
    var image: Bitmap? = null,
    //var imageRotation: Float = 0f,
    var error:String = "",
    var loading:Boolean = false,
    var imageScale: Float = 3f,
    var zoomValue: Float = 0f,
    var runningMode: RunningMode = RunningMode.LIVE_STREAM,
    var frameList: List<frameModel> = mutableListOf()
)
