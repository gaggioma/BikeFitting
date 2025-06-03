package com.example.myposition.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import com.example.myposition.R
import com.example.myposition.components.AppInfos
import com.example.myposition.components.NavigationIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBikeInitScreen(
    navController: NavHostController
){

    val showInfoScreen = remember{ mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(
                        onClick = {
                            showInfoScreen.value = !showInfoScreen.value
                        }
                    ) {
                        Icon(
                            painter = painterResource(if(!showInfoScreen.value) R.drawable.info else R.drawable.close),
                            contentDescription = "info"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        if(!showInfoScreen.value) {

        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .background(color = Color.White)
        ){


                Image(
                    modifier = Modifier
                        .matchParentSize(),
                    painter = painterResource(id = R.drawable.cycling_man),
                    contentDescription = "cycling_man",
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NavigationIcon(navController = navController)
                }


        }

        }else{
        AppInfos()
    }

    }


}