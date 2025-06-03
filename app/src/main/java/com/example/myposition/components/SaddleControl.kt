package com.example.myposition.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myposition.R

@Composable
fun SaddleControl(
    leftHandler: (() -> Unit),
    rightHandler: (() -> Unit),
    upHandler: (() -> Unit),
    downHandler: (() -> Unit),
    closeHandler: (() -> Unit),
    saddleXCm: Float,
    saddleYCm: Float
){

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ){

        Icon(
            modifier = Modifier.size(40.dp),
            painter = painterResource(R.drawable.saddle),
            contentDescription = "saddle_shift")

        SmallFloatingActionButton(
            onClick = {
                upHandler()
            },
        ){
            Icon(painterResource(R.drawable.arrow_up), "arrow_up")
        }

        SmallFloatingActionButton(
            onClick = {
                downHandler()
            },
            ){
            Icon(painterResource(R.drawable.arrow_down), "arrow_down")
        }

        SmallFloatingActionButton(
            onClick = {
                leftHandler()
            },

            ){
            Icon(painterResource(R.drawable.arrow_left), "arrow_left")
        }

        SmallFloatingActionButton(
            onClick = {
                rightHandler()
            },

            ){
            Icon(painterResource(R.drawable.arrow_right), "arrow_right")
        }

        SmallFloatingActionButton(
            onClick = {
                closeHandler()
            },

            ){
            Icon(painterResource(R.drawable.undo), "undo")
        }
    }

    Text(text=
        "x-shift: ${String.format("%.1f", (saddleXCm))} cm," + " y-shift: ${String.format("%.1f", (saddleYCm))} cm")

}