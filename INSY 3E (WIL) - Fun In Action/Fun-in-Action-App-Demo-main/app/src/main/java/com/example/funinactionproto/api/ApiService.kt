package com.example.funinactionproto.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT   // NEW: needed for the update-profile endpoint

// Each function maps to an endpoint on the external API, which is the only layer that talks to the Azure SQL database (Android Developers, 2026b; IIE Emeris School of Computer Science, 2025a; 2025b; The IIE, 2026c).
interface ApiService {
    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<AuthResponse>

    @GET("api/profile")
    fun getProfile(): Call<ProfileDto>

    // NEW: updates name, email and phone. Returns the saved profile (a body-less 200/204 is also handled in the fragment).
    @PUT("api/profile")
    fun updateProfile(@Body request: UpdateProfileRequest): Call<ProfileDto>

    // NEW: success has no useful body, so Void is used. Failures are read from errorBody() in the fragment.
    @POST("api/profile/change-password")
    fun changePassword(@Body request: ChangePasswordRequest): Call<Void>
}

// Android Developers, 2026b. Guide to app architecture: Data layer. [online] Available at: <https://developer.android.com/topic/architecture/data-layer> [Accessed 20 September 2026]
// IIE Emeris School of Computer Science, 2025a. CLDV6211 - Playlist. [video online] Available at: <https://www.youtube.com/playlist?list=PL480DYS-b_kevhFsiTpPIB2RzhKPig4iK> [Accessed 18 March 2025]
// IIE Emeris School of Computer Science, 2025b. CLDV6212 - Playlist. [video online] Available at: <https://www.youtube.com/playlist?list=PL480DYS-b_kcZiyuCyHolh6Nad8J_Xnk7> [Accessed 20 September 2025]
// The IIE, 2026c. Programming 3D [PROG7314 Module Manual]. The Independent Institute of Education: Unpublished