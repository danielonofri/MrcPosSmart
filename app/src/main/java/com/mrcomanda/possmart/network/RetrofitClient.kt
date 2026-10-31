package com.mrcomanda.possmart.network

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object RetrofitClient {

    // Interceptor que intercepta la petición y le inyecta la URL configurada dinámicamente
    private val dynamicUrlInterceptor = Interceptor { chain ->
        var request = chain.request()
        val customBaseUrl = AppPreferences.baseUrl.toHttpUrl()

        val newUrl = request.url.newBuilder()
            .scheme(customBaseUrl.scheme)
            .host(customBaseUrl.host)
            .port(customBaseUrl.port)
            .build()

        request = request.newBuilder().url(newUrl).build()
        chain.proceed(request)
    }

    // Interceptor que inyecta la API Key actual
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val request = original.newBuilder()
            .header("X-API-Key", AppPreferences.apiKey)
            .header("Accept", "*/*")
            .build()
        chain.proceed(request)
    }

    val apiService: ApiService by lazy {
        val client = getUnsafeOkHttpClient()
            .addInterceptor(dynamicUrlInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        Retrofit.Builder()
            .baseUrl("https://placeholder.local/") // URL dummy requerida por Retrofit; el interceptor la reemplaza
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient.Builder {
        return try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
        } catch (e: Exception) {
            OkHttpClient.Builder()
        }
    }
}