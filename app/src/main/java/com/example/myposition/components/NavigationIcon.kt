package com.example.myposition.components

import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import com.example.myposition.R
import com.example.myposition.views.MyBikePositionRealtime
import com.example.myposition.views.MyBikePositionVideo


@Composable
fun NavigationIcon(
    navController: NavHostController
){
        //real time
        Button(
            modifier = Modifier,
            onClick = {
                navController.navigate(MyBikePositionRealtime)/*{
                    popUpTo(navController.graph.id){
                        inclusive = true
                    }
                }*/
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.videocam),
                contentDescription = "real_time_page",
            )
            Text("Real time analysis")
        }

        //Camera
        Button(
            modifier = Modifier,
            onClick = {
                navController.navigate(MyBikePositionVideo)/*{
                    popUpTo(navController.graph.id){
                        inclusive = true
                    }
                }*/
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.upload_file),
                contentDescription = "video_page",
            )
            Text("Video/photo analysis")
        }
}