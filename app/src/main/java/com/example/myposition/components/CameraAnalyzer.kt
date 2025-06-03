package com.example.myposition.components

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import com.example.myposition.views.viewModel.MyBikePositionViewModel
import java.util.concurrent.Executors


@Composable
fun CameraAnalyzer(
    cameraSelectorInput: Int,
    //zoomValue: Float
) {

    //Get context
    val context = LocalContext.current

    //View model. If this view is inside nav controller the navigation package for hilt
    // is mandatory: androidx.hilt:hilt-navigation-compose
    val vm: MyBikePositionViewModel = hiltViewModel()
    val state = vm.uiState.collectAsState()
    val zoomValue = state.value.zoomValue

    //Init cameraX
    var cameraProvider: ProcessCameraProvider? = remember { null }
    //val previewUseCase = remember { Preview.Builder().build() }

    //Executor for data analysis
    val executor = Executors.newSingleThreadExecutor()

    //State of CameraControl to take control of zoom
    val cameraControl = remember { mutableStateOf<CameraControl?>(null) }
    val cameraInfo = remember { mutableStateOf<CameraInfo?>(null) }

    LaunchedEffect(zoomValue) {
        cameraControl.value?.setLinearZoom(zoomValue)
    }

    LaunchedEffect(cameraInfo.value?.cameraState?.value?.type?.name) {
        //val cameraState = cameraInfo.value?.cameraState?.value?.type?.name
        val linearZoom = cameraInfo.value?.zoomState?.value?.linearZoom
        if(linearZoom != null){
            vm.setZoomValue(linearZoom)
        }
    }

    //Attach all behaviours to cameraProvider
    fun rebindCameraProvider(
        cameraSelector : Int
    ) {
        //De-register all lifecycle components
        cameraProvider?.unbindAll()

        //Configure camera
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(cameraSelector)
            .build()

        //Configure analyzer
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(executor, { imageProxy ->

            val bitmapImage = imageProxy.toBitmap()

            //Get image infos
            val rotationDegree = imageProxy.imageInfo.rotationDegrees
            val matrix = Matrix().apply {
                // Rotate the frame received from the camera to be in the same direction as it'll be shown
                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            }
            val rotatedBitmap = Bitmap.createBitmap(
                bitmapImage, 0, 0, bitmapImage.width, bitmapImage.height,
                matrix, true
            )

            //maintain last photo into vm
            vm.setImage(rotatedBitmap)

            //Execute analysis
            //Log.d("MyBikePositionScreen", "rotation: $rotationDegree")
            vm.posePredictionStreaming(
                imageProxy,
                isFrontCamera = false //this option flip the image
            )

            //release object
            imageProxy.close()
        })

        val camera = cameraProvider?.bindToLifecycle(
            context as LifecycleOwner,
            cameraSelector,
            //previewUseCase,
            imageAnalysis
        )

        // For performing operations that affect all outputs: https://developer.android.com/media/camera/camerax/configuration#camera-output
        cameraControl.value = camera?.cameraControl
        cameraInfo.value = camera?.cameraInfo
    }



    LaunchedEffect(Unit) {
        //Init camera provider
        cameraProvider = ProcessCameraProvider.awaitInstance(context)
        //Define behaviour of camera provider
        rebindCameraProvider(cameraSelectorInput)
    }

    LaunchedEffect(cameraSelectorInput) {
        rebindCameraProvider(cameraSelectorInput)
    }

    DisposableEffect(Unit) {
        onDispose {
            //De-register all lifecycle components
            cameraProvider?.unbindAll()
        }
    }

    /*val previewView: PreviewView = remember { PreviewView(context).also {
        previewUseCase.setSurfaceProvider(it.surfaceProvider)
        rebindCameraProvider()
    } }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView },
            modifier = Modifier
                .fillMaxSize()
        )
    }*/
}