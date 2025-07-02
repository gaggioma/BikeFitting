package com.example.myposition.components

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.FocusMeteringAction.FLAG_AF
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import com.example.myposition.views.viewModel.MyBikePositionViewModel
import java.util.concurrent.Executors


@Composable
fun CameraAnalyzer(
    cameraSelectorInput: Int,
    tapToFocusOffset: Offset?  = null,
    tapToFocusResult: (result: Boolean) -> Unit? = {}
    //zoomValue: Float
) {

    //Get context
    val context = LocalContext.current

    //View model. If this view is inside nav controller the navigation package for hilt
    // is mandatory: androidx.hilt:hilt-navigation-compose
    val vm: MyBikePositionViewModel = hiltViewModel()
    val state = vm.uiState.collectAsState()
    val zoomValue = state.value.zoomValue
    val imageScale = state.value.imageScale

    //Init cameraX
    var cameraProvider: ProcessCameraProvider? = remember { null }
    //val previewUseCase = remember { Preview.Builder().build() }

    //Executor for data analysis
    val executor = Executors.newSingleThreadExecutor()

    //State of CameraControl to take control of zoom
    val cameraControl = remember { mutableStateOf<CameraControl?>(null) }
    val cameraInfo = remember { mutableStateOf<CameraInfo?>(null) }

    //State used to "tap to focus"
    val surfaceMeteringPointFactoryState = remember { mutableStateOf<SurfaceOrientedMeteringPointFactory?>(null) }

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

    //Tap to focus behaviour: https://developer.android.com/media/camera/camerax/configuration#focus-and-metering
    LaunchedEffect(tapToFocusOffset) {
        if(tapToFocusOffset != null && surfaceMeteringPointFactoryState.value != null && tapToFocusOffset != Offset.Unspecified){

            //X and y coordinates are inverted because image is rotated by 90°
            val meteringPoint1 = surfaceMeteringPointFactoryState.value!!.createPoint(tapToFocusOffset.y/imageScale, tapToFocusOffset.x/imageScale)
            val action = FocusMeteringAction.Builder(meteringPoint1, FLAG_AF) // default AF|AE|AWB
            // The action is canceled in 3 seconds (if not set, default is 5s).
            //.setAutoCancelDuration(3, TimeUnit.SECONDS)
            .build()

            val result = cameraControl.value!!.startFocusAndMetering(action)
            // Adds listener to the ListenableFuture if you need to know the focusMetering result.
            result.addListener({
                //result.get()
                //result.get().isFocusSuccessful //returns if the auto focus is successful or not.
                tapToFocusResult(result.get().isFocusSuccessful)
            }, ContextCompat.getMainExecutor(context))
        }
    }

    //Attach all behaviours to cameraProvider
    fun rebindCameraProvider(
        cameraSelectorType : Int
    ) {
        //De-register all lifecycle components
        cameraProvider?.unbindAll()

        //Configure camera
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(cameraSelectorType)
            .build()

        //Configure analyzer
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(executor, { imageProxy ->

            val bitmapImage = imageProxy.toBitmap()

            //Get image infos
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

            //Get current surface to apply MeteringPoint for tap to focus: https://developer.android.com/media/camera/camerax/configuration#metering-point
            surfaceMeteringPointFactoryState.value = SurfaceOrientedMeteringPointFactory(
                bitmapImage.width.toFloat(),
                bitmapImage.height.toFloat(),
                imageAnalysis
            )

            //Execute analysis
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


    //Init camera
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