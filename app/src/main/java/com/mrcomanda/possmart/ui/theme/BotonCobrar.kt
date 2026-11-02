package com.mrcomanda.possmart.ui.theme

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.mrcomanda.possmart.model.Mesa
import com.mrcomanda.possmart.network.AppPreferences
import com.mrcomanda.possmart.network.MercadoPagoPaymentManager
import com.mrcomanda.possmart.network.RetrofitClient
import com.mrcomanda.possmart.network.obtenerAccessTokenDiario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

@Composable
fun BotonCobrar(
    mesa: Mesa,
    totalCalculado: Double,
    cobroLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    Button(
        onClick = {
            val guidReferencia = UUID.randomUUID().toString()
            android.util.Log.d("MrComandaLog", "-> [BotonCobrar] Clic detectado | Mesa: ${mesa.mesa} | Monto: $$totalCalculado | Ref (GUID): $guidReferencia")
            android.util.Log.d("MrComandaLog", "-> [BotonCobrar] Estado OAuth configurado en AppPreferences: ${AppPreferences.oauth}")
            android.util.Log.d("MrComandaLog", "-> Iniciando cobro | Mesa: ${mesa.mesa} | Monto: $$totalCalculado | Ref (GUID): $guidReferencia")

            coroutineScope.launch {
                try {
                    if (AppPreferences.oauth) {
                        // 1. FLUJO CON OAUTH: Obtenemos el token diario cacheado
                        android.util.Log.d("MrComandaLog", "-> OAuth activo: Solicitando token diario...")
                        val accessToken = obtenerAccessTokenDiario(RetrofitClient.apiService)
                        if (accessToken.isBlank()) {
                            android.util.Log.e("MrComandaLog", "-> [OAuth] ERROR: El token obtenido está vacío.")
                            return@launch
                        }
                        android.util.Log.d("MrComandaLog", "-> [OAuth] Token obtenido correctamente (longitud: ${accessToken.length})")
                        // 2. Construimos la llamada HTTP con OkHttp para crear la orden en Mercado Pago
                        val client = OkHttpClient()

                        val terminalIdUsar = AppPreferences.device.ifBlank { "" }
                        val esChile = AppPreferences.pais.equals("Chile", ignoreCase = true)

                        val montoJsonSegment: Any = if (esChile) {
                            totalCalculado.toInt() // Entero puro sin decimales para Chile
                        } else {
                            String.format(java.util.Locale.US, "%.2f", totalCalculado) // Con decimales para Argentina
                        }

                        val jsonBody = """
                        {
                          "type": "point",
                          "external_reference": "$guidReferencia",
                          "transactions": {
                            "payments": [
                              {
                                "amount": "$montoJsonSegment"
                              }
                            ]
                          },
                          "config": {
                            "point": {
                              "terminal_id": "$terminalIdUsar"
                            }
                          }
                        }
                        """.trimIndent()

                        android.util.Log.d("MrComandaLog", "-> [BotonCobrar] JSON exacto a enviar:\n$jsonBody")


                        android.util.Log.d("MrComandaLog", "-> [OAuth] Enviando petición POST a /v1/orders con external_reference: $guidReferencia")
                        android.util.Log.d("MrComandaLog", "-> [OAuth] JSON a enviar: $jsonBody")

                        val request = Request.Builder()
                            .url("https://api.mercadopago.com/v1/orders")
                            .post(jsonBody.toRequestBody("application/json".toMediaTypeOrNull()))
                            .addHeader("Authorization", "Bearer $accessToken")
                            .addHeader("X-Idempotency-Key", guidReferencia)
                            .build()

                        withContext(Dispatchers.IO) {
                            client.newCall(request).execute().use { resp ->
                                val body = resp.body?.string()
                                android.util.Log.d("MrComandaLog", "-> [OAuth] Respuesta HTTP recibida | Código: ${resp.code} | Body: $body")
                                if (!resp.isSuccessful) {
                                    throw Exception("HTTP ${resp.code}: $body")
                                }
                                android.util.Log.d("MrComandaLog", "-> Orden creada con éxito en MP: $body")
                            }
                        }

                    } else {
                        // 3. FLUJO NATIVO SIN OAUTH: El método clásico directo con la terminal enlazada
                        MercadoPagoPaymentManager.iniciarCobro(
                            launcher = cobroLauncher,
                            monto = totalCalculado,
                            externalReference = guidReferencia
                        )
                    }

                } catch (e: Exception) {
                    android.util.Log.e("MrComandaLog", "Error al procesar cobro OAuth: ${e.localizedMessage}")
                }
            }
        },
        modifier = modifier,
        enabled = !mesa.cerrando
    ) {
        Text(if (mesa.cerrando) "En Proceso" else "Cobrar", fontSize = 13.sp)
    }
}