package com.example.myposition.views.viewModel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myposition.components.models.analysisModel
import com.example.myposition.modelHelpers.PoseLandmarkerHelper
import com.example.myposition.modelHelpers.PoseLandmarkerHelper.ResultBundle
import com.example.myposition.views.viewModel.models.MyBikePositionVideoModel
import com.example.myposition.views.viewModel.models.archModel
import com.example.myposition.views.viewModel.models.circleModel
import com.example.myposition.views.viewModel.models.frameModel
import com.example.myposition.views.viewModel.models.getAction
import com.example.myposition.views.viewModel.models.getDirectionList
import com.example.myposition.views.viewModel.models.getDistanceForPixel
import com.example.myposition.views.viewModel.models.getLandmarkAngleList
import com.example.myposition.views.viewModel.models.getLandmarkName
import com.example.myposition.views.viewModel.models.isMaximum
import com.example.myposition.views.viewModel.models.landmarkModel
import com.example.myposition.views.viewModel.models.lineModel
import com.example.myposition.views.viewModel.models.makeAngleAnalysis
import com.example.myposition.views.viewModel.models.moveSaddleX
import com.example.myposition.views.viewModel.models.moveSaddleY
import com.example.myposition.views.viewModel.models.saddleModel
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.atan2

@HiltViewModel
class MyBikePositionVideoViewModel @Inject constructor(
    //Inject here poseHelperProvider
    private val poseHelperProvider: PoseLandmarkerHelper,
    @ApplicationContext private val context: Context
): ViewModel() {

    //Define state as model element
    private val _state = MutableStateFlow(MyBikePositionVideoModel())
    val state: StateFlow<MyBikePositionVideoModel> = _state.asStateFlow()

    init{
        poseHelperProvider.setRunningMode(_state.value.runningMode)
    }


    fun setRunningMode(mode: RunningMode){
        //Update helper
        poseHelperProvider.setRunningMode(mode)

        //update state
        val tmpState = _state.value.copy()
        tmpState.runningMode = mode
        _state.value = tmpState
    }

    private fun setLoading(loading: Boolean){
        val tmpState = _state.value.copy()
        tmpState.loading = loading
        _state.value = tmpState
    }

    private fun setBestConfigLoading(loading: Boolean){
        val tmpState = _state.value.copy()
        tmpState.bestConfigLoading = loading
        _state.value = tmpState
    }

    private fun setFrameList(list: List<frameModel>){
        val tmpState = _state.value.copy()
        tmpState.frameList = list
        _state.value = tmpState
    }

    private fun getFrameList(): List<frameModel>{
        return(_state.value.frameList)
    }

    fun resetFrameList(){
        val tmpState = _state.value.copy()
        tmpState.frameList = emptyList()
        _state.value = tmpState
    }

    fun setVideoFps(fps: Int){
        val tmpState = _state.value.copy()
        tmpState.video_fps = fps
        _state.value = tmpState
    }

    fun setResult(result: PoseLandmarkerHelper.ResultBundle?){
        val tmpState = _state.value.copy()
        tmpState.result = result
        _state.value = tmpState
    }

    fun setSaddleXCm(value: Float){
        val tmpState = _state.value.copy()
        tmpState.saddleXCm = value
        _state.value = tmpState
    }

    fun setSaddleYCm(value: Float){
        val tmpState = _state.value.copy()
        tmpState.saddleYCm = value
        _state.value = tmpState
    }

    fun setResultAndMakeGraphics(
        imgScaleValue: Float = 1f,
        saddleXShift: Float = 0f,
        saddleYShift: Float = 0f
    ){

        val result = _state.value.result

        //Angle with max min infos
        val landmarkAngleList = getLandmarkAngleList()

        //Where to save box list
        var boxInfoListState: MutableList<landmarkModel>

        //Points container
        var circleList: MutableList<circleModel>

        //Line container
        var lineList: MutableList<lineModel>

        //Arch container
        var archList: MutableList<archModel>

        //Frame list
        val frameList = mutableListOf<frameModel>()

        //Image reference
        val imageWidth = result!!.inputImageWidth // image!!.width
        val imageHeight = result.inputImageHeight // image.height

        var imageFromAnalysis: Bitmap? = null

        result?.results?.let { poseLandmarkerResults: List<PoseLandmarkerResult> ->

                poseLandmarkerResults.forEachIndexed{frameIndex, poseLandmarkResult ->

                if(result.imageList.isNotEmpty()){
                    imageFromAnalysis = result.imageList[frameIndex]
                }

                if(poseLandmarkResult.landmarks().isEmpty() && _state.value.runningMode == RunningMode.IMAGE ){
                    setError("No landmarks detected for this photo. Try with more detailed one.")
                    return
                }

                //Rest container list
                boxInfoListState = mutableListOf()
                circleList = mutableListOf()
                lineList = mutableListOf()
                archList = mutableListOf()

                    //Analyze landmark of this frame
                for (normalizedLandmarksOrigin in poseLandmarkResult.landmarks()) {

                    var normalizedLandmarks = normalizedLandmarksOrigin

                    //Find direction based on hip and shoulder coordinates
                    val interestListState = getDirectionList(
                        originList = normalizedLandmarks
                    )

                    //Distance [m/pixel]
                    val meterForPixel = getDistanceForPixel(
                        originList = normalizedLandmarks,
                        worldList = poseLandmarkResult.worldLandmarks()[0],
                        imgScaleValue = imgScaleValue,
                        imageWidth = imageWidth,
                        imageHeight = imageHeight
                    )
                    //Save value for display
                    if(meterForPixel != 0f) {
                        setSaddleXCm(meterForPixel * saddleXShift * imageWidth * imgScaleValue * 100)
                        setSaddleYCm(meterForPixel * saddleYShift * imageHeight * imgScaleValue * 100)
                    }

                    //Saddle shift
                    normalizedLandmarks = moveSaddleX(normalizedLandmarks, saddleXShift)
                    normalizedLandmarks = moveSaddleY(normalizedLandmarks, saddleYShift)

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
                                    ),
                                    dashed = (saddleXShift != 0f || saddleYShift != 0f),
                                    //distanceMeters = prevDistance,
                                    nameStart = getLandmarkName(index),
                                    nameEnd = getLandmarkName(landmarkAngle.prev)
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
                                    ),
                                    dashed = (saddleXShift != 0f || saddleYShift != 0f),
                                    //distanceMeters = nextDistance,
                                    nameStart = getLandmarkName(index),
                                    nameEnd = getLandmarkName(landmarkAngle.next)
                                ))
                            }
                        }
                    }


                }
                    //update state
                    frameList.add(
                        frameModel(
                            boxInfoList = boxInfoListState,
                            circleList = circleList,
                            lineList = lineList,
                            archList = archList,
                            image = imageFromAnalysis,
                            frameId = frameIndex
                        ))
            }
        }
        setFrameList(frameList)
    }


    fun setError(error: String){
        val tmpState = _state.value.copy()
        tmpState.error = error
        _state.value = tmpState
    }

     fun executeVideoAnalysis(
         scaleFactor: Float,
         videoUri: Uri,
         saddleXShift: Float,
         saddleYShift: Float,
     ){

         //Setup analyzer
         poseHelperProvider.setupPoseLandmarker()

         //val currentUri = _state.value.videoUri
         val video_interval_ms:Long = (1f / _state.value.video_fps.toFloat() * 1000f).toLong()
         setLoading(true)
         setError("")
         setResult(null)

         //GlobalScope.launch {
         viewModelScope.launch(Dispatchers.Default){

             try{
                 val result = poseHelperProvider.detectVideoFile(
                     videoUri = videoUri,
                     inferenceIntervalMs = video_interval_ms,
                     maxVideoLengthMs = _state.value.maxVideoLengthMs
                 )
                 setResult(result)
                 setLoading(false)

                 result?.let {
                     setResultAndMakeGraphics(
                         //result = it,
                         imgScaleValue = scaleFactor,
                         saddleXShift = saddleXShift,
                         saddleYShift = saddleYShift
                     )
                 }

             }catch (e: Exception){
                 setError(e.message.toString())
                 setLoading(false)
             }
         }
    }

    fun executeImageAnalysis(
        scaleFactor: Float,
        saddleXShift: Float,
        saddleYShift: Float,
        image: Bitmap
    ) {

        setError("")
        setResult(null)

        //Setup analyzer
        poseHelperProvider.setupPoseLandmarker()

        val result: ResultBundle? = poseHelperProvider.detectImage(image)
        result?.let {
            setResult(it)
            setResultAndMakeGraphics(
                //result = it,
                imgScaleValue = scaleFactor,
                saddleXShift = saddleXShift,
                saddleYShift = saddleYShift
            )
        }
    }

    fun findBestConfiguration(){
        setBestConfigLoading(true)
        viewModelScope.launch(Dispatchers.Default){
            try {
                findBestConfigurationSaddle()
                setBestConfigLoading(false)
            }catch (e: Exception){
                setError(e.message.toString())
                setBestConfigLoading(false)
            }
        }
    }

    private fun findBestConfigurationSaddle(){

        //Total shift
        var saddleYShift: Float = 0f
        var saddleXShift: Float = 0f

        //Current analysis values
        var currentAnalysis: List<analysisModel> = emptyList()

        var bestConfigurationFound = false
        //This counter give the last best configuration
        var sequenceCounter = 0

        //The best configuration are saved in list
        val finalConfiguration = mutableListOf<Int>()
        var failedInRowCounter = 0
        while(!bestConfigurationFound){

            //Choose to move up, down, left right
            val action = getAction(sequenceCounter)
            saddleXShift += action.saddleXShift
            saddleYShift += action.saddleYShift

            //Evaluate score for every landmark
            setResultAndMakeGraphics(
                //imgScaleValue = imgScale.floatValue,
                saddleXShift = saddleXShift,
                saddleYShift = saddleYShift
            )

            //if at least one score is best of previous and none worst, then proceed with other configuration, otherwise stop
            val frameList: List<frameModel> = getFrameList()
            val analysisResult:  List<analysisModel> = makeAngleAnalysis(frameList)
            if(currentAnalysis.isEmpty()){
                currentAnalysis = analysisResult
                sequenceCounter++
                continue
            }
            val isBest = isMaximum(current = currentAnalysis, next = analysisResult)
            //If this is best configuration, save the configuration and try if exists a best one
            if(isBest){
                finalConfiguration.add(sequenceCounter)
                failedInRowCounter = 0
                currentAnalysis = analysisResult
            }else{ //Back to previous configuration
                failedInRowCounter++
                saddleXShift += -1*action.saddleXShift
                saddleYShift += -1*action.saddleYShift
            }
            sequenceCounter++


            //If you try 4 combinations in row without increment the score, then the maximum is reached
            if(failedInRowCounter == 4){
                bestConfigurationFound = true
            }
        }

        //Based on final configuration list re-compute result
        saddleYShift = 0f
        saddleXShift = 0f
        finalConfiguration.forEach { actionSeq ->
            val action = getAction(actionSeq)
            saddleXShift += action.saddleXShift
            saddleYShift += action.saddleYShift
        }

        setResultAndMakeGraphics(saddleXShift = saddleXShift, saddleYShift = saddleYShift)
    }
}