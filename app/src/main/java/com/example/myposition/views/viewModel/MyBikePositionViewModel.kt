package com.example.myposition.views.viewModel

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.scale
import androidx.lifecycle.ViewModel
import com.example.myposition.modelHelpers.PoseLandmarkerHelper
import com.example.myposition.modelHelpers.PoseLandmarkerHelper.ResultBundle
import com.example.myposition.views.viewModel.models.MyBikePositionModel
import com.example.myposition.views.viewModel.models.archModel
import com.example.myposition.views.viewModel.models.circleModel
import com.example.myposition.views.viewModel.models.frameModel
import com.example.myposition.views.viewModel.models.getDirection
import com.example.myposition.views.viewModel.models.getDirectionList
import com.example.myposition.views.viewModel.models.getLandmarkAngleList
import com.example.myposition.views.viewModel.models.getLandmarkName
import com.example.myposition.views.viewModel.models.landmarkModel
import com.example.myposition.views.viewModel.models.lineModel
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.math.atan2

@HiltViewModel
class MyBikePositionViewModel @Inject constructor(
    //Inject here poseHelperProvider
    private val poseHelperProvider: PoseLandmarkerHelper
) : ViewModel(), PoseLandmarkerHelper.LandmarkerListener {

    val TAG = "MyBikePositionViewModel"

    private val _state = MutableStateFlow<MyBikePositionModel>(MyBikePositionModel())
    val uiState: StateFlow<MyBikePositionModel> = _state.asStateFlow()

    init{
        //Set this class like a listener for pose helper
        poseHelperProvider.setRunningMode(RunningMode.LIVE_STREAM)
        poseHelperProvider.setListenerClass(this)
    }

    //Execute pose prediction for streaming
    fun posePredictionStreaming(image: ImageProxy, isFrontCamera: Boolean ){
        poseHelperProvider.detectLiveStream(image, isFrontCamera)
    }

    fun setImage(image: Bitmap){
        val stateCopy = _state.value.copy()
        stateCopy.image = image
        _state.value = stateCopy
    }

    private fun setFrameList(list: List<frameModel>){
        val tmpState = _state.value.copy()
        tmpState.frameList = list
        _state.value = tmpState
    }

    fun setImageScale(value: Float){
        val tmpState = _state.value.copy()
        tmpState.imageScale = value
        _state.value = tmpState
    }

    fun setZoomValue(value: Float){
        val tmpState = _state.value.copy()
        tmpState.zoomValue = value
        _state.value = tmpState
    }

    fun clearPoseLandmarker(){
        poseHelperProvider.clearPoseLandmarker()
    }

    //Interface implmentation
    override fun onError(error: String, errorCode: Int) {
        val stateCopy = _state.value.copy()
        stateCopy.error = error
        _state.value = stateCopy
    }

    //Interface implementation
    override fun onResults(resultBundle: PoseLandmarkerHelper.ResultBundle) {
        //setResult(resultBundle)
        setResultAndMakeGraphics(
            result = resultBundle,
            //interestListState = _state.value.interestListState,
            imgScaleValue = _state.value.imageScale
        )
    }

    private fun setResultAndMakeGraphics(
        result: ResultBundle?,
        //interestListState: List<Int>,
        imgScaleValue: Float = 1f,
    ){

        //Angle with max min infos
        val landmarkAngleList = getLandmarkAngleList()

        //Where to save box list
        var boxInfoListState = mutableListOf<landmarkModel>()

        //Points container
        var circleList = mutableListOf<circleModel>()

        //Line container
        var lineList = mutableListOf<lineModel>()

        //Arch container
        var archList = mutableListOf<archModel>()

        //Frame list
        val frameList = mutableListOf<frameModel>()

        //Image reference
        val image = _state.value.image
        val imageWidth = image!!.width
        val imageHeight = image.height

        //Scale image by scale factor
        val resizedBitmap:  Bitmap = image.scale(
            (imageWidth * imgScaleValue).toInt(),
            (imageHeight * imgScaleValue).toInt(),
            filter = true
        )

        result?.results?.let { poseLandmarkerResults: List<PoseLandmarkerResult> ->

            poseLandmarkerResults.forEachIndexed{frameIndex, poseLandmarkResult ->


                for (normalizedLandmarks in poseLandmarkResult.landmarks()) {

                    //Rest container list
                    boxInfoListState = mutableListOf<landmarkModel>()
                    circleList = mutableListOf<circleModel>()
                    lineList = mutableListOf<lineModel>()
                    archList = mutableListOf<archModel>()

                    //Find direction based on hip and shoulder coordinates
                    val interestListState = getDirectionList(
                        originList = normalizedLandmarks
                    )

                    normalizedLandmarks.forEachIndexed { index, landmark ->

                        //Draw circle on landmarks belong to interested list of poses
                        if (interestListState.contains(index)) {

                            //Landmark coordinates
                            val landmarkX = landmark.x()
                            val landmarkY = landmark.y()

                            //Landmark coordinates with scale
                            val xCenter = landmarkX * imageWidth * imgScaleValue
                            val yCenter = landmarkY * imageHeight * imgScaleValue

                            val currentOffset = Offset(
                                xCenter,
                                yCenter
                            )

                            circleList.add(
                                circleModel(
                                    center = currentOffset
                                )
                            )

                            //Get landmark object in which are defined prev and next landmark to draw angle
                            val landmarkAngle: landmarkModel? = landmarkAngleList.firstOrNull { it.current == index }

                            if (landmarkAngle != null) {

                                val prevLandmark =
                                    normalizedLandmarks[landmarkAngle.prev!!]
                                val nextLandmark =
                                    normalizedLandmarks[landmarkAngle.next!!]

                                //Prev and next coordinates
                                val xPrev = prevLandmark.x()
                                val xPrevCenter = xPrev * imageWidth * imgScaleValue
                                val yPrev = prevLandmark.y()
                                val yPrevCenter = yPrev * imageHeight * imgScaleValue

                                val xNext = nextLandmark.x()
                                val xNextCenter = xNext * imageWidth * imgScaleValue
                                val yNext = nextLandmark.y()
                                val yNextCenter = yNext * imageHeight * imgScaleValue

                                //landmark prev and next coordinates centered in current landmark
                                val xPrevRelative = xPrevCenter - xCenter
                                val yPrevRelative = yPrevCenter - yCenter

                                val xNextRelative = xNextCenter - xCenter
                                val yNextRelative = yNextCenter - yCenter

                                //Prev and next angle [0, 360]
                                var archDegreesPrev = Math.toDegrees(
                                    atan2(
                                        yPrevRelative,
                                        xPrevRelative
                                    ).toDouble()
                                )
                                archDegreesPrev =
                                    if (archDegreesPrev < 0) archDegreesPrev + 360 else archDegreesPrev

                                var archDegreesNext = Math.toDegrees(
                                    atan2(
                                        yNextRelative,
                                        xNextRelative
                                    ).toDouble()
                                )
                                archDegreesNext =
                                    if (archDegreesNext < 0) 360 + archDegreesNext else archDegreesNext

                                //Arch between prev and next
                                var archDegrees = archDegreesPrev - archDegreesNext
                                //If arch < 0 - start draw arch from prev
                                var startArch = archDegreesNext
                                if (archDegrees < 0) {
                                    archDegrees = archDegreesNext - archDegreesPrev
                                    startArch = archDegreesPrev
                                }

                                //Evaluate reverse
                                val reverseArchDegree = 360 - archDegrees
                                if (reverseArchDegree < archDegrees) {
                                    archDegrees = reverseArchDegree
                                    startArch =
                                        if (startArch == archDegreesNext) archDegreesPrev else archDegreesNext
                                }

                                //Save into boxInfoList
                                boxInfoListState.add(
                                    landmarkModel(
                                        current = index,
                                        currentOffset = currentOffset,
                                        currentAngle = archDegrees.toFloat(),
                                        maxAngle = landmarkAngle.maxAngle,
                                        minAngle = landmarkAngle.minAngle,
                                        name = getLandmarkName(index).toString()
                                    )
                                )

                                //Angle arch
                                val archSize = 160f
                                archList.add(
                                    archModel(
                                        startAngle = startArch.toFloat(),
                                        sweepAngle = archDegrees.toFloat(),
                                        topLeft = Offset(
                                            xCenter - (archSize / 2),
                                            yCenter - (archSize / 2)
                                        ),
                                        archSize = archSize,
                                        color = if(archDegrees.toFloat() < landmarkAngle.maxAngle!! && archDegrees.toFloat() > landmarkAngle.minAngle!!) Color.Green else Color.Red
                                    ))

                                //Line between current landmark and prev landmark
                                lineList.add(lineModel(
                                    start = Offset(
                                        x = xCenter,
                                        y = yCenter
                                    ),
                                    end = Offset(
                                        x = xPrevCenter,
                                        y = yPrevCenter
                                    )
                                ))

                                //Line between current landmark and next landmark
                                lineList.add(lineModel(
                                    start = Offset(
                                        x = xCenter,
                                        y = yCenter
                                    ),
                                    end = Offset(
                                        x = xNextCenter,
                                        y = yNextCenter
                                    )
                                ))
                            }
                        }
                    }

                }
                frameList.add(
                    frameModel(
                        boxInfoList = boxInfoListState,
                        circleList = circleList,
                        lineList = lineList,
                        archList = archList,
                        frameId = frameIndex,
                        image = resizedBitmap
                    ))
                setFrameList(frameList)
            }
        }
    }
}