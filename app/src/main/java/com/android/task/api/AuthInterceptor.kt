package com.android.task.api

import okhttp3.Credentials.basic
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

internal class AuthInterceptor(username: String, password: String) : Interceptor {

    private val authorization = basic(username, password)

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val builder = originalRequest.newBuilder()
        builder.header("Authorization", authorization)

        val authenticatedRequest = builder.build()
        return chain.proceed(authenticatedRequest)
    }
}