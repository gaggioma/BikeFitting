package com.example.myposition.views.viewModel.models

import com.example.myposition.modelHelpers.PoseLandmarkerHelper
import com.google.mediapipe.tasks.vision.core.RunningMode

data class MyBikePositionVideoModel(
    var loading: Boolean = false,
    var bestConfigLoading: Boolean = false,
    var error: String = "",
    var video_fps: Int = 30,
    val maxVideoLengthMs: Long = 10000, //10s
    //var video_interval_ms: Long = 333L,
    var runningMode: RunningMode = RunningMode.VIDEO,
    var frameList: List<frameModel> = mutableListOf(),
    var result: PoseLandmarkerHelper. ResultBundle? = null,
    var saddleXCm: Float = 0f,
    var saddleYCm: Float = 0f,
    var saddleShiftStepPx : Float = 0.002f
    //var direction: String? = null
)
