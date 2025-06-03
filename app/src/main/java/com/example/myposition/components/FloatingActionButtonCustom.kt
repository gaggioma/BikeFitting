package com.example.myposition.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FloatingActionButtonCustom(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    text:String,
    containerColor:Color =  FloatingActionButtonDefaults.containerColor,
    content: @Composable () -> Unit
){

    val buttonSize = 90.dp

    FloatingActionButton(
        modifier = modifier
            .padding(bottom = 5.dp)
            .width(buttonSize),
        onClick = {onClick()},
        containerColor = containerColor

        ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(5.dp)
        ) {
            Text(
                text = text,
                fontSize = 10.sp
            )
            content()
        }
    }

}