package com.android.task.api

import com.android.task.data.model.ProductsResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Url

interface ApiInterface {

    @GET
    fun getProducts(@Url url: String): Single<ProductsResponse>
}