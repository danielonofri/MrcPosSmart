package com.mrcomanda.possmart.ui.theme

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrcomanda.possmart.PrinterHelper
import com.mrcomanda.possmart.R // <--- ¡IMPORTANTE: Agregado para resolver 'R'!
import com.mrcomanda.possmart.model.ItemFacturaDto
import com.mrcomanda.possmart.model.Mesa
import com.mrcomanda.possmart.network.AppPreferences
import com.mrcomanda.possmart.network.MercadoPagoPaymentManager
import com.mrcomanda.possmart.network.RetrofitClient
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.alpha

@Composable
fun MesasScreen() {
    var mesaSeleccionada by remember { mutableStateOf<Mesa?>(null) }
    var listaMesas by remember { mutableStateOf<List<Mesa>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            isLoading = true
            listaMesas = RetrofitClient.apiService.getMesasAbiertas(apiKey = AppPreferences.apiKey)
        } catch (e: Exception) {
            errorMensaje = "Error al cargar cuentas:\n${e.localizedMessage}"
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
                SelectionContainer(
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                ) {
                    Text(
                        text = errorMensaje ?: "Error desconocido",
                        color = Color.Red,
                        fontSize = 15.sp
                    )
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
                                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.Red.copy(alpha = 0.7f)),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleMesaScreen(mesa: Mesa, onVolver: () -> Unit) {
    val context = LocalContext.current
    var itemsFactura by remember { mutableStateOf<List<ItemFacturaDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val cobroLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        android.util.Log.d("MrComandaLog", "<- Resultado recibido del SDK | resultCode: ${result.resultCode} (RESULT_OK = -1, RESULT_CANCELED = 0)")

        result.data?.extras?.let { bundle ->
            for (key in bundle.keySet()) {
                val value = bundle.get(key)
                android.util.Log.d("MrComandaLog", "   Bundle extra -> $key : $value")
            }
        }

        MercadoPagoPaymentManager.procesarResultado(
            resultCode = result.resultCode,
            data = result.data,
            onAprobado = { paymentId ->
                android.util.Log.d("MrComandaLog", "-> Pago Aprobado con ID: $paymentId")
                android.widget.Toast.makeText(context, "¡Pago Aprobado! ID: $paymentId", android.widget.Toast.LENGTH_LONG).show()

                // Llamada al backend para registrar el pago con la API Key configurada
                coroutineScope.launch {
                    try {
                        RetrofitClient.apiService.postRegistrarPago(
                            numeroMesa = mesa.mesa,
                            apiKey = AppPreferences.apiKey
                        )
                        android.widget.Toast.makeText(context, "Pago registrado en el servidor correctamente", android.widget.Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "Error al registrar pago: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                    }
                }

                // Impresión del ticket de pre-cuenta / comprobante
                PrinterHelper.imprimirPrecuenta(
                    context = context,
                    items = itemsFactura,
                    total = mesa.total,
                    numeroMesa = mesa.mesa
                )
            },
            onRechazadoOMas = { motivo ->
                android.util.Log.d("MrComandaLog", "-> Pago Rechazado / Cancelado / Error. Motivo: $motivo")
                android.widget.Toast.makeText(context, "Respuesta SDK: $motivo", android.widget.Toast.LENGTH_LONG).show()
            }
        )
    }

    LaunchedEffect(mesa.mesa) {
        try {
            isLoading = true
            itemsFactura = RetrofitClient.apiService.getFacturaMesa(mesa.mesa, apiKey = AppPreferences.apiKey)
        } catch (e: Exception) {
            errorMensaje = "Error al cargar:\n${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    val totalCalculado = itemsFactura.sumOf { it.precio }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cuenta ${mesa.mesa} - Detalle") },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("← Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                errorMensaje != null -> {
                    SelectionContainer(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    ) {
                        Text(
                            text = errorMensaje ?: "Error desconocido",
                            color = Color.Red,
                            fontSize = 15.sp
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Ítems pedidos:", fontWeight = FontWeight.Bold, color = Color.Gray)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }

                        items(itemsFactura) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = item.descrip, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = "Cant: ${item.cantidad} - Cód: ${item.codigo}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Text(
                                        text = "$${item.precio}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                tonalElevation = 4.dp,
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("TOTAL:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text(
                                            text = "$$totalCalculado",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                PrinterHelper.imprimirPrecuenta(
                                                    context = context,
                                                    items = itemsFactura,
                                                    total = totalCalculado,
                                                    numeroMesa = mesa.mesa
                                                )
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Pre-cuenta", fontSize = 13.sp)
                                        }

                                        Button(
                                            onClick = {
                                                val guidReferencia = java.util.UUID.randomUUID().toString()
                                                android.util.Log.d("MrComandaLog", "-> Iniciando cobro | Mesa: ${mesa.mesa} | Monto: $$totalCalculado | Ref (GUID): $guidReferencia")
                                                MercadoPagoPaymentManager.iniciarCobro(
                                                    launcher = cobroLauncher,
                                                    monto = totalCalculado,
                                                    externalReference = guidReferencia
                                                )
                                            },
                                            modifier = Modifier.weight(1f),
                                            enabled = !mesa.cerrando
                                        ) {
                                            Text(if (mesa.cerrando) "En Proceso" else "Cobrar", fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}