package com.android.task.data.repository

import android.content.Context
import android.util.Log
import com.android.task.data.model.Product
import com.android.task.data.model.ProductsResponse
import com.android.task.data.model.SearchResponse
import com.android.task.helpers.JsonHelper.Companion.readJSONFromAsset
import com.google.gson.Gson
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class ProductsRepository(private val mContext: Context) {

    companion object {
        private const val TAG = "ProductsRepository"
    }

    fun searchProducts(query: String, offset: Int, limit: Int): Single<SearchResponse> {

        return Single.create { emitter: SingleEmitter<SearchResponse> ->
            val products = mutableListOf<Product>()

            if (query.length > 2) {
                products.addAll(getProducts(query, offset, limit))
            }

            val searchResponse = SearchResponse(query, products)

            emitter.onSuccess(searchResponse)
        }
    }

    private fun getProducts(query: String, offset: Int, limit: Int): List<Product> {

        val products = mutableListOf<Product>()

        val jsonResponse = readJSONFromAsset(mContext) ?: return products

        try {
            val productsResponse = Gson().fromJson(jsonResponse, ProductsResponse::class.java)

            if (productsResponse.products.isNotEmpty()) {

                val tempList = mutableListOf<Product>()
                for (i in productsResponse.products.indices) {
                    if (productsResponse.products[i].title.toLowerCase().contains(query)) {
                        tempList.add(productsResponse.products[i])
                    }
                }

                if (tempList.isNotEmpty() && offset < tempList.size) {
                    if (offset + limit <= tempList.size) {
                        products.addAll(tempList.subList(offset, offset + limit))
                    } else {
                        products.addAll(tempList.subList(offset, tempList.size))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "searchProducts: ", e)
        }

        return products
    }
}