
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
import com.example.funinactionproto.pose.Achievement
import com.example.funinactionproto.pose.AchievementScale
import com.example.funinactionproto.pose.CoachingFeedbackAdapter
import com.example.funinactionproto.pose.PoseFeatureCalculator
import com.example.funinactionproto.pose.PoseLandmarkerHelper
import com.example.funinactionproto.pose.ReferenceScorer
import com.example.funinactionproto.pose.RepAggregator
import com.example.funinactionproto.pose.ScoreResult
import com.example.funinactionproto.pose.SkillConfig
import com.example.funinactionproto.pose.Skills
import java.io.FileNotFoundException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Video capture screen using a manual repetition counter.
 *
 * Flow:
 * Start -> Get ready -> Perform repetition -> Complete Rep -> Scoring.
 *
 * In challenge mode, three successfully scored repetitions are averaged.
 */
class VideoCaptureFragment : Fragment(R.layout.fragment_video_capture) {

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 1002
        private const val GET_READY_MS = 3000L
        private const val MIN_FRAMES_PER_REP = 10
    }

    private lateinit var skill: SkillConfig

    private val challengeRepCount = 3
    private var completedChallengeReps = 0
    private val challengeRepScores = mutableListOf<Achievement>()

    private val isChallenge: Boolean
        get() = arguments?.getBoolean(ChallengeFlow.ARG_CHALLENGE) == true

    private lateinit var previewView: PreviewView
    private lateinit var flipCameraBtn: Button
    private lateinit var stateText: TextView
    private lateinit var startBtn: Button
    private lateinit var completeRepBtn: Button
    private lateinit var repCounterText: TextView
    private lateinit var overallScoreText: TextView
    private lateinit var feedbackList: RecyclerView
    private lateinit var skeletonOverlay: SkeletonOverlayView

    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

    private var landmarkerHelper: PoseLandmarkerHelper? = null

    // Pose frames are collected while the user is performing a repetition.
    private val frameLock = Any()
    private val currentRepFrames = mutableListOf<Map<String, Float>>()

    @Volatile
    private var captureFrames = false

    private var sessionActive = false

    private val mainHandler = Handler(Looper.getMainLooper())
    private var pendingReadyRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val skillId = arguments?.getString("skillId") ?: "push_pass"
        skill = Skills.byId(skillId)

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        previewView = view.findViewById(R.id.previewView)
        flipCameraBtn = view.findViewById(R.id.flipCameraBtn)
        stateText = view.findViewById(R.id.stateText)
        startBtn = view.findViewById(R.id.startBtn)
        completeRepBtn = view.findViewById(R.id.completeRepBtn)
        repCounterText = view.findViewById(R.id.repCounterText)
        overallScoreText = view.findViewById(R.id.overallScoreText)
        feedbackList = view.findViewById(R.id.feedbackList)
        skeletonOverlay = view.findViewById(R.id.skeletonOverlay)

        feedbackList.layoutManager = LinearLayoutManager(requireContext())

        stateText.text = "${skill.displayName} - tap Start when you're ready"

        updateRepCounter()

        completeRepBtn.visibility = View.GONE
        completeRepBtn.isEnabled = false

        flipCameraBtn.setOnClickListener {
            flipCamera()
        }

        startBtn.setOnClickListener {
            onStartTapped()
        }

        completeRepBtn.setOnClickListener {
            onCompleteRepTapped()
        }

        if (
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (
                grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                startCamera()
            } else {
                stateText.text = "Camera permission required"

                Toast.makeText(
                    requireContext(),
                    "Camera permission is required.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // ---------------------------------------------------------------------
    // Session control
    // ---------------------------------------------------------------------

    private fun onStartTapped() {
        if (sessionActive) {
            cancelSession()
        } else {
            startSession()
        }
    }

    private fun startSession() {
        if (landmarkerHelper == null) {
            stateText.text = "Camera not ready yet"
            return
        }

        cancelPendingReady()

        completedChallengeReps = 0
        challengeRepScores.clear()

        synchronized(frameLock) {
            currentRepFrames.clear()
        }

        captureFrames = false
        sessionActive = true

        overallScoreText.text = "Score: —"
        feedbackList.adapter = null

        updateRepCounter()

        startBtn.text = "Cancel"

        completeRepBtn.visibility = View.VISIBLE
        completeRepBtn.isEnabled = false

        stateText.text = "Get ready..."

        // Give the player three seconds to get into position.
        val readyRunnable = Runnable {
            if (!isAdded || !sessionActive) return@Runnable

            synchronized(frameLock) {
                currentRepFrames.clear()
            }

            captureFrames = true
            completeRepBtn.isEnabled = true

            stateText.text = "Perform one repetition, then tap Complete Rep"
        }

        pendingReadyRunnable = readyRunnable
        mainHandler.postDelayed(readyRunnable, GET_READY_MS)
    }

    private fun cancelSession() {
        cancelPendingReady()

        captureFrames = false
        sessionActive = false

        synchronized(frameLock) {
            currentRepFrames.clear()
        }

        startBtn.text = "Start"

        completeRepBtn.isEnabled = false
        completeRepBtn.visibility = View.GONE

        stateText.text = "${skill.displayName} - tap Start when you're ready"
    }

    private fun cancelPendingReady() {
        pendingReadyRunnable?.let {
            mainHandler.removeCallbacks(it)
        }

        pendingReadyRunnable = null
    }

    // ---------------------------------------------------------------------
    // Manual repetition counter
    // ---------------------------------------------------------------------

    private fun onCompleteRepTapped() {
        if (!sessionActive || !captureFrames) return

        // Stop collecting frames while this repetition is being scored.
        captureFrames = false
        completeRepBtn.isEnabled = false

        val frames = synchronized(frameLock) {
            currentRepFrames.toList()
        }

        if (frames.size < MIN_FRAMES_PER_REP) {
            captureFrames = true
            completeRepBtn.isEnabled = true

            stateText.text =
                "Not enough movement recorded. Perform the repetition before tapping Complete Rep."

            return
        }

        stateText.text = "Scoring repetition..."

        scoreRepetition(frames)
    }

    private fun scoreRepetition(
        bufferedFrames: List<Map<String, Float>>
    ) {
        if (!isAdded || !sessionActive) return

        val aggregated = RepAggregator.aggregate(bufferedFrames)

        val result = try {
            ReferenceScorer.score(
                requireContext(),
                skill,
                aggregated
            )
        } catch (e: FileNotFoundException) {
            sessionActive = false
            captureFrames = false

            startBtn.text = "Start"
            completeRepBtn.isEnabled = false
            completeRepBtn.visibility = View.GONE

            stateText.text =
                "No reference available for ${skill.displayName} yet"

            return
        }

        val overall = result.overall

        // If scoring failed, allow the same repetition to be attempted again.
        if (overall == null) {
            synchronized(frameLock) {
                currentRepFrames.clear()
            }

            captureFrames = true
            completeRepBtn.isEnabled = true

            stateText.text =
                "Couldn't score that repetition. Please try again."

            return
        }

        // Save the successful practice attempt.
        ProgressStore.recordAttempt(
            requireContext(),
            skill.skillId,
            overall.percent.toFloat()
        )

        showScore(result)

        // -------------------------------------------------------------
        // Normal practice: one repetition is enough.
        // -------------------------------------------------------------

        if (!isChallenge) {
            sessionActive = false
            captureFrames = false

            startBtn.text = "Try again"

            completeRepBtn.isEnabled = false
            completeRepBtn.visibility = View.GONE

            stateText.text = "Repetition scored!"

            return
        }

        // -------------------------------------------------------------
        // Challenge mode: collect three successful repetitions.
        // -------------------------------------------------------------

        challengeRepScores.add(overall)
        completedChallengeReps++

        updateRepCounter()

        if (completedChallengeReps < challengeRepCount) {

            // Prepare to collect the next repetition immediately.
            synchronized(frameLock) {
                currentRepFrames.clear()
            }

            captureFrames = true
            completeRepBtn.isEnabled = true

            stateText.text =
                "Rep $completedChallengeReps scored! Perform your next repetition."

        } else {

            // Average the percentages from all three repetitions.
            val averagePercent = challengeRepScores
                .map { it.percent }
                .average()
                .toFloat()

            val finalAchievement =
                AchievementScale.achievementFromPercent(averagePercent)

            // Save the final Push Pass achievement.
            ChallengeSession.results[skill.skillId] = finalAchievement

            sessionActive = false
            captureFrames = false

            startBtn.text = "Completed"

            completeRepBtn.isEnabled = false
            completeRepBtn.visibility = View.GONE

            overallScoreText.text =
                "${skill.displayName} average: " +
                        "${finalAchievement.level.level}/7 · " +
                        "${finalAchievement.percent}%\n" +
                        finalAchievement.level.descriptor

            stateText.text = "All three repetitions completed!"

            // Continue to the next challenge screen.
            ChallengeFlow.onSkillFinished(
                requireActivity(),
                skill.skillId
            )
        }
    }

    private fun updateRepCounter() {
        repCounterText.text = if (isChallenge) {
            "Repetition ${completedChallengeReps + 1} of $challengeRepCount"
        } else {
            "One repetition"
        }
    }

    private fun showScore(result: ScoreResult) {
        val overall = result.overall

        overallScoreText.text = if (overall != null) {
            "${skill.displayName}: ${overall.level.level}/7 · " +
                    "${overall.percent}%\n${overall.level.descriptor}"
        } else {
            "${skill.displayName}: —"
        }

        feedbackList.adapter =
            CoachingFeedbackAdapter(result.coachingFeedback)
    }

    // ---------------------------------------------------------------------
    // Camera + pose
    // ---------------------------------------------------------------------

    private fun flipCamera() {
        cameraSelector = if (
            cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA
        ) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }

        bindCamera()
    }

    private fun startCamera() {
        if (landmarkerHelper == null) {
            landmarkerHelper = PoseLandmarkerHelper(
                context = requireContext(),

                onResult = { landmarks, _ ->

                    // Keep the skeleton overlay responsive.
                    skeletonOverlay.update(landmarks)

                    // Collect pose features only while the user is performing
                    // a repetition.
                    if (
                        captureFrames &&
                        landmarks != null &&
                        PoseFeatureCalculator.isFrameReliable(
                            landmarks,
                            skill.skillId
                        )
                    ) {
                        val features =
                            PoseFeatureCalculator.computeFrameFeatures(landmarks)

                        synchronized(frameLock) {
                            // Check again inside the lock in case the user
                            // tapped Complete Rep while this frame was processing.
                            if (captureFrames) {
                                currentRepFrames.add(features.toMap())
                            }
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

                onFrame = { bitmap, _ ->
                    skeletonOverlay.setFrameGeometry(
                        bitmap.width,
                        bitmap.height,
                        0
                    )
                }
            ).also {
                it.setup()
            }
        }

        bindCamera()
    }

    private fun bindCamera() {
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({

            try {
                cameraProvider = cameraProviderFuture.get()

                skeletonOverlay.mirrorX =
                    cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(640, 480))
                    .setBackpressureStrategy(
                        ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                    )
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->

                    val helper = landmarkerHelper

                    if (helper == null) {
                        imageProxy.close()
                        return@setAnalyzer
                    }

                    try {
                        helper.detectAsync(
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

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Camera error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    // ---------------------------------------------------------------------
    // Cleanup
    // ---------------------------------------------------------------------

    override fun onDestroyView() {
        super.onDestroyView()

        cancelPendingReady()

        captureFrames = false
        sessionActive = false

        synchronized(frameLock) {
            currentRepFrames.clear()
        }

        landmarkerHelper?.close()
        landmarkerHelper = null

        cameraProvider?.unbindAll()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
    }
}
