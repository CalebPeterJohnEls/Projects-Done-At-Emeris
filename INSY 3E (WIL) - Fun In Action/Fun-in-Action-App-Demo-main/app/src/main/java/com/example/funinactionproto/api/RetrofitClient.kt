package com.example.funinactionproto.api

import android.content.Context
import android.content.pm.ApplicationInfo   // CHANGED: replaces the BuildConfig import
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // The app talks to the deployed Azure API (not the emulator's 10.0.2.2 localhost alias) (Android Developers, 2026c).
    private const val BASE_URL = "https://funinaction-api-bbdnawdfawanftc0.southafricanorth-01.azurewebsites.net/"

    // A single shared Retrofit instance is created once and reused (Square, 2026; The IIE, 2026c).
    @Volatile private var retrofit: Retrofit? = null

    fun api(context: Context): ApiService =
        (retrofit ?: synchronized(this) {
            retrofit ?: build(context.applicationContext).also { retrofit = it }
        }).create(ApiService::class.java)

    private fun build(context: Context): Retrofit {
        // Adds the saved JWT as a Bearer header so the API can identify the user (Microsoft, 2026a).
        // The token is read on every request, so it always uses the currently logged-in user's token.
        val authInterceptor = Interceptor { chain ->
            val token = SessionManager(context).getToken()
            val req = if (!token.isNullOrEmpty())
                chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
            else chain.request()
            chain.proceed(req)
        }

        // CHANGED: true only for debug builds. Android sets FLAG_DEBUGGABLE on debug installs,
        // so this works without BuildConfig or any Gradle changes.
        val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        // The logging interceptor prints requests and responses for debugging only (OkHttp, 2026).
        // BODY level would log passwords, so it is only enabled in debug builds.
        val logging = HttpLoggingInterceptor().apply {
            level = if (isDebuggable) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
            redactHeader("Authorization") // never print the bearer token, even while debugging
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

// Android Developers, 2026c. Set up Android Emulator networking. [online] Available at: <https://developer.android.com/studio/run/emulator-networking> [Accessed 21 September 2026]
// Microsoft, 2026a. Minimal APIs security. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/fundamentals/minimal-apis/security?view=aspnetcore-10.0> [Accessed 23 September 2026]
// OkHttp, 2026. logging-interceptor.api. [online] Available at: <https://github.com/lysine-dev/okhttp/blob/main/okhttp-logging-interceptor/api/logging-interceptor.api> [Accessed 21 September 2026]
// Square, 2026. Retrofit. [online] Available at: <https://square.github.io/retrofit/> [Accessed 16 September 2026]
// The IIE, 2026c. Programming 3D [PROG7314 Module Manual]. The Independent Institute of Education: Unpublished