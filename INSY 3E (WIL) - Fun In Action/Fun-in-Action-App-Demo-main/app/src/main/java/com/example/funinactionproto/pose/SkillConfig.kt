package com.example.funinactionproto.pose

enum class CaptureMode {
    PHOTO, VIDEO_AUTO_DETECT
}

data class SkillConfig(
    val skillId: String,
    val displayName: String,
    val captureMode: CaptureMode,
    val referenceAssetPath: String
)

object Skills {

    val ALL = listOf(

        SkillConfig(
            skillId = "grip",
            displayName = "Grip",
            captureMode = CaptureMode.PHOTO,
            referenceAssetPath = "models/grip_reference.json"
        ),

        SkillConfig(
            skillId = "push_pass",
            displayName = "Push Pass",
            captureMode = CaptureMode.VIDEO_AUTO_DETECT,
            referenceAssetPath = "models/push_pass_reference.json"
        ),

        SkillConfig(
            skillId = "receiving_ball",
            displayName = "Receiving",
            captureMode = CaptureMode.VIDEO_AUTO_DETECT,
            referenceAssetPath = "models/receiving_ball_reference.json"
        ),

        SkillConfig(
            skillId = "running_with_ball",
            displayName = "Running with the Ball",
            captureMode = CaptureMode.VIDEO_AUTO_DETECT,
            referenceAssetPath = "models/running_with_ball_reference.json"
        )
    )

    fun byId(skillId: String): SkillConfig =
        ALL.first { it.skillId == skillId }
}