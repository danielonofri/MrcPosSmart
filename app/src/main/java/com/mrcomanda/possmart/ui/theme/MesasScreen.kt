package com.mrcomanda.possmart.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrcomanda.possmart.PrinterHelper
import com.mrcomanda.possmart.R
import com.mrcomanda.possmart.model.Mesa
import com.mrcomanda.possmart.network.AppPreferences
import com.mrcomanda.possmart.network.RetrofitClient

@Composable
fun MesasScreen() {
    var mesaSeleccionada by remember { mutableStateOf<Mesa?>(null) }
    var listaMesas by remember { mutableStateOf<List<Mesa>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }
    var triggerReintento by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val esquinaCinta = RoundedCornerShape(4.dp)

    LaunchedEffect(triggerReintento) {
        try {
            isLoading = true
            errorMensaje = null
            listaMesas = RetrofitClient.apiService.getMesasAbiertas(apiKey = AppPreferences.apiKey)
            android.util.Log.d(
                "MrComandaLog",
                "Mesas recibidas: " + listaMesas.joinToString { "mesa=${it.mesa}/cerrando=${it.cerrando}" }
            )
        } catch (e: Exception) {
            errorMensaje = e.localizedMessage ?: "Error desconocido de red"
        } finally {
            isLoading = false
        }
    }

    val mesaActual = mesaSeleccionada
    if (mesaActual != null) {
        DetalleMesaScreen(
            mesa = mesaActual,
            onVolver = {
                mesaSeleccionada = null
                triggerReintento++
            }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // --- MARCA DE AGUA DE FONDO CON EL LOGO ---
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo_mrc),
                contentDescription = "Marca de agua MRC",
                modifier = Modifier
                    .size(180.dp)
                    .alpha(0.05f)
            )
        }

        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            errorMensaje != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🔌❌",
                        fontSize = 54.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Sin conexión con el servidor",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFFD32F2F)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "No se pudo establecer comunicación con la API central. Verifique que la red Wi-Fi esté activa y el servidor encendido.",
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SelectionContainer {
                        Text(
                            text = "Detalle: $errorMensaje",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { triggerReintento++ },
                        shape = esquinaCinta,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                    ) {
                        Text("Reintentar Conexión", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Cinta superior de todo el ancho con botón "Imprimir mesas"
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = esquinaCinta,
                        color = Color(0xFFF3F4F6),
                        border = BorderStroke(1.dp, Color(0xFFD0D5DD))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mesas: ${listaMesas.size}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1F2937)
                            )

                            Button(
                                onClick = {
                                    android.util.Log.d("MrComandaLog", "-> [Imprimir Mesas Integrada] cantidad=${listaMesas.size}")
                                    PrinterHelper.imprimirMesasAbiertasPrinterIntegrada(
                                        context = context,
                                        mesas = listaMesas
                                    )
                                },
                                enabled = listaMesas.isNotEmpty(),
                                shape = esquinaCinta,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Text(
                                    text = "Imprimir mesas",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listaMesas) { mesa ->
                            val colorContenedor = if (mesa.cerrando) Color(0xFFC8E6C9) else Color(0xFFFFE0B2)

                            Card(
                                modifier = Modifier
                                    .height(90.dp)
                                    .clickable { mesaSeleccionada = mesa },
                                shape = RoundedCornerShape(6.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = colorContenedor
                                )
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(8.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "${mesa.mesa}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "$${mesa.total.toInt()}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (mesa.cerrando) Color(0xFF1B5E20) else Color(0xFFE65100)
                                        )
                                    }

                                    if (mesa.cerrando) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Surface(
                                                color = Color(0xFF2E7D32).copy(alpha = 0.18f),
                                                shape = RoundedCornerShape(4.dp),
                                                border = BorderStroke(1.5.dp, Color(0xFF2E7D32).copy(alpha = 0.8f)),
                                                modifier = Modifier.graphicsLayer(rotationZ = -12f)
                                            ) {
                                                Text(
                                                    text = " COBRADA ",
                                                    color = Color(0xFF1B5E20),
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}