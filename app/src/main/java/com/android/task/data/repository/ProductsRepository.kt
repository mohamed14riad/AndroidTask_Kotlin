package com.android.task.data.repository

import android.content.Context
import android.util.Log
import com.android.task.api.ApiClient
import com.android.task.api.ApiInterface
import com.android.task.data.model.Product
import com.android.task.data.model.ProductsResponse
import com.android.task.data.model.SearchResponse
import com.android.task.helpers.JsonHelper.Companion.readJSONFromAsset
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class ProductsRepository(private val mContext: Context) {

    companion object {
        private const val TAG = "ProductsRepository"
    }

    fun searchProducts(query: String, offset: Int, limit: Int) = getProducts(query, offset, limit)

    private fun getProducts(query: String, offset: Int, limit: Int): Single<SearchResponse> {
        return Single.create { emitter ->

            try {
                val json = readJSONFromAsset(mContext)

                val productsResponse =
                    Gson().fromJson(json, ProductsResponse::class.java)

                val products = mutableListOf<Product>()

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

                emitter.onSuccess(SearchResponse(query, products))
            } catch (e: Exception) {
                Log.e(TAG, "getProducts: ", e)
                emitter.onError(e)
            }
        }
    }

    fun searchProductsAPI(query: String, offset: Int, limit: Int) =
        getProductsFromAPI(query, offset, limit)

    private fun getProductsFromAPI(query: String, offset: Int, limit: Int): Single<SearchResponse> {
        return Single.create { emitter ->

            val apiInterface = ApiClient.getInstance().create(ApiInterface::class.java)
            val responseSingle = apiInterface.getProducts(ApiClient.PRODUCTS_ENDPOINT)
            responseSingle.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ productsResponse ->

                    val products = mutableListOf<Product>()

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

                    emitter.onSuccess(SearchResponse(query, products))
                }, { e ->
                    Log.e(TAG, "getProductsFromAPI: ", e)
                    emitter.onError(e)
                })
        }
    }
}