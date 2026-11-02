package com.mrcomanda.possmart.network

import android.util.Log
import com.google.gson.Gson
import com.mrcomanda.possmart.model.TokenMpResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun obtenerAccessTokenDiario(apiService: ApiService): String {
    val hoy = LocalDate.now().toString()
    Log.d("MrComandaLog", "-> [TokenRepository] Verificando token diario. Fecha actual: $hoy")

    if (AppPreferences.accessTokenMp.isNotBlank() && AppPreferences.fechaTokenMp == hoy) {
        Log.d("MrComandaLog", "-> [TokenRepository] Token recuperado desde caché (válido para hoy).")
        return AppPreferences.accessTokenMp
    }

    val baseUrl = AppPreferences.urlTokenMp
    val tokenMrcHeader = AppPreferences.tokenMrc
    val integracion = AppPreferences.integracion
    val licencia = AppPreferences.licencia
    val cuit = AppPreferences.cuit

    val urlCompleta = "$baseUrl?integracion=$integracion&licencia=$licencia&cuit=$cuit"
    Log.d("MrComandaLog", "-> [TokenRepository] URL completa OkHttp: '$urlCompleta'")

    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(urlCompleta)
                .get()
                .addHeader("Authorization", "Bearer $tokenMrcHeader")
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                Log.d("MrComandaLog", "-> [TokenRepository] Respuesta HTTP Code: ${response.code} | Body length: ${bodyString.length}")

                if (!response.isSuccessful) {
                    throw Exception("HTTP ${response.code}: $bodyString")
                }

                val tokenResponse = Gson().fromJson(bodyString, TokenMpResponse::class.java)
                val tokenRecibido = tokenResponse?.access_token ?: ""

                if (tokenRecibido.isBlank()) {
                    Log.e("MrComandaLog", "-> [TokenRepository] ERROR: El JSON llegó pero 'access_token' está vacío.")
                } else {
                    Log.d("MrComandaLog", "-> [TokenRepository] Token obtenido con éxito vía OkHttp.")
                }

                AppPreferences.accessTokenMp = tokenRecibido
                AppPreferences.fechaTokenMp = hoy

                tokenRecibido
            }
        } catch (e: Exception) {
            Log.e("MrComandaLog", "-> [TokenRepository] EXCEPCIÓN al solicitar el token con OkHttp: ${e.localizedMessage}")
            AppPreferences.accessTokenMp
        }
    }
}