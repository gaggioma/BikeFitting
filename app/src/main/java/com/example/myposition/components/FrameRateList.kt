package com.example.myposition.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myposition.R

@Composable
fun FrameRateList(
    frameRateList: List<Int>,
    selectedFrameRate: Int,
    changeFrameRateHandler: (Int) -> Unit
){

    Row(
        horizontalArrangement = Arrangement.Start
    ) {
        frameRateList.forEach{
            value ->
            FilledTonalButton(
                modifier = Modifier.padding(end = 5.dp),
                onClick = { changeFrameRateHandler(value) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if(selectedFrameRate == value) Color.Green else ButtonDefaults.buttonColors().containerColor
                )
            ) {
                Text(text="$value fps")
                Icon(painter = painterResource(R.drawable.replay),
                    contentDescription = "fps_replay"
                )
            }
        }

    }

}