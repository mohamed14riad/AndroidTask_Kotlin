package com.android.task.helpers

import android.content.Context
import android.util.Log
import java.nio.charset.StandardCharsets

class JsonHelper {

    companion object {
        private const val TAG = "JsonHelper"

        fun readJSONFromAsset(context: Context): String? {
            var json: String? = null

            try {
                val inputStream = context.assets.open("products.json")
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()
                json = String(buffer, StandardCharsets.UTF_8)
            } catch (ex: Exception) {
                Log.e(TAG, "readJSONFromAsset: ", ex)
            }

            return json
        }
    }

}