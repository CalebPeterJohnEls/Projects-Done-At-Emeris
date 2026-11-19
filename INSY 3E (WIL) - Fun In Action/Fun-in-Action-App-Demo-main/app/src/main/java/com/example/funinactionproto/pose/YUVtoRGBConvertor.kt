package com.example.funinactionproto.pose

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.media.Image
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy


class YuvToRgbConverter {

    @OptIn(ExperimentalGetImage::class)
    fun yuvToRgb(bitmap: Bitmap, imageProxy: ImageProxy) {
        val image: Image = imageProxy.image ?: throw IllegalArgumentException("ImageProxy has no Image")
        if (image.format != ImageFormat.YUV_420_888) {
            throw IllegalArgumentException("Expected YUV_420_888, got ${image.format}")
        }

        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val yRowStride = image.planes[0].rowStride
        val uvRowStride = image.planes[1].rowStride
        val uvPixelStride = image.planes[1].pixelStride

        val width = imageProxy.width
        val height = imageProxy.height

        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            val yPos = y * yRowStride
            for (x in 0 until width) {
                val yIndex = yPos + x
                val yVal = (yBuffer.get(yIndex).toInt() and 0xFF) - 16

                // U/V are subsampled 2x2
                val uvX = x / 2
                val uvY = y / 2
                val uvIndex = (uvY * uvRowStride) + (uvX * uvPixelStride)

                val uVal = (uBuffer.get(uvIndex).toInt() and 0xFF) - 128
                val vVal = (vBuffer.get(uvIndex).toInt() and 0xFF) - 128

                // YUV -> RGB
                var r = (298 * yVal + 409 * vVal + 128) shr 8
                var g = (298 * yVal - 100 * uVal - 208 * vVal + 128) shr 8
                var b = (298 * yVal + 516 * uVal + 128) shr 8

                r = r.coerceIn(0, 255)
                g = g.coerceIn(0, 255)
                b = b.coerceIn(0, 255)

                val alpha = 255
                pixels[y * width + x] = (alpha shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }
}