package com.example.tensorflowlitetest.components

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GetPermission(
    content: @Composable() () -> Unit
){

    // Camera permission state
    val cameraPermissionState = rememberMultiplePermissionsState(permissions = arrayListOf(
        Manifest.permission.CAMERA
    ))

    if (cameraPermissionState.allPermissionsGranted) {
        content()
    } else {

        Scaffold {innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding)
            ) {
                cameraPermissionState.permissions.forEach {
                    val permissionState: PermissionState = it
                    val textToShow = if (permissionState.status.shouldShowRationale) {
                        // If the user has denied the permission but the rationale can be shown,
                        // then gently explain why the app requires this permission
                        "${permissionState.permission} permission is important for this app. Please grant the permission."
                    } else {
                        // If it's the first time the user lands on this feature, or the user
                        // doesn't want to be asked again for this permission, explain that the
                        // permission is required
                        "${permissionState.permission} permission required for this feature to be available. " +
                                "Please grant the permission"
                    }
                    Text(textToShow)
                    Button(onClick = { permissionState.launchPermissionRequest() }) {
                        Text("Request permission")
                    }
                }
            }
        }
    }

}