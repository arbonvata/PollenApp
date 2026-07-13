package com.arbonvata.pollentracker.data.network
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object PollenRetrofitClient {
    private const val BASE_URL = "https://api.pollenrapporten.se/"

    private val json =
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
            classDiscriminator = "type"
        }

    private val okHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY // Set to BASIC or HEADERS in production
                },
            ).connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    private val retrofit: Retrofit by lazy {
        Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType()),
            ).build()
    }
    val apiService: PollenApiService by lazy {
        retrofit.create(PollenApiService::class.java)
    }
}
