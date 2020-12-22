package com.android.task.api

import com.android.task.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    const val BASE_URL = "https://2b7c4d9e-a467-48ea-9453-1a97266e5196.mock.pstmn.io/"
    const val PRODUCTS_ENDPOINT = "products"

    fun getInstance(
        baseUrl: String? = null,
        username: String? = null,
        password: String? = null
    ): Retrofit {
        return initRetrofit(baseUrl, username, password)
    }

    private fun initRetrofit(
        baseUrl: String? = null,
        username: String? = null,
        password: String? = null
    ): Retrofit {

        val okHttpBuilder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            okHttpBuilder.addInterceptor(loggingInterceptor)
        }
        if (username != null && password != null) {
            val authInterceptor = AuthInterceptor(username, password)
            okHttpBuilder.addInterceptor(authInterceptor)
        }
        val okHttpClient = okHttpBuilder.build()

        val mBaseUrl = baseUrl ?: BASE_URL

        return Retrofit.Builder()
            .baseUrl(if (mBaseUrl.endsWith("/")) mBaseUrl else "$mBaseUrl/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .client(okHttpClient)
            .build()
    }
}