plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    //DI
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")

    //Navigation
    // Kotlin serialization plugin for type safe routes and navigation arguments
    kotlin("plugin.serialization") version "2.0.21"

    //Download from gradle
    id("de.undercouch.download") version "5.6.0"

}

android {
    namespace = "com.example.myposition"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myposition"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "alpha 1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            //isMinifyEnabled = true
            //isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isDebuggable = false
        }
        debug {
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

//Download models
// import DownloadMPTasks task
project.ext.set("ASSET_DIR", "$projectDir/src/main/assets")
apply(from= "download_tasks.gradle")

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //DI
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")

    //View model
    val viewModel_version = "2.8.7"
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$viewModel_version")

    //Navigation
    val nav_version = "2.8.8"
    // Jetpack Compose integration
    implementation("androidx.navigation:navigation-compose:$nav_version")
    // JSON serialization library, works with the Kotlin serialization plugin
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    //Enable hiltViewModel to get view model from hilt
    //https://developer.android.com/develop/ui/compose/libraries#hilt-navigation
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    //Permissions manager
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // CameraX core library using the camera2 implementation
    val camerax_version = "1.5.0-alpha06"
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")

    // MediaPipe Library
    implementation("com.google.mediapipe:tasks-vision:0.10.14")
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}