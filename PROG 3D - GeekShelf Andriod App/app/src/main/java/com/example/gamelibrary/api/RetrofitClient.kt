package com.example.gamelibrary.api

import com.example.gamelibrary.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = BuildConfig.GIANTBOMB_BASE_URL

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // (Square, n.d.)
    }

    private val giantBombInterceptor = Interceptor { chain -> // (Square, n.d.)
        val original = chain.request()
        val urlWithParams = original.url.newBuilder()
            .addQueryParameter("api_key", BuildConfig.GIANTBOMB_API_KEY)
            .addQueryParameter("format", "json")
            .build()
        val requestWithHeader = original.newBuilder()
            .url(urlWithParams)
            .header("User-Agent", "GeekShelf/1.0 - PoC Android app, university project")
            .build()
        chain.proceed(requestWithHeader)
    }

    private val okHttpClient = OkHttpClient.Builder() // (The IIE, 2026)
        .addInterceptor(giantBombInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()  // (The IIE, 2026)
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val gameApiService: GameApiService by lazy { // (The IIE, 2026)
        retrofit.create(GameApiService::class.java)
    }
    private val comicVineInterceptor = Interceptor { chain ->
        val original = chain.request()
        val urlWithParams = original.url.newBuilder()
            .addQueryParameter("api_key", BuildConfig.COMICVINE_API_KEY)
            .addQueryParameter("format", "json")
            .build()
        val requestWithHeader = original.newBuilder()
            .url(urlWithParams)
            .header("User-Agent", "GeekShelf/1.0 - PoC Android app, university project")
            .build()
        chain.proceed(requestWithHeader)
    }

    private val comicVineOkHttpClient = OkHttpClient.Builder()
        .addInterceptor(comicVineInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val comicVineRetrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.COMICVINE_BASE_URL)
        .client(comicVineOkHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val comicVineApiService: ComicVineApiService by lazy {
        comicVineRetrofit.create(ComicVineApiService::class.java)

    }
    private val tmdbInterceptor = Interceptor { chain ->
        val original = chain.request()
        val urlWithKey = original.url.newBuilder()
            .addQueryParameter("api_key", BuildConfig.TMDB_API_KEY)
            .build()
        chain.proceed(original.newBuilder().url(urlWithKey).build())
    }

    private val tmdbOkHttpClient = OkHttpClient.Builder()
        .addInterceptor(tmdbInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val tmdbRetrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.TMDB_BASE_URL)
        .client(tmdbOkHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val tmdbApiService: TmdbApiService by lazy {
        tmdbRetrofit.create(TmdbApiService::class.java)
    }

    private val jikanOkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // no auth interceptor — Jikan needs none
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jikanRetrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.JIKAN_BASE_URL)
        .client(jikanOkHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val jikanApiService: JikanApiService by lazy {
        jikanRetrofit.create(JikanApiService::class.java)
    }
}
/* Reference List
Square (n.d.) Retrofit. Available at: https://square.github.io/retrofit/ (Accessed: 15 September 2026).
The IIE, 2026. Programming 3D [PROG7314 Module Manual]. The Independent Institute of Education: Unpublished.
*/
