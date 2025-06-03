package com.example.myposition.views

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.scale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.myposition.R
import com.example.myposition.components.ActionButtons
import com.example.myposition.components.AngleAnalysisBox
import com.example.myposition.components.AutomaticFitting
import com.example.myposition.components.DrawLandmarkers
import com.example.myposition.components.FrameRateList
import com.example.myposition.components.SaddleControl
import com.example.myposition.components.TimeAnalysis
import com.example.myposition.views.viewModel.MyBikePositionVideoViewModel
import com.example.myposition.views.viewModel.getBitmapFromUri
import com.example.myposition.views.viewModel.models.frameModel
import com.google.mediapipe.tasks.vision.core.RunningMode
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.round


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBikePositionVideo(
    navController: NavHostController
){

    val TAG = "MyBikePositionVideo"

    val context = LocalContext.current

    //Image scale
    val imgScale = rememberSaveable { mutableFloatStateOf(1f) }

    //View model
    val vm:MyBikePositionVideoViewModel = hiltViewModel()

    //State
    val state = vm.state.collectAsState()
    //val uriState = state.value.videoUri
    val loading = state.value.loading
    val loadingBestConf = state.value.bestConfigLoading
    val runningMode = state.value.runningMode
    val frameList = state.value.frameList
    val error = state.value.error
    val frameState = state.value.video_fps
    val saddleXCm = state.value.saddleXCm
    val saddleYCm = state.value.saddleYCm

    //Only remember will be cleared during screen rotation. Use rememberSaveable
    val uriState = rememberSaveable { mutableStateOf<Uri?>(null) }

    //Current frame to plot
    val frame = remember {
        mutableStateOf<frameModel?>(null)
    }

    //Replay state
    val replayState = rememberSaveable { mutableFloatStateOf(0f) }

    //Play video status
    val playState = rememberSaveable { mutableStateOf(false) }
    val pauseState = rememberSaveable { mutableStateOf(false) }
    val frameIdState = rememberSaveable { mutableIntStateOf(0) }

    //Pan zoom
    val panZoomState = rememberSaveable { mutableStateOf(false) }

    //Saddle shift
    val saddleShiftEnable = rememberSaveable { mutableStateOf(false) }
    val saddleXShift = rememberSaveable { mutableFloatStateOf(0f) }
    val saddleYShift = rememberSaveable { mutableFloatStateOf(0f) }

    //Show image
    val showImage = rememberSaveable { mutableStateOf(true)  }

    //Show box
    val showBox = rememberSaveable { mutableStateOf(true) }

    //Show charts
    val showCharts = rememberSaveable { mutableStateOf(false) }

    //Show automatic fitting card
    val showAutomaticFitting = rememberSaveable { mutableStateOf(false) }

    //Frame rate[fps]
    val frameRateList = remember { mutableStateOf(
        listOf<Int>(
            //12,
            //21,
            30
        )
    ) }

    fun zoomChangeHandler(zoomValue: Float, uri:Uri?){

        if(uri == null){
            return Unit
        }

        //Get bitmap and resize them
        val bitmap: Bitmap = getBitmapFromUri(uri, context)
        val imageWidth = bitmap.width
        val imageHeight = bitmap.height

        //Scale image by scale factor
        val resizedBitmap = bitmap.scale(
            (imageWidth * zoomValue).toInt(),
            (imageHeight * zoomValue).toInt(),
            true
        )

        //reset saddle shift
        saddleXShift.floatValue = 0f
        saddleYShift.floatValue = 0f

        //Execute analysis
        vm.executeImageAnalysis(
            scaleFactor = 1f,
            saddleXShift = 0f,
            saddleYShift = 0f,
            image = resizedBitmap
        )

        //Update img scale
        imgScale.floatValue = zoomValue
    }

    //Use photo picker like in https://developer.android.com/training/data-storage/shared/photopicker#compose
    // Registers a photo picker activity launcher in single-select mode.
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {

            vm.resetFrameList()

            //Set uri
            uriState.value = uri

            if(runningMode == RunningMode.IMAGE) {
                //Zoom image and execute analysis
                zoomChangeHandler(imgScale.floatValue, uri)
            }else{
                playState.value = true
                vm.executeVideoAnalysis(
                    scaleFactor = 1f,
                    videoUri = uri,
                    saddleXShift = saddleXShift.floatValue,
                    saddleYShift = saddleYShift.floatValue
                )
                pauseState.value = false
            }

            Log.d(TAG, "Selected URI: $uri")
        } else {
            Log.d(TAG, "No media selected")
        }
    }

    fun selectFileClickHandler() {
        // Launch the photo picker and let the user choose only videos.
        if (runningMode == RunningMode.VIDEO){
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
        }

        if(runningMode == RunningMode.IMAGE){
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        //Deactivate feature
        showAutomaticFitting.value = false
        saddleShiftEnable.value = false
    }

    fun panZoomHandler(){
        panZoomState.value = !panZoomState.value
    }

    fun setRunningMode(mode: RunningMode){
        //Reset uri and result frame list
        vm.resetFrameList()
        uriState.value = null

        vm.setRunningMode(mode)
    }

    fun resetSaddleShift(){
        saddleXShift.floatValue = 0.0f
        saddleYShift.floatValue = 0.0f

        //Zoom image and execute analysis
        vm.setResultAndMakeGraphics(
            //imgScaleValue = imgScale.floatValue,
            saddleXShift = saddleXShift.floatValue,
            saddleYShift = saddleYShift.floatValue
        )
        //Reset video play
        frameIdState.intValue = 0
    }

    fun changeFrameRateHandler(fps: Int){
        //Update vm state
        vm.setVideoFps(fps)

        //Reset current frame list
        vm.resetFrameList()

        //Execute analysis
        if(uriState.value !== null && runningMode == RunningMode.VIDEO ){
            playState.value = true
            vm.executeVideoAnalysis(
                scaleFactor = 1f,
                videoUri = uriState.value!!,
                saddleXShift = saddleXShift.floatValue,
                saddleYShift = saddleYShift.floatValue
            )
            //Reset video play
            frameIdState.intValue = 0
        }
    }

    fun showImageHandler(){
        showImage.value = !showImage.value
    }

    //handler show_charts
    fun showChartsHandler(){
        showCharts.value = !showCharts.value
    }

    //Saddle handler
    fun moveSaddle(position: String){
        if(position == "up"){
            saddleYShift.floatValue -= 0.005f
            //Make analysis
            vm.setResultAndMakeGraphics(
                //imgScaleValue = imgScale.floatValue,
                saddleXShift = saddleXShift.floatValue,
                saddleYShift = saddleYShift.floatValue
            )
        }else if(position == "down"){
            saddleYShift.floatValue += 0.005f
            //Make analysis
            vm.setResultAndMakeGraphics(
                //imgScaleValue = imgScale.floatValue,
                saddleXShift = saddleXShift.floatValue,
                saddleYShift = saddleYShift.floatValue
            )

        }else if(position == "left"){
            saddleXShift.floatValue -= 0.005f
            //Make analysis
            vm.setResultAndMakeGraphics(
                //imgScaleValue = imgScale.floatValue,
                saddleXShift = saddleXShift.floatValue,
                saddleYShift = saddleYShift.floatValue
            )
        }
        else if(position == "right"){
            saddleXShift.floatValue += 0.005f
            //Make analysis
            vm.setResultAndMakeGraphics(
                //imgScaleValue = imgScale.floatValue,
                saddleXShift = saddleXShift.floatValue,
                saddleYShift = saddleYShift.floatValue
            )
        }
        else if(position == "auto"){
            vm.findBestConfiguration()
        }

        //Reset video play
        frameIdState.intValue = 0
    }

    //On frame list change, select to display only first frame
    LaunchedEffect(frameList, replayState.floatValue) {
        if(frameList.isNotEmpty()){
            playState.value = true
            val iterator = frameList.iterator()
            while(iterator.hasNext())
            {
                val frameTmp = iterator.next()
                if(frameTmp.frameId < frameIdState.intValue){
                    continue
                }
                frame.value = frameTmp

                delay(300)
                if(pauseState.value){
                    frameIdState.intValue = frameTmp.frameId
                    break
                }
            }
            playState.value = false
            if(!iterator.hasNext()){
                pauseState.value = true
                frameIdState.intValue = 0
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start) {
                        //NavigationIcon(navController = navController)

                    }
                },
                actions ={
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        //Video or image
                        Button(
                            onClick = {
                                setRunningMode(RunningMode.IMAGE)
                            },
                            colors = if (runningMode == RunningMode.IMAGE)
                                ButtonDefaults.buttonColors(
                                    containerColor = Color(red = 255, green = 255, blue = 102)
                            ) else ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Photo")
                            Icon(
                                painter = painterResource(R.drawable.screenshot),
                                contentDescription = "analysis_type"
                            )
                        }

                        Button(
                            onClick = {
                                setRunningMode(RunningMode.VIDEO)
                            },
                            colors = if (runningMode == RunningMode.VIDEO)
                                ButtonDefaults.buttonColors(
                                    containerColor = Color(red = 255, green = 255, blue = 102)
                                ) else ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Video")
                            Icon(
                                painter = painterResource(R.drawable.play_circle),
                                contentDescription = "analysis_type"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if(runningMode === RunningMode.VIDEO) {
                BottomAppBar(
                    //containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                ) {

                    //Show button only if automatic fitting is not loading
                    if(!loadingBestConf){
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (!playState.value) {
                                IconButton(
                                    onClick = {
                                        replayState.floatValue = Math.random().toFloat()
                                        frameIdState.intValue = 0
                                        pauseState.value = false
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.replay),
                                        contentDescription = "replay"
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    pauseState.value = !pauseState.value
                                    if (!pauseState.value) {
                                        replayState.floatValue = Math.random().toFloat()
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(if (pauseState.value) R.drawable.play_circle else R.drawable.pause),
                                    contentDescription = "pause"
                                )
                            }

                        }
                    }
                }
            }
        },
        floatingActionButton = {

            ActionButtons(
                runningMode = runningMode,
                selectedUri = uriState.value,
                saddleShiftHandler = {
                    saddleShiftEnable.value = !saddleShiftEnable.value
                    showAutomaticFitting.value = false },
                saddleShift = saddleShiftEnable.value,
                panZoomState = panZoomState.value,
                panZoomHandler = {panZoomHandler()},
                selectFileClickHandler = {selectFileClickHandler()},
                backgroundShow = showImage.value,
                backgroundShowHandler = {showImageHandler()},
                showCharts = showCharts.value,
                showChartsHandler = {showChartsHandler()},
                showBox = showBox.value,
                showBoxHandler = {
                    showBox.value = !showBox.value
                },
                autoFittingHandler = {
                    if(uriState.value != null && !loading && !loadingBestConf) {
                        moveSaddle("auto")
                        saddleShiftEnable.value = false
                        showAutomaticFitting.value = true
                    }
                }
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->

        if (loading) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text ="Wait until analysis process will be ended.\r\nThe time of process depends on device hardware.",
                    textAlign = TextAlign.Center)
                CircularProgressIndicator(
                    modifier = Modifier.size(100.dp),
                    color = Color.Red,
                    strokeWidth = 3.dp
                )
            }
        } else if (error.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(error)
            }
        } else
        {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                //zoom slider. Enable only with static image
                if (runningMode == RunningMode.IMAGE) {
                    Slider(
                        value = imgScale.floatValue,
                        onValueChange = { zoomChangeHandler(it, uriState.value) },
                        steps = 10,
                        valueRange = 0.1f..2f
                    )
                    Text(text = "Zoom: ${round(imgScale.floatValue * 100).toInt()}%")
                }

                if (runningMode == RunningMode.VIDEO) {
                    FrameRateList(
                        frameRateList = frameRateList.value,
                        selectedFrameRate = frameState,
                        changeFrameRateHandler = { fps ->
                            changeFrameRateHandler(fps)
                        }
                    )
                }

                //Saddle control
                if(saddleShiftEnable.value && !loadingBestConf) {
                    SaddleControl(
                        upHandler = { moveSaddle("up") },
                        downHandler = { moveSaddle("down") },
                        leftHandler = { moveSaddle("left") },
                        rightHandler = { moveSaddle("right") },
                        closeHandler = { resetSaddleShift() },
                        saddleXCm = saddleXCm,
                        saddleYCm = saddleYCm
                    )
                }

                //Automatic fitting card

                    if (loadingBestConf) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(50.dp),
                            color = Color.Red,
                            strokeWidth = 3.dp
                        )
                    } else {
                        if (showAutomaticFitting.value) {
                            AutomaticFitting(
                                saddleXCm = saddleXCm,
                                saddleYCm = saddleYCm,
                                closeHandler = {showAutomaticFitting.value = false}
                            )
                        }
                }

                //Show charts
                if (showCharts.value) {
                    TimeAnalysis(
                        frames = frameList,
                        currentFrame = frame.value
                    )
                }

                if(uriState.value != null && frame.value != null && !showCharts.value && !loadingBestConf){

                    LazyColumn {

                        item() {

                            //Show analyzed image with landmarks
                            DrawLandmarkers(
                                frame = frame.value,
                                useMultiTouch = panZoomState.value,
                                showImage = showImage.value,
                                showBox = showBox.value
                            )
                        }

                        //Show angle analysis box
                        for (boxInfo in frame.value?.boxInfoList!!) {
                            item(
                                key = boxInfo.currentAngle
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp).padding(5.dp)
                                ) {

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = boxInfo.name,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    AngleAnalysisBox(
                                        angle = boxInfo.currentAngle,
                                        angleMax = boxInfo.maxAngle,
                                        angleMin = boxInfo.minAngle
                                    )

                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

