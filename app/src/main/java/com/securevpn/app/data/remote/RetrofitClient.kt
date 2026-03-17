package com.securevpn.app.data.remote

import com.securevpn.app.BuildConfig
import com.securevpn.app.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client.
 * Privacy note:
 *  - Logging interceptor is enabled ONLY in debug builds.
 *  - No user data or browsing traffic is logged.
 *  - HTTPS is enforced via network_security_config.xml.
 */
object RetrofitClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY  // debug only
        } else {
            HttpLoggingInterceptor.Level.NONE  // no logging in release
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(Constants.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(Constants.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(Constants.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        // TODO: Add auth token interceptor here when login is implemented
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
