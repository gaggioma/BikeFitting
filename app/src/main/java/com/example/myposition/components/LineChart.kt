package com.example.myposition.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.round

@Composable
fun LineChart(
    ySerie: List<Float?>,
    xLabels: List<Float>,
    currentX: Float?,
    currentY: Float?,
    maxY: Float?,
    minY: Float?
){

    //for draw text
    val textMeasurer = rememberTextMeasurer()

    val canvasHeight = 200.dp

    val yMaxValue = 360

    //Separation between y values
    val yScaleStep = 0.4.dp

    //scale margin from left boundary. Used to get space for y scale label
    val scaleMarginLeft = 25.dp

    //Used to draw y margin
    val yScaleMarginTop = 0.dp

    //Length in px of y scale
    val yScaleLength = yScaleStep * yMaxValue

    //Dimensions of chart Text
    val textHeight = 50f
    val textWidth = 70f

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .height(canvasHeight),
        onDraw = {

            val canvasSize = size
            val xScaleStep = ((canvasSize.width-scaleMarginLeft.toPx()) / xLabels.size).toDp()
            val xScaleLength = (xScaleStep * xLabels.size)

            fun getYOffset(valueY: Float, idX: Int, yMaxValue: Float): Offset {
                val pxPerVal = yScaleLength.toPx() / yMaxValue
                val pxFromTop = yScaleLength.toPx() - (pxPerVal * valueY)
                return Offset(
                    x = (scaleMarginLeft.toPx() + (xScaleStep.toPx() * idX)),
                    y = yScaleMarginTop.toPx() + pxFromTop
                )
            }

            //Draw horizontal x scale
            drawLine(
                color = Color.White,
                start = Offset(
                    x = scaleMarginLeft.toPx(),
                    y = (yScaleMarginTop + yScaleLength).toPx()
                ),
                end = Offset(
                    x = (scaleMarginLeft + xScaleLength).toPx(),
                    y = (yScaleMarginTop + yScaleLength).toPx()
                ),
                strokeWidth = 4f
            )

            //Draw vertical y scale
            drawLine(
                color = Color.White,
                start = Offset(x = scaleMarginLeft.toPx(), y = yScaleMarginTop.toPx()),
                end = Offset(
                    x = scaleMarginLeft.toPx(),
                    y = (yScaleMarginTop + yScaleLength).toPx()
                ),
                strokeWidth = 4f
            )

            //Add y labels
            for (label in 0..yMaxValue step 30) {
                drawText(
                    textMeasurer,
                    text = "$label°",
                    topLeft = getYOffset(
                        valueY = label.toFloat(),
                        idX = 0,
                        yMaxValue = yMaxValue.toFloat()
                    ) + Offset(x = scaleMarginLeft.toPx() * -1, y = -textHeight/2),
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = Color.White,
                        //fontWeight = FontWeight.,
                    ),
                    size = Size(width = textWidth, height = textHeight),
                )
            }

            //Draw serie
            val path = Path()
            ySerie.forEachIndexed { index, fl ->
                val offset = getYOffset(valueY = fl ?: 0f, idX = index, yMaxValue = yMaxValue.toFloat())
                val x = offset.x
                val y = offset.y

                if (index == 0 || fl == null) {
                    path.moveTo(x = x, y = y)
                } else {
                    path.lineTo(x = x, y = y)
                }

            }
            drawPath(path, Color.Green, alpha = 0.8f, style = Stroke(width = 5f))

            //Draw current point
            if(currentX != null && currentY != null) {

                drawText(
                    textMeasurer,
                    text = "${round(currentY).toInt()}°",
                    topLeft = getYOffset(
                        valueY = currentY,
                        idX = currentX.toInt(),
                        yMaxValue = yMaxValue.toFloat()
                    ) + Offset(x = -textWidth/2, y = -50f),
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = Color.White,
                        //fontWeight = FontWeight.,
                    ),
                    size = Size(width = textWidth, height = textHeight),
                )

                //Real time circle
                drawCircle(
                    radius = 10f,
                    color = if(currentY > maxY!! || currentY < minY!!) Color.Red else Color.Green,
                    center = getYOffset(
                        valueY = currentY,
                        idX = currentX.toInt(),
                        yMaxValue = yMaxValue.toFloat()
                    ),
                    style = Stroke(4f)
                )

                if(minY != null) {
                    //Min row
                    val pathMin = Path()
                    val offsetMin =
                        getYOffset(valueY = minY, idX = 0, yMaxValue = yMaxValue.toFloat())
                    pathMin.moveTo(x = offsetMin.x, y = offsetMin.y)
                    pathMin.lineTo(x = (scaleMarginLeft + xScaleLength).toPx(), y = offsetMin.y)
                    drawPath(pathMin, Color.Yellow, alpha = 0.8f, style = Stroke(width = 5f, pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(10f, 10f))))

                    //Max row
                    val pathMax = Path()
                    val offsetMax =
                        getYOffset(valueY = maxY, idX = 0, yMaxValue = yMaxValue.toFloat())
                    pathMax.moveTo(x = offsetMax.x, y = offsetMax.y)
                    pathMax.lineTo(x = (scaleMarginLeft + xScaleLength).toPx(), y = offsetMax.y)
                    drawPath(pathMax, Color.Yellow, alpha = 0.8f, style = Stroke(width = 5f, pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(10f, 10f))))
                }
            }
        })
}