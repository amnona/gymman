package com.example.gymtracker.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp

@Composable
fun ExerciseImageThumbnail(
    imagePath: String?,
    modifier: Modifier = Modifier,
    placeholderColor: Color = Color(0x1F4CAF50),
    maxImageDimensionPx: Int = 256
) {
    val bitmap = remember(imagePath) {
        imagePath?.takeIf { it.isNotBlank() }?.let { decodeSampledBitmap(it, maxImageDimensionPx) }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(placeholderColor),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun decodeSampledBitmap(path: String, maxImageDimensionPx: Int): Bitmap? {
    if (maxImageDimensionPx <= 0) {
        return BitmapFactory.decodeFile(path)
    }

    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, boundsOptions)

    val sourceWidth = boundsOptions.outWidth
    val sourceHeight = boundsOptions.outHeight
    if (sourceWidth <= 0 || sourceHeight <= 0) {
        return null
    }

    var sampleSize = 1
    while ((sourceWidth / sampleSize) > maxImageDimensionPx || (sourceHeight / sampleSize) > maxImageDimensionPx) {
        sampleSize *= 2
    }

    val decodedBitmap = BitmapFactory.decodeFile(
        path,
        BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
    )

    return decodedBitmap?.let { applyExifOrientation(path, it) }
}

private fun applyExifOrientation(path: String, bitmap: Bitmap): Bitmap {
    val orientation = runCatching { ExifInterface(path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL) }
        .getOrDefault(ExifInterface.ORIENTATION_UNDEFINED)

    if (orientation == ExifInterface.ORIENTATION_NORMAL || orientation == ExifInterface.ORIENTATION_UNDEFINED) {
        return bitmap
    }

    val matrix = Matrix().apply {
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> postScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                postRotate(180f)
                postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                postRotate(90f)
                postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                postRotate(-90f)
                postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(-90f)
        }
    }

    return runCatching {
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }.getOrDefault(bitmap)
}



