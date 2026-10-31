package com.mrcomanda.possmart.ui.theme

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrcomanda.possmart.PrinterHelper
import com.mrcomanda.possmart.model.ItemFacturaDto
import com.mrcomanda.possmart.model.Mesa
import com.mrcomanda.possmart.network.MercadoPagoPaymentManager
import com.mrcomanda.possmart.network.RetrofitClient
import androidx.activity.result.contract.ActivityResultContracts

@Composable
fun MesasScreen() {
    var mesaSeleccionada by remember { mutableStateOf<Mesa?>(null) }
    var listaMesas by remember { mutableStateOf<List<Mesa>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            isLoading = true
            listaMesas = RetrofitClient.apiService.getMesasAbiertas()
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

    // Sin Scaffold ni TopAppBar aquí adentro para evitar barras duplicadas
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listaMesas) { mesa ->
                        Card(
                            modifier = Modifier
                                .height(90.dp)
                                .clickable { mesaSeleccionada = mesa },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFE0B2)
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(8.dp),
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
                                    text = "\$${mesa.total.toInt()}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFFE65100)
                                )
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

    val cobroLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        MercadoPagoPaymentManager.procesarResultado(
            resultCode = result.resultCode,
            data = result.data,
            onAprobado = { paymentId ->
                // Si el pago es exitoso, imprimimos el ticket
                android.widget.Toast.makeText(context, "¡Pago Aprobado! ID: $paymentId", android.widget.Toast.LENGTH_LONG).show()
                PrinterHelper.imprimirPrecuenta(
                    context = context,
                    items = itemsFactura,
                    total = mesa.total,
                    numeroMesa = mesa.mesa
                )
            },
            onRechazadoOMas = { motivo ->
                // Manejar rechazo o cancelación si hace falta
                android.widget.Toast.makeText(context, "Pago Rechazado o Cancelado: $motivo", android.widget.Toast.LENGTH_LONG).show()
            }
        )
    }

    LaunchedEffect(mesa.mesa) {
        try {
            isLoading = true
            itemsFactura = RetrofitClient.apiService.getFacturaMesa(mesa.mesa)
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
                    // Usamos una única LazyColumn para todo el contenido y los botones al final
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Ítems pedidos:", fontWeight = FontWeight.Bold, color = Color.Gray)
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
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
                                        text = "\$${item.precio}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        // Sección de Total y Botones al final del scroll (Nunca más se pierden)
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
                                            text = "\$$totalCalculado",
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
                                                // Cambiar a true cuando corras la app dentro del Smart POS físico
                                                val estoyEnSmartPos = false

                                                PrinterHelper.imprimirPrecuenta(
                                                    context = context, // Asegurate de pasar el context si lo necesitás (o pasarlo desde el Composable con LocalContext.current)
                                                    items = itemsFactura,
                                                    total = totalCalculado,
                                                    numeroMesa = mesa.mesa
                                                )
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Precuenta", fontSize = 13.sp)
                                        }

                                        Button(
                                            onClick = {
                                                val guidReferencia = java.util.UUID.randomUUID().toString()
                                                MercadoPagoPaymentManager.iniciarCobro(
                                                launcher = cobroLauncher,
                                                monto = totalCalculado,
                                                externalReference = guidReferencia
                                            ) },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Cobrar", fontSize = 13.sp)
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