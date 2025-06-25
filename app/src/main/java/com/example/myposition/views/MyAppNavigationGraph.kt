package com.example.myposition.views

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tensorflowlitetest.components.GetPermission
import kotlinx.serialization.Serializable

@Serializable
object MyBikePositionRealtime

@Serializable
object MyBikePositionVideo

@Serializable
object MyBikeInitScreen

@Composable
fun MyAppNavigationGraph(){

    //Define navigation controller
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = MyBikeInitScreen) {

        // Add more destinations similarly.
        composable<MyBikePositionRealtime> {
            GetPermission {
                KeepScreenOn()
                MyBikePositionRealTime()
            }
        }

        composable<MyBikePositionVideo> {
            GetPermission {
                KeepScreenOn()
                MyBikePositionVideo()
            }
        }

        composable<MyBikeInitScreen> {
            GetPermission{
                MyBikeInitScreen(navController = navController)
            }
        }


    }

}