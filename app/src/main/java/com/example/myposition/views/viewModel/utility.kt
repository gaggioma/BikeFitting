package com.example.myposition.views.viewModel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.FileDescriptor

//Take this function from here: https://developer.android.com/training/data-storage/shared/documents-files#bitmap
fun getBitmapFromUri(uri: Uri, context: Context): Bitmap {
    val contentResolver = context.contentResolver
    val parcelFileDescriptor: ParcelFileDescriptor =
        contentResolver.openFileDescriptor(uri, "r")!!
    val fileDescriptor: FileDescriptor = parcelFileDescriptor.fileDescriptor
    val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
    parcelFileDescriptor.close()
    return image
}