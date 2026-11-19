package com.example.funinactionproto

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Size
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.funinactionproto.pose.CoachingFeedbackAdapter
import com.example.funinactionproto.pose.HandFeatureCalculator
import com.example.funinactionproto.pose.PoseFeatureCalculator
import com.example.funinactionproto.pose.PoseLandmarkerHelper
import com.example.funinactionproto.pose.ReferenceScorer
import com.example.funinactionproto.pose.RepAggregator
import com.example.funinactionproto.pose.ScoreResult
import com.example.funinactionproto.pose.SkillConfig
import com.example.funinactionproto.pose.Skills
import com.example.funinactionproto.pose.HandLandmarkerHelper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class GripCaptureFragment :
    Fragment(R.layout.fragment_grip_capture) {

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 1001
    }

    private lateinit var skill: SkillConfig

    private lateinit var flipCameraBtn: Button
    private lateinit var previewView: PreviewView
    private lateinit var stateText: TextView
    private lateinit var captureBtn: Button
    private lateinit var timer3s: Button
    private lateinit var timer5s: Button
    private lateinit var timer10s: Button
    private lateinit var overallScoreText: TextView
    private lateinit var feedbackList: RecyclerView

    private lateinit var landmarkerHelper:
            PoseLandmarkerHelper

    private lateinit var handLandmarkerHelper:
            HandLandmarkerHelper
    private lateinit var skeletonOverlay: SkeletonOverlayView

    private lateinit var cameraExecutor:
            ExecutorService

    private var cameraSelector =
        CameraSelector.DEFAULT_FRONT_CAMERA

    private var cameraProvider:
            ProcessCameraProvider? = null

    private var selectedTimerSeconds = 5

    private val isChallenge: Boolean
        get() = arguments?.getBoolean(
            ChallengeFlow.ARG_CHALLENGE,
            false
        ) == true

    /*
     * Latest hand features.
     */
    private var latestHandFeatures:
            Map<String, Float> = emptyMap()

    /*
     * During the actual capture we store all
     * combined pose + hand frames.
     */
    private val capturedFrames =
        mutableListOf<Map<String, Float>>()

    private var isCapturing = false

    private val mainHandler =
        Handler(Looper.getMainLooper())

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        val skillId =
            arguments?.getString("skillId")
                ?: "grip"

        skill =
            Skills.byId(skillId)

        cameraExecutor =
            Executors.newSingleThreadExecutor()
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )
        skeletonOverlay = view.findViewById(R.id.skeletonOverlay)

        previewView =
            view.findViewById(
                R.id.previewView
            )

        stateText =
            view.findViewById(
                R.id.stateText
            )

        captureBtn =
            view.findViewById(
                R.id.captureBtn
            )

        timer3s =
            view.findViewById(
                R.id.timer3s
            )

        timer5s =
            view.findViewById(
                R.id.timer5s
            )

        timer10s =
            view.findViewById(
                R.id.timer10s
            )

        overallScoreText = view.findViewById(R.id.overallScoreText)
        feedbackList = view.findViewById(R.id.feedbackList)


        flipCameraBtn =
            view.findViewById(
                R.id.flipCameraBtn
            )

        feedbackList.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        flipCameraBtn.setOnClickListener {
            flipCamera()
        }

        setupTimers()

        if (
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            setupCamera()

        } else {

            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA
                ),
                CAMERA_PERMISSION_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (
            requestCode ==
            CAMERA_PERMISSION_REQUEST
        ) {

            if (
                grantResults.isNotEmpty() &&
                grantResults[0] ==
                PackageManager.PERMISSION_GRANTED
            ) {

                setupCamera()

            } else {

                stateText.text =
                    "Camera permission required"

                Toast.makeText(
                    requireContext(),
                    "Camera permission is required.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun flipCamera() {
        cameraProvider?.unbindAll()
        if (::landmarkerHelper.isInitialized) landmarkerHelper.close()
        if (::handLandmarkerHelper.isInitialized) handLandmarkerHelper.close()
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA)
            CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA
        setupCamera()
    }

    private fun setupTimers() {

        listOf(
            timer3s,
            timer5s,
            timer10s
        ).forEach { button ->

            button.setOnClickListener {

                selectedTimerSeconds =
                    when (button) {

                        timer3s -> 3
                        timer5s -> 5
                        else -> 10
                    }

                listOf(
                    timer3s,
                    timer5s,
                    timer10s
                ).forEach {
                    it.alpha = 0.5f
                }

                button.alpha = 1f
            }
        }

        timer5s.alpha = 1f

        captureBtn.setOnClickListener {
            startCountdownCapture()
        }
    }

    private fun startCountdownCapture() {

        if (isCapturing) {
            return
        }

        capturedFrames.clear()

        isCapturing = false

        stateText.text =
            "Get ready..."

        overallScoreText.text =
            "Preparing..."

        /*
         * Wait for the selected countdown.
         */
        mainHandler.postDelayed({

            capturedFrames.clear()

            isCapturing = true

            stateText.text =
                "Hold your grip..."

            /*
             * Stop recording after the selected
             * amount of time.
             */
            mainHandler.postDelayed({

                isCapturing = false

                scoreCapturedGrip()

            }, selectedTimerSeconds * 1000L)

        }, 1000L)
    }

    private fun scoreCapturedGrip() {

        if (capturedFrames.isEmpty()) {
            stateText.text = "No grip frames detected."
            overallScoreText.text = "Score: —"
            return
        }

        val aggregated = RepAggregator.aggregate(capturedFrames.toList())

        val result = ReferenceScorer.score(
            requireContext(),
            skill,
            aggregated
        )

        val overall = result.overall

        if (overall == null) {
            stateText.text = "Grip could not be scored. Please try again."
            overallScoreText.text = "Score: —"
            return
        }

        // Save the same percentage displayed to the player.
        ProgressStore.recordAttempt(
            requireContext(),
            skill.skillId,
            overall.percent.toFloat()
        )

        showScore(result, overall.percent)

        stateText.text = "Grip scored!"

        // Save the Grip achievement and advance if this is the report card challenge.
        val isChallenge = arguments?.getBoolean(ChallengeFlow.ARG_CHALLENGE) == true

        if (isChallenge) {
            ChallengeSession.results[skill.skillId] = overall
            ChallengeFlow.onSkillFinished(requireActivity(), skill.skillId)
        }
    }

    private fun showScore(result: ScoreResult, percentage: Int) {
        val overall = result.overall
        overallScoreText.text = if (overall != null) {
            "Grip: ${overall.level.level}/7 · ${overall.percent}%\n${overall.level.descriptor}"
        } else {
            "Grip: —"
        }

        feedbackList.adapter = CoachingFeedbackAdapter(result.coachingFeedback)
    }

    private fun setupCamera() {

        landmarkerHelper =
            PoseLandmarkerHelper(

                context =
                    requireContext(),

                onResult = { landmarks, _ ->
                    skeletonOverlay.update(landmarks)
                    if (
                        landmarks == null ||
                        !PoseFeatureCalculator
                            .isFrameReliable(
                                landmarks,
                                skill.skillId
                            )
                    ) {
                        return@PoseLandmarkerHelper
                    }

                    val poseFeatures =
                        PoseFeatureCalculator
                            .computeFrameFeatures(
                                landmarks
                            )

                    val combined =
                        poseFeatures +
                                latestHandFeatures

                    /*
                     * Only save frames while the
                     * actual practice timer is running.
                     */
                    if (isCapturing) {

                        synchronized(
                            capturedFrames
                        ) {

                            capturedFrames.add(
                                combined
                            )
                        }
                    }
                },

                onError = { message ->

                    mainHandler.post {

                        if (isAdded) {

                            Toast.makeText(
                                requireContext(),
                                "Pose error: $message",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },

                /*
                 * The same camera bitmap is also
                 * sent to the Hand Landmarker.
                 */
                onFrame = { bitmap, _ ->
                    skeletonOverlay.setFrameGeometry(
                        bitmap.width,
                        bitmap.height,
                        0
                    )
                }
            )

        /*
         * Hand tracking
         */
        handLandmarkerHelper =
            HandLandmarkerHelper(

                context =
                    requireContext(),

                onResult = {
                        handLandmarks,
                        handedness,
                        _
                    ->

                    latestHandFeatures =
                        HandFeatureCalculator
                            .computeHandFeatureMap(
                                handLandmarks,
                                handedness
                            )
                },

                onError = { message ->

                    /*
                     * Hand tracking failing should
                     * not crash the entire camera.
                     */
                    mainHandler.post {

                        if (isAdded) {

                            stateText.text =
                                "Keep both hands visible"
                        }
                    }
                }
            )

        handLandmarkerHelper.setup()
        landmarkerHelper.setup()

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(
                requireContext()
            )

        cameraProviderFuture.addListener({

            try {

                cameraProvider =
                    cameraProviderFuture.get()
                skeletonOverlay.mirrorX = (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA)

                val preview =
                    Preview.Builder()
                        .build()
                        .also {

                            it.setSurfaceProvider(
                                previewView
                                    .surfaceProvider
                            )
                        }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(640, 480))
                    .setTargetRotation(previewView.display.rotation)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(
                    cameraExecutor
                ) { imageProxy ->

                    try {

                        landmarkerHelper
                            .detectAsync(
                                imageProxy,
                                imageProxy.imageInfo.rotationDegrees
                            )

                    } catch (e: Exception) {

                        imageProxy.close()
                    }
                }

                cameraProvider?.unbindAll()

                cameraProvider?.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

                stateText.text =
                    "Camera ready"

            } catch (e: Exception) {

                Toast.makeText(
                    requireContext(),
                    "Camera error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

        }, ContextCompat.getMainExecutor(
            requireContext()
        ))
    }

    override fun onDestroyView() {

        super.onDestroyView()

        isCapturing = false

        if (::landmarkerHelper.isInitialized) {
            landmarkerHelper.close()
        }

        if (::handLandmarkerHelper.isInitialized) {
            handLandmarkerHelper.close()
        }

        cameraProvider?.unbindAll()
    }

    override fun onDestroy() {

        super.onDestroy()

        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
    }
}