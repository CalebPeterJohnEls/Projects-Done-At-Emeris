package com.example.funinactionproto.api

// Data classes mirror the JSON the API expects and returns; Gson converts them automatically (CodeLucky, 2025; The IIE, 2026c).
data class LoginRequest(val email: String, val password: String)

data class RegisterRequest(
    val firstName: String, val lastName: String, val email: String,
    val phone: String, val password: String, val role: String
)

// Response from the API after the user is saved or verified in the database (Microsoft, 2026a).
data class AuthResponse(
    val token: String, val expiresOn: String, val role: String,
    val fullName: String, val email: String, val clientId: String?
)

data class ProfileDto(
    val id: String, val fullName: String, val email: String,
    val phone: String, val role: String
)

// NEW: body for PUT api/profile (edit personal information).
// Gson sends these property names as the JSON keys (fullName, email, phone),
// so they must match the API's update-profile request. Role and id are not sent
// because the API ignores them and keeps the stored values.
data class UpdateProfileRequest(
    val fullName: String,
    val email: String,
    val phone: String
)

// NEW: body for POST api/profile/change-password.
// Keys sent: currentPassword, newPassword. Check these names against the
// API's change-password request class.
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

// CodeLucky, 2025. Retrofit Tutorial – REST API Client for Android with Kotlin. [video online] 9 October. Available at: <https://www.youtube.com/watch?v=iIjHHZNb_Qk> [Accessed 23 September 2026]
// Microsoft, 2026a. Minimal APIs security. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/fundamentals/minimal-apis/security?view=aspnetcore-10.0> [Accessed 23 September 2026]
// The IIE, 2026c. Programming 3D [PROG7314 Module Manual]. The Independent Institute of Education: Unpublished