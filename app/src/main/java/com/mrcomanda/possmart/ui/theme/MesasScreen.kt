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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrcomanda.possmart.R
import com.mrcomanda.possmart.model.Mesa
import com.mrcomanda.possmart.network.AppPreferences
import com.mrcomanda.possmart.network.RetrofitClient
import androidx.compose.runtime.mutableIntStateOf

@Composable
fun MesasScreen() {
    var mesaSeleccionada by remember { mutableStateOf<Mesa?>(null) }
    var listaMesas by remember { mutableStateOf<List<Mesa>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }
    var triggerReintento by remember { mutableIntStateOf(0) }

    LaunchedEffect(triggerReintento) {
        try {
            isLoading = true
            errorMensaje = null
            listaMesas = RetrofitClient.apiService.getMesasAbiertas(apiKey = AppPreferences.apiKey)
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
            onVolver = { mesaSeleccionada = null }
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
                // Pantalla amigable de desconexión de red / servidor
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

                    // Opcional colapsable con el detalle técnico exacto para depuración
                    SelectionContainer {
                        Text(
                            text = "Detalle: $errorMensaje",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón para reintentar la conexión limpiamente
                    Button(
                        onClick = { triggerReintento++ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                    ) {
                        Text("Reintentar Conexión", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listaMesas) { mesa ->
                        val colorContenedor = if (mesa.cerrando) Color(0xFFFFCDD2) else Color(0xFFFFE0B2)

                        Card(
                            modifier = Modifier
                                .height(90.dp)
                                .clickable { mesaSeleccionada = mesa },
                            shape = RoundedCornerShape(8.dp),
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
                                        text = "Cuenta ${mesa.mesa}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "$${mesa.total.toInt()}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFFE65100)
                                    )
                                }

                                if (mesa.cerrando) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Surface(
                                            color = Color.Red.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(1.5.dp, Color.Red.copy(alpha = 0.7f)),
                                            modifier = Modifier.graphicsLayer(rotationZ = -12f)
                                        ) {
                                            Text(
                                                text = " $ PROCESANDO ",
                                                color = Color.Red.copy(alpha = 0.9f),
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