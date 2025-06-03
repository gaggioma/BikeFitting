package com.example.myposition.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myposition.R
import kotlin.math.abs

@Composable
fun AutomaticFitting(
    saddleXCm: Float,
    saddleYCm: Float,
    closeHandler: () -> Unit
){

    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = Modifier
            .width(300.dp)
            .height(140.dp)
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "AI suggestion:",
                    modifier = Modifier.padding(10.dp),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 18.sp
                    ),
                    textAlign = TextAlign.Center,
                )

                IconButton(
                    onClick = {closeHandler()}
                ) {
                    Icon(
                        painter = painterResource(R.drawable.close),
                        contentDescription = "close_card")
                }

            }


            HorizontalDivider(
                thickness = 2.dp
            )

        }

        var stringX = ""
        var stringY = ""
        if( (saddleXCm*10).toInt() > 0){
            stringX = "- Shift saddle right about: ${String.format("%.1f", abs(saddleXCm))} cm"
        }

        if((saddleXCm*10).toInt() < 0){
            stringX = "- Shift saddle left about: ${String.format("%.1f", abs(saddleXCm))} cm"
        }

        if((saddleYCm*10).toInt() > 0){
            stringY = "- Drop saddle about: ${String.format("%.1f", abs(saddleYCm))} cm"
        }

        if((saddleYCm*10).toInt() < 0){
            stringY = "- Lift saddle about: ${String.format("%.1f", abs(saddleYCm))} cm"
        }


        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = stringX,
                style = TextStyle(
                    color = Color.White,
                    //fontSize = 20.sp
                )
            )
            Text(
                text = stringY,
                style = TextStyle(
                    color = Color.White,
                    //fontSize = 20.sp
                )
            )
        }

    }

}