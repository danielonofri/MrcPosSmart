package com.mrcomanda.possmart.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrcomanda.possmart.network.RetrofitClient
import com.mrcomanda.possmart.network.obtenerAccessTokenDiario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

data class OrderItem(
    val id: String,
    val status: String,
    val externalReference: String,
    val totalAmount: Double,
    val dateCreated: String,
    val lastUpdatedDate: String,
    val terminalId: String,
    val paymentId: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdenesDiaScreen() {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var listaOrdenes by remember { mutableStateOf<List<OrderItem>>(emptyList()) }
    var esVistaGrilla by remember { mutableStateOf(false) } // Toggle entre Grilla y Lista

    LaunchedEffect(Unit) {
        try {
            isLoading = true
            errorMessage = null

            // 1. Obtenemos el token OAuth diario
            val accessToken = obtenerAccessTokenDiario(RetrofitClient.apiService)
            if (accessToken.isBlank()) {
                errorMessage = "No se pudo obtener el Access Token de OAuth."
                isLoading = false
                return@LaunchedEffect
            }

            // 2. Preparamos las fechas del día actual en formato ISO 8601
            val tz = TimeZone.getTimeZone("UTC")
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'00:00:00'Z'", Locale.US).apply { timeZone = tz }
            val dateFormatEnd = SimpleDateFormat("yyyy-MM-dd'T'23:59:59'Z'", Locale.US).apply { timeZone = tz }
            val ahora = Date()

            val beginDateStr = dateFormat.format(ahora)
            val endDateStr = dateFormatEnd.format(ahora)

            // 3. Consumimos el endpoint GET /v1/orders de Mercado Pago
            val url = "https://api.mercadopago.com/v1/orders?begin_date=$beginDateStr&end_date=$endDateStr"
            android.util.Log.d("MrComandaLog", "-> [Ordenesdeldia] Url: $url")
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()

            val responseBodyString = withContext(Dispatchers.IO) {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw Exception("Error HTTP ${response.code}: ${response.body?.string()}")
                    }
                    response.body?.string() ?: ""
                }
            }
            android.util.Log.d("MrComandaLog", "-> [Respuesta] Url: $responseBodyString")
            // 4. Parseamos la respuesta JSON de Mercado Pago (¡Cambiado de "elements" a "data"!)
            val jsonObject = JSONObject(responseBodyString)
            val jsonArray = jsonObject.optJSONArray("data") ?: org.json.JSONArray()
            val tempOrdenes = mutableListOf<OrderItem>()

            for (i in 0 until jsonArray.length()) {
                val itemObj = jsonArray.optJSONObject(i) ?: continue
                val id = itemObj.optString("id", "")
                val status = itemObj.optString("status", "desconocido")
                val externalRef = itemObj.optString("external_reference", "-")
                val dateCreated = itemObj.optString("created_date", "")
                val lastUpdatedDate = itemObj.optString("last_updated_date", "")

                // Extraer terminal_id desde config -> point -> terminal_id
                val configObj = itemObj.optJSONObject("config")
                val pointObj = configObj?.optJSONObject("point")
                val terminalId = pointObj?.optString("terminal_id", "") ?: ""

                // Extraer monto y payment id desde transactions -> payments[0]
                var totalAmount = 0.0
                var paymentId = ""
                val transactionsObj = itemObj.optJSONObject("transactions")
                if (transactionsObj != null) {
                    val paymentsArray = transactionsObj.optJSONArray("payments")
                    if (paymentsArray != null && paymentsArray.length() > 0) {
                        val paymentObj = paymentsArray.optJSONObject(0)
                        totalAmount = paymentObj?.optDouble("amount", 0.0) ?: 0.0
                        paymentId = paymentObj?.optString("id", "") ?: ""
                    }
                }

                tempOrdenes.add(
                    OrderItem(
                        id = id,
                        status = status,
                        externalReference = externalRef,
                        totalAmount = totalAmount,
                        dateCreated = dateCreated,
                        lastUpdatedDate = lastUpdatedDate,
                        terminalId = terminalId,
                        paymentId = paymentId
                    )
                )
            }

            listaOrdenes = tempOrdenes

            listaOrdenes = tempOrdenes

        } catch (e: Exception) {
            errorMessage = "Error al consultar órdenes:\n${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Cabecera local con el Botón para alternar Grilla / Lista
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Órdenes de Hoy (${listaOrdenes.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Botón Exportar CSV (WhatsApp / Drive / etc.)
                IconButton(
                    onClick = { compartirCsvOrdenes(context, listaOrdenes) },
                    enabled = listaOrdenes.isNotEmpty()
                ) {
                    Text("📤", fontSize = 16.sp)
                }

                // Botón Toggle Grilla / Lista
                IconButton(onClick = { esVistaGrilla = !esVistaGrilla }) {
                    Text(if (esVistaGrilla) "📋" else "🗂️", fontSize = 16.sp)
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                errorMessage != null -> {
                    SelectionContainer(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "Error desconocido",
                            color = Color.Red,
                            fontSize = 15.sp
                        )
                    }
                }
                listaOrdenes.isEmpty() -> {
                    Text(
                        text = "No se registran órdenes para el día de hoy.",
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 15.sp,
                        color = Color.Gray
                    )
                }
                esVistaGrilla -> {
                    // Vista en Cuadrícula
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listaOrdenes) { orden ->
                            OrdenCardItem(orden)
                        }
                    }
                }
                else -> {
                    // Vista en Lista Lineal
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listaOrdenes) { orden ->
                            OrdenCardItem(orden)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrdenCardItem(orden: OrderItem) {
    val colorEstado = when (orden.status.lowercase()) {
        "closed", "finished", "approved" -> Color(0xFFC8E6C9) // Verde
        "cancelled", "expired" -> Color(0xFFFFCDD2)          // Rojo
        else -> Color(0xFFFFF9C4)                            // Amarillo
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = colorEstado)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Fila 1: ID de la Orden completo y visible
            Text(
                text = "ID: ${orden.id}",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.Black
            )

            // Fila 2: Referencia Externa
            Text(
                text = "Ref: ${orden.externalReference}",
                fontSize = 10.sp,
                color = Color.DarkGray
            )

            // Fila 3: Monto y Estado destacados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monto: $${orden.totalAmount}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    color = Color(0xFF2E7D32)
                )
                Surface(
                    color = Color.Black.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(3.dp)
                ) {
                    Text(
                        text = orden.status.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Fila 4: Payment ID (si existe)
            if (orden.paymentId.isNotEmpty()) {
                Text(
                    text = "PayID: ${orden.paymentId}",
                    fontSize = 9.sp,
                    color = Color.Gray
                )
            }

            // Fila 5: Fechas y Terminal ID
            val fechaCrea = formatearFechaMp(orden.dateCreated)
            val fechaAct = formatearFechaMp(orden.lastUpdatedDate)
            val termShort = if (orden.terminalId.isNotEmpty()) orden.terminalId.substringAfter("__").take(15) else "-"

            Text(
                text = "Cre: $fechaCrea | Act: $fechaAct",
                fontSize = 9.sp,
                color = Color.Gray
            )
            Text(
                text = "Terminal: $termShort",
                fontSize = 9.sp,
                color = Color.Gray
            )
        }
    }
}

fun formatearFechaMp(fechaStr: String): String {
    return try {
        // Recibe "2026-11-02T09:43:07.603Z" y extrae hasta los segundos
        val fechaLimpia = fechaStr.substringBefore(".").replace("T", " ")
        val partes = fechaLimpia.split(" ")
        if (partes.size == 2) {
            val fechaPartes = partes[0].split("-") // [2026, 11, 02]
            if (fechaPartes.size == 3) {
                val diaMes = "${fechaPartes[2]}/${fechaPartes[1]}"
                val hora = partes[1] // HH:mm:ss
                return "$diaMes $hora"
            }
        }
        fechaStr
    } catch (_: Exception) {
        fechaStr
    }
}

// Genera el texto CSV de forma nativa y liviana
fun generarCsvOrdenes(ordenes: List<OrderItem>): String {
    val sb = StringBuilder()
    sb.append("ID,ExternalReference,Monto,Estado,FechaCreacion,FechaActualizacion,TerminalID,PaymentID\n")
    for (o in ordenes) {
        sb.append("${o.id},${o.externalReference},${o.totalAmount},${o.status},${o.dateCreated},${o.lastUpdatedDate},${o.terminalId},${o.paymentId}\n")
    }
    return sb.toString()
}

// Comparte el archivo CSV usando el sistema nativo de Android (WhatsApp, Drive, Gmail, etc.)
fun compartirCsvOrdenes(context: Context, ordenes: List<OrderItem>) {
    try {
        if (ordenes.isEmpty()) return
        val csvText = generarCsvOrdenes(ordenes)

        // Archivo temporal en la caché (pesa 0 MB en la APK)
        val archivo = File(context.cacheDir, "ordenes_dia_${System.currentTimeMillis()}.csv")
        archivo.writeText(csvText)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Reporte de Órdenes del Día - Mr Comanda")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Exportar órdenes CSV"))
    } catch (e: Exception) {
        android.util.Log.e("MrComandaLog", "Error al exportar CSV: ${e.localizedMessage}")
    }
}