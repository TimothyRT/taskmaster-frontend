package com.pads.taskmaster.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Singleton for Retrofit client
 */
object RetrofitClient {
    private const val TAG = "RetrofitClient"
    private const val BASE_URL = "http://10.0.2.2:5002/api/"
    
    // Custom JSON logger interceptor
    private class JsonLoggingInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val response = chain.proceed(request)
            
            try {
                if (response.isSuccessful && response.body != null) {
                    val contentType = response.body?.contentType()
                    if (contentType?.subtype?.contains("json") == true) {
                        val bodyString = response.peekBody(Long.MAX_VALUE).string()
                        Log.d(TAG, "Raw JSON Response: $bodyString")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error logging response", e)
            }
            
            return response
        }
    }
    
    private class DateAdapter : JsonDeserializer<Date> {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        private val fallbackFormats = listOf(
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        )

        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): Date? {
            try {
                // Handle null dates
                if (json.isJsonNull) {
                    return null
                }
                
                val dateString = json.asString
                
                // Try with the primary format
                try {
                    return dateFormat.parse(dateString)
                } catch (e: ParseException) {
                    // Try fallback formats
                    for (format in fallbackFormats) {
                        try {
                            return format.parse(dateString)
                        } catch (e: ParseException) {
                            // Try next format
                        }
                    }
                }
                
                // If all else fails, return current date
                return Date()
            } catch (e: Exception) {
                return null
            }
        }
    }
    
    private class BooleanAdapter : JsonDeserializer<Boolean> {
        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): Boolean {
            return when {
                json.isJsonPrimitive -> {
                    val primitive = json.asJsonPrimitive
                    when {
                        primitive.isBoolean -> primitive.asBoolean
                        primitive.isNumber -> primitive.asInt != 0
                        primitive.isString -> {
                            val stringValue = primitive.asString.lowercase(Locale.getDefault())
                            when (stringValue) {
                                "true", "yes", "1", "on" -> true
                                else -> false
                            }
                        }
                        else -> false
                    }
                }
                else -> false
            }
        }
    }
    
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, DateAdapter())
        .registerTypeAdapter(Boolean::class.java, BooleanAdapter())
        .registerTypeAdapter(Boolean::class.javaPrimitiveType, BooleanAdapter())
        .setPrettyPrinting() // For better log readability
        .create()
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(JsonLoggingInterceptor()) // Add our custom JSON logger
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
} 