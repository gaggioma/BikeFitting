package com.example.myposition.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myposition.R
import com.google.mediapipe.tasks.vision.core.RunningMode

@Composable
fun ActionButtons(

    runningMode: RunningMode,

    selectedUri: Uri? = null,

    saddleShiftHandler: (() -> Unit),
    saddleShift: Boolean = false,

    panZoomState: Boolean = false,
    panZoomHandler:  () -> Unit? = {},

    selectFileClickHandler: ()-> Unit? = {},

    backgroundShow: Boolean = true,
    backgroundShowHandler: () -> Unit? = {},

    showCharts: Boolean = false,
    showChartsHandler: () -> Unit? = {},

    showBox: Boolean = true,
    showBoxHandler: () -> Unit = {},

    autoFittingHandler: (() -> Unit) = {}
){

    //Floating button state
    val floatingOpen = remember { mutableStateOf(true) }

    Column(
        horizontalAlignment = AbsoluteAlignment.Right
    ) {

        // The Expandable Sheet layout
        AnimatedVisibility(
            visible = floatingOpen.value,
            enter = expandHorizontally(tween(1500)) + fadeIn(),
            exit = shrinkHorizontally(tween(1200)) + fadeOut(
                animationSpec = tween(1000)
            )
        ) {
            Column(
                horizontalAlignment = AbsoluteAlignment.Right
            ) {

                //show charts
                if(runningMode == RunningMode.VIDEO && selectedUri != null) {
                    FloatingActionButtonCustom(
                        //containerColor = Color.Transparent,
                        onClick = {
                            showChartsHandler()
                        },
                        text ="Charts"
                    ) {
                            Icon(
                                painter = painterResource(if (!showCharts) R.drawable.show_chart else R.drawable.close),
                                contentDescription = "show_charts"
                            )
                    }

                    //Automatic fitting
                    FloatingActionButtonCustom(
                        onClick = {
                            autoFittingHandler()
                        },
                        text =""
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.auto_awesome),
                            contentDescription = "auto"
                        )
                    }
                }

                //Remove/show background image
                FloatingActionButtonCustom(
                    onClick = { backgroundShowHandler() },
                    text = "Background"
                ) {
                        Icon(
                            painter = painterResource(if (!backgroundShow) R.drawable.close else R.drawable.done),
                            contentDescription = "show_background"
                        )

                }

                //Remove/show box near angles
                FloatingActionButtonCustom(
                    onClick = { showBoxHandler() },
                    text = "Box"
                ) {
                    Icon(
                        painter = painterResource(if (!showBox) R.drawable.close else R.drawable.done),
                        contentDescription = "show_box"
                    )

                }

                //These function only with image running mode
                if((runningMode == RunningMode.IMAGE || runningMode == RunningMode.VIDEO) && selectedUri != null) {

                    //Saddle shift
                    FloatingActionButtonCustom(
                        //containerColor = Color.Transparent,
                        onClick = {
                            saddleShiftHandler()
                        },
                        text = "Saddle shift"
                    ) {
                        Icon(
                            painter = painterResource(if(saddleShift) R.drawable.close else R.drawable.done),
                            contentDescription = "saddle_shift"
                        )
                    }
                }

                //Upload file
                if(runningMode == RunningMode.IMAGE || runningMode == RunningMode.VIDEO) {
                    FloatingActionButtonCustom(
                        onClick = { panZoomHandler() },
                        text = "Pan"
                    ) {
                        Icon(
                            painter = painterResource(if (!panZoomState) R.drawable.pan_tool else R.drawable.close),
                            contentDescription = "pan_tool"
                        )
                    }

                    FloatingActionButtonCustom(
                        onClick = { selectFileClickHandler() },
                        text ="Upload",
                        containerColor = Color(201, 38, 68)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.file_upload),
                            contentDescription = "file_upload"
                        )
                    }
                }
            }
        }

        //Main menu floating button
        FloatingActionButton(
            modifier = Modifier.size(40.dp),
            onClick = {
                floatingOpen.value = !floatingOpen.value
            }
        ) {
            Icon(
                imageVector = if (floatingOpen.value) Icons.Rounded.Close else Icons.Rounded.Menu,
                contentDescription = "menu"
            )
        }
    }
}