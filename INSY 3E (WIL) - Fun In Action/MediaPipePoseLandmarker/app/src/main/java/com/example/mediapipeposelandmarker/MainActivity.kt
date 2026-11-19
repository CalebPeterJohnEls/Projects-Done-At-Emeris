package com.example.mediapipeposelandmarker

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), PoseLandmarkerHelper.LandmarkerListener, HandLandmarkerHelper.HandLandmarkerListener {
    private lateinit var overlayView: OverlayView
    private lateinit var handLandmarkerHelper: HandLandmarkerHelper
    private lateinit var poseLandmarkerHelper: PoseLandmarkerHelper
    private lateinit var previewView: PreviewView
    private lateinit var cameraExecutor: ExecutorService

    private var isFrontCamera = true  // change to false for rear camera

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        overlayView = findViewById(R.id.overlayView)
        cameraExecutor = Executors.newSingleThreadExecutor()

        poseLandmarkerHelper = PoseLandmarkerHelper(context = this, listener = this)
        handLandmarkerHelper = HandLandmarkerHelper(context = this, listener = this)
        handLandmarkerHelper.setup()
        poseLandmarkerHelper.setup()

        if (cameraPermissionGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), 100
            )
        }
    }

    // ---- Start CameraX ----
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Preview — what the user sees on screen
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // ImageAnalysis — what gets sent to MediaPipe
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val bitmapBuffer = Bitmap.createBitmap(
                            imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888
                        )
                        imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }

                        val matrix = Matrix().apply {
                            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                            if (isFrontCamera) postScale(-1f, 1f, imageProxy.width / 2f, imageProxy.height / 2f)
                        }

                        val rotatedBitmap = Bitmap.createBitmap(
                            bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height, matrix, true
                        )

                        val timestamp = imageProxy.imageInfo.timestamp

                        poseLandmarkerHelper.detectBitmap(rotatedBitmap, timestamp)
                        handLandmarkerHelper.detectBitmap(rotatedBitmap, timestamp)
                    }
                }

            // Select front or rear camera
            val cameraSelector = if (isFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (e: Exception) {
                Log.e("CameraX", "Binding failed: ${e.message}")
            }

        }, ContextCompat.getMainExecutor(this))
    }

    // ---- Handle permission result ----
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            Log.e("Camera", "Permission denied")
        }
    }

    // ---- MediaPipe results arrive here every frame ----
    override fun onResults(result: PoseLandmarkerResult, imageWidth: Int, imageHeight: Int) {
        runOnUiThread {
            if (result.landmarks().isEmpty()) {
                overlayView.clear()
            } else {
                overlayView.setResults(result, imageWidth, imageHeight)
            }
        }
    }

    override fun onError(error: String) {
        Log.e("Pose", "Error: $error")
    }

    // ---- Cleanup ----
    override fun onDestroy() {
        super.onDestroy()
        poseLandmarkerHelper.close()
        handLandmarkerHelper.close()
        cameraExecutor.shutdown()
    }

    private fun cameraPermissionGranted() =
        ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    override fun onHandResults(result: HandLandmarkerResult) {
        runOnUiThread {
            overlayView.setHandResults(result)
        }
    }

    override fun onHandError(error: String) {
        Log.e("Hand", "Error: $error")
    }

}