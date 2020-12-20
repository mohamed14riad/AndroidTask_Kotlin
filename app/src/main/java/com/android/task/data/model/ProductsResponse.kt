package com.android.task.data.model

import com.google.gson.annotations.SerializedName

data class ProductsResponse(
    @SerializedName("products") val products: List<Product>
)