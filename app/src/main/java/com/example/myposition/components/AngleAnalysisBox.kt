package com.example.myposition.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun AngleAnalysisBox (
    angle: Float? = 120f,
    angleMin: Float? = 50f,
    angleMax: Float? = 100f,
) {

    //for draw text
    val textMeasurer = rememberTextMeasurer()

    //Angle state
    val angleState = rememberSaveable { mutableFloatStateOf(angle!!) }
    val angleMinState = rememberSaveable { mutableFloatStateOf(angleMin!!) }
    val angleMaxState = rememberSaveable { mutableFloatStateOf(angleMax!!) }

    LaunchedEffect(angle) {
        angleState.floatValue = angle!!
    }

    LaunchedEffect(angleMin) {
        angleMinState.floatValue = angleMin!!
    }

    LaunchedEffect(angleMax) {
        angleMaxState.floatValue = angleMax!!
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        //Line width. All space
        val lineWidth = size.width

        val TrWidth = 50f
        val TrHeight = 50f
        val xOffset = angleState.floatValue * lineWidth / 360

        val xMinOffset = angleMinState.floatValue * lineWidth / 360
        val xMaxOffset = angleMaxState.floatValue * lineWidth / 360

        val triangle_color = if(angle!! > angleMax!! || angle < angleMin!!) Color.Red else Color.Green

        //Triangle indicator
        val path = Path()
        path.moveTo(xOffset - TrWidth/2, 0f)
        path.lineTo(xOffset, TrHeight)
        path.lineTo(xOffset + TrWidth / 2, 0f)
        path.close()
        drawPath(
            path,
            triangle_color,
            style = Fill
        )

        //Gradient indicator is made with 3 segments: lower-center-above
        drawLine(
            brush = Brush.
            linearGradient(
                start = Offset(0f , TrHeight),
                end = Offset(xMinOffset, TrHeight),
                colors = listOf(Color.Red, Color.Yellow)),
            start = Offset(0f , TrHeight),
            end = Offset(xMinOffset, TrHeight),
            strokeWidth = 10f.dp.value
        )

        drawLine(
            brush = Brush.
            linearGradient(
                start = Offset(xMinOffset, TrHeight),
                end = Offset(xMaxOffset, TrHeight),
                colors = listOf(Color.Green, Color.Green)),
            start = Offset(xMinOffset, TrHeight),
            end = Offset(xMaxOffset, TrHeight),
            strokeWidth = 10f.dp.value
        )

        drawLine(
            brush = Brush.
            linearGradient(
                start = Offset(xMaxOffset, TrHeight),
                end = Offset(lineWidth, TrHeight),
                colors = listOf(Color.Yellow, Color.Red)),
            start = Offset(xMaxOffset, TrHeight),
            end = Offset(lineWidth, TrHeight),
            strokeWidth = 10f.dp.value
        )

        //O degree
        drawText(
            textMeasurer,
            text = "0°",
            topLeft = Offset(0f , TrHeight),
            style = TextStyle(
                fontSize = 13.sp,
                color = Color.White,
                fontWeight = FontWeight.Black,
            ),
            size = Size(width = 150f, height = 100f),
        )

        //36O degree
        drawText(
            textMeasurer,
            text = "360°",
            topLeft = Offset(lineWidth - 13.sp.value*6, TrHeight),
            style = TextStyle(
                fontSize = 13.sp,
                color = Color.White,
                fontWeight = FontWeight.Black,
            ),
            size = Size(width = 13.sp.value*6, height = 13.sp.value*3),
        )

        //Min angle
        drawText(
            textMeasurer,
            text = "${angleMin?.toInt()}°",
            topLeft = Offset(xMinOffset-50f, TrHeight),
            style = TextStyle(
                fontSize = 13.sp,
                color = Color.White,
                fontWeight = FontWeight.Black,
            ),
            size = Size(width = 150f, height = 100f),
        )

        //Max angle
        drawText(
            textMeasurer,
            text = "${angleMax?.toInt()}°",
            topLeft = Offset(xMaxOffset, TrHeight),
            style = TextStyle(
                fontSize = 13.sp,
                color = Color.White,
                fontWeight = FontWeight.Black,
            ),
            size = Size(width = 150f, height = 100f),
        )

        //Value on top triangle
        drawText(
            textMeasurer,
            text = "${angleState.floatValue.toInt()}°",
            topLeft = Offset(xOffset, 0f),
            style = TextStyle(
                fontSize = 13.sp,
                color = Color.White,
                fontWeight = FontWeight.Black,
            ),
            size = Size(width = 13.sp.value*5, height = 13.sp.value*3),
        )
    }
}