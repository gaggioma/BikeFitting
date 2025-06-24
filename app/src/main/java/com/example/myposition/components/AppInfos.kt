package com.example.myposition.components

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.pm.PackageInfoCompat

@Composable
fun AppInfos(){

    val context = LocalContext.current

    val appInfos: AppVersion? = getAppVersion(context)

    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "App version name: ${appInfos?.versionName}"
        )

        Text(
            text = "App version number: ${appInfos?.versionNumber}"
        )

        var debugText = "Build type: "
        val isDebuggable = appInfos?.packageName?.contains("debug") == true
        if(isDebuggable){
            debugText += "debug"
        }else{
            debugText += "release"
        }
        Text(
            text = debugText
        )


    }
}

data class AppVersion(
    val versionName: String?,
    val packageName: String,
    val versionNumber: Long,
)

fun getAppVersion(
    context: Context,
): AppVersion? {
    return try {
        val packageManager = context.packageManager
        val packageName = context.packageName
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            packageManager.getPackageInfo(packageName, 0)
        }
        AppVersion(
            versionName = packageInfo.versionName,
            packageName = packageInfo.packageName,
            versionNumber = PackageInfoCompat.getLongVersionCode(packageInfo),
        )
    } catch (e: Exception) {
        null
    }
}