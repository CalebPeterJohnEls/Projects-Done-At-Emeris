package com.example.funinactionproto

/** One tutorial slide's content. */
data class TutorialPage(
    val title: String,
    val body: String,
    val iconRes: Int
)

/** The 5 slides, in order — same copy as the original web tutorial. */
fun buildTutorialPages(): List<TutorialPage> = listOf(
    TutorialPage(
        title = "Meet your coach",
        body = "Your Fun in Action mascot is here to help you learn hockey the fun way, one stance at a time.",
        iconRes = R.drawable.athletic_pro_final_transparent
    ),
    TutorialPage(
        title = "We track your skeleton",
        body = "Your camera maps key points on your body — head, shoulders, hips, knees and feet — and follows them in real time as you move.",
        iconRes = R.drawable.tracking
    ),
    TutorialPage(
        title = "Practice your stances",
        body = "Point your camera at yourself and try attacking, defending, receiving, moving with the ball, or tackling.",
        iconRes = R.drawable.camera_icon
    ),
    TutorialPage(
        title = "Instant colour feedback",
        body = "Green means great form, yellow means close, and red means try again. No confusing numbers, just colours.",
        iconRes = R.drawable.palette_icon
    ),
    TutorialPage(
        title = "Level up & earn your certificate",
        body = "Every session builds your level. Finish all 4 sessions to earn your Fun in Action certificate.",
        iconRes = R.drawable.score_icon
    )
)

// Reference:
// Kotlin Foundation (2026) Kotlin data classes. Available at: https://kotlinlang.org/docs/data-classes.html (Accessed: 16 August 2026).
