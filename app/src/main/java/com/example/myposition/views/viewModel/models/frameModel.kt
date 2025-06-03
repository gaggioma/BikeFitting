package com.example.myposition.views.viewModel.models

import android.graphics.Bitmap

data class frameModel(
    var frameId: Int,
    var image: Bitmap? = null,
    var boxInfoList: List<landmarkModel>,
    var circleList: List<circleModel>,
    var lineList: List<lineModel>,
    var archList: List<archModel>
)
