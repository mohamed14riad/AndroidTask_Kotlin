package com.android.task.data.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("title") val title: String,
    @SerializedName("type") val type: String,
    @SerializedName("description") val description: String,
    @SerializedName("filename") val filename: String,
    @SerializedName("height") val height: String,
    @SerializedName("width") val width: String,
    @SerializedName("price") val price: String,
    @SerializedName("rating") val rating: String
)