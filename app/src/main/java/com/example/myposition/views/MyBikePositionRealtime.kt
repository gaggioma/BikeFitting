package com.example.myposition.views

import androidx.camera.core.CameraSelector
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myposition.components.ActionButtons
import com.example.myposition.components.AngleAnalysisBox
import com.example.myposition.components.CameraAnalyzer
import com.example.myposition.components.DrawLandmarkers
import com.example.myposition.views.viewModel.MyBikePositionViewModel
import kotlinx.coroutines.launch
import kotlin.math.floor


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBikePositionRealTime() {

    //View model. If this view is inside nav controller the navigation package for hilt
    // is mandatory: androidx.hilt:hilt-navigation-compose
    val vm: MyBikePositionViewModel = hiltViewModel()

    //States
    val state = vm.uiState.collectAsState()
    val frameList = state.value.frameList
    val runningMode = state.value.runningMode
    val cameraZoom = state.value.zoomValue


    //Show image
    val showImage = rememberSaveable { mutableStateOf(true)  }

    //Show box
    val showBox = rememberSaveable { mutableStateOf(true) }

    //Used for snackbar
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    fun showImageHandler(){
        showImage.value = !showImage.value
    }

    //State to keep track of auto focus
    val tapToFocusOffset = remember { mutableStateOf<Offset>(Offset.Unspecified) }
    //Handler came from DrawLandmarkers
    fun tapToFocusHandler(offset: Offset){
        tapToFocusOffset.value = offset
    }

    //Handler to dismiss focus circle into CameraAnalyzer
    fun tapToFocusResult(result: Boolean){
        tapToFocusOffset.value = Offset.Unspecified
        if(!result){
            scope.launch {
                snackbarHostState
                    .showSnackbar(
                        message = "Tap to focus error",
                        // Defaults to SnackbarDuration.Short
                        duration = SnackbarDuration.Short
                )
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

                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Zoom: ${floor(cameraZoom * 100).toInt()}%")
                    Slider(
                        value =  cameraZoom,
                        onValueChange = {
                            vm.setZoomValue(it)
                            ///cameraZoom.floatValue = it
                        },
                        steps = 10,
                        valueRange = 0f..1f
                    )
                }
            }
        },
        floatingActionButton = {
            ActionButtons(
                runningMode = runningMode,
                backgroundShow = showImage.value,
                backgroundShowHandler = {showImageHandler()},
                showBox = showBox.value,
                showBoxHandler = {
                    showBox.value = !showBox.value
                },
                saddleShiftHandler = {}
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {

            //CameraX analyzer
            CameraAnalyzer(
                cameraSelectorInput = CameraSelector.LENS_FACING_BACK,
                tapToFocusOffset = tapToFocusOffset.value,
                tapToFocusResult = { result -> tapToFocusResult(result)}
            )

            if (frameList.isNotEmpty()) {

                LazyColumn {

                    item(){
                        DrawLandmarkers(
                            frame = if (frameList.isNotEmpty()) frameList[0] else null,
                            useMultiTouch = false,
                            showImage = showImage.value,
                            showBox = showBox.value,
                            tapToFocusHandler = {offset: Offset -> tapToFocusHandler(offset)  },
                            tapToFocusOffset = tapToFocusOffset.value
                        )
                    }

                    //Show angle analysis box
                    if (frameList.isNotEmpty()) {

                        val currentFrame = frameList[0]
                        for (boxInfo in currentFrame.boxInfoList) {
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

                                HorizontalDivider(
                                    thickness = 2.dp,
                                    color = Color.Magenta
                                )

                            }
                        }
                    }
                }
            }
        }
    }
}