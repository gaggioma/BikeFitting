package com.example.myposition.components

import android.content.Context
import android.util.DisplayMetrics
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myposition.views.viewModel.models.frameModel
import com.example.myposition.views.viewModel.models.offsetDistance


@Composable
fun DrawLandmarkers(
    initHeight: Float = 10f,
    frame: frameModel?,
    useMultiTouch:Boolean = false,
    showImage:Boolean = true,
    showBox:Boolean = true
) {

    val context = LocalContext.current

    val TAG = "NewDrawLandmarkers"

    //for draw text
    val textMeasurer = rememberTextMeasurer()

    //Used to modify image scale
    val imgHeightState = rememberSaveable { mutableFloatStateOf(initHeight) }

    //Use offset/zoom by touch: https://developer.android.com/develop/ui/compose/touch-input/pointer-input/multi-touch
    val offset = remember { mutableStateOf(Offset.Zero) }
    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
            offset.value += offsetChange
    }

    //When landmark is clicked show all info end threshold
    fun showLandmarkInfo(index: Int){

    }

    LaunchedEffect(useMultiTouch) {
        //Reset offset when enable
        if(useMultiTouch){
            offset.value = Offset.Zero
        }
    }

    fun Float.pxToDp(context: Context): Float {
        return (this / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT))
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(imgHeightState.floatValue.pxToDp(context).dp)
            .transformable(
                state = state,
                enabled = useMultiTouch
            )

            //Use this to intercept click on markers
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { tapOffset ->
                        // When the user taps on the Canvas, you can
                        // check if the tap offset is in one of the
                        // tracked Rects.
                        var index = 0
                        if(frame != null) {
                            for (rect in frame.boxInfoList) {
                                if (offsetDistance(rect.currentOffset!!, tapOffset) < 100) {
                                    // Handle the click here and do
                                    // some action based on the index
                                    showLandmarkInfo(index)
                                    break // don't need to check other points,
                                    // so break
                                }
                                index++
                            }
                        }
                    }
                )
            },
        onDraw = {

            if (frame?.image != null) {
                //update height state
                imgHeightState.floatValue = frame.image!!.height.toFloat()

                //Draw image
                if(showImage) {
                    drawImage(
                        image = frame.image!!.asImageBitmap(),
                        topLeft = offset.value
                    )
                }
            }

            if (frame != null) {
                //Draw circle
                for (circle in frame.circleList) {
                    drawCircle(
                        radius = 10f,
                        color = Color.Cyan,
                        center = circle.center!! + offset.value
                    )
                }

                //Draw arch
                for (arch in frame.archList) {
                    drawArc(
                        color = arch.color,
                        alpha = 0.6f,
                        useCenter = true, //Enable circle sector. Otherwise only arch is drawn
                        startAngle = arch.startAngle!!,
                        sweepAngle = arch.sweepAngle!!,
                        topLeft = arch.topLeft!! + offset.value,
                        size = Size(
                            width = arch.archSize,
                            height = arch.archSize
                        )
                    )
                }

                //Draw line
                for (line in frame.lineList) {
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 10f), 0f)
                    drawLine(
                        color = Color.Green,
                        start = line.start!! + offset.value,
                        end = line.end!! + offset.value,
                        strokeWidth = 4f,
                        pathEffect = if(line.dashed) pathEffect else null
                    )
                }

                //Box close to points
                if(showBox) {
                    for (box in frame.boxInfoList) {

                        val archDegrees = box.currentAngle!!
                        val xCenter = box.currentOffset!!.x + offset.value.x
                        val yCenter = box.currentOffset!!.y + offset.value.y

                        //Draw text after drawRect otherwise text is shadow by rectangle
                        drawRoundRect(
                            color = if (archDegrees < box.maxAngle!! && archDegrees > box.minAngle!!) Color.Green else Color.Red,
                            topLeft = Offset(
                                x = xCenter + 20f,
                                y = yCenter - 20f
                            ),
                            size = Size(width = 160f, height = 80f),
                            cornerRadius = CornerRadius(x = 10f, y = 10f)
                        )

                        //Angle text. Draw text after drawRect otherwise text is shadow by rectangle
                        val displayText =
                            if (archDegrees < box.maxAngle!! && archDegrees > box.minAngle!!)
                                "${Math.round(archDegrees * 10) / 10}° \uD83D\uDE0A"
                            else
                                "${Math.round(archDegrees * 10) / 10}° \uD83D\uDE14"
                        drawText(
                            textMeasurer,
                            text = displayText,
                            topLeft = Offset(
                                x = xCenter + 40f,
                                y = yCenter
                            ),
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                            ),
                            size = Size(width = 150f, height = 100f),
                        )
                    }
                }
            }
        }
    )
}