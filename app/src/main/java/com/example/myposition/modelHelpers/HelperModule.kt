package com.example.myposition.modelHelpers

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent


//Create and destroy instances based on this document
//https://developer.android.com/training/dependency-injection/hilt-android#component-lifetimes
@Module
@InstallIn(ViewModelComponent::class)
object HelperModule {

    @Provides
    fun providePoseLandmarks(@ApplicationContext context: Context): PoseLandmarkerHelper{
        return PoseLandmarkerHelper(context = context)
    }


}