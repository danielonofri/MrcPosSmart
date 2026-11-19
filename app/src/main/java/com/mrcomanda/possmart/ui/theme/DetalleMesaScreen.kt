package com.mrcomanda.possmart.ui.theme

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrcomanda.possmart.PrinterHelper
import com.mrcomanda.possmart.model.ItemFacturaDto
import com.mrcomanda.possmart.model.Mesa
import com.mrcomanda.possmart.network.AppPreferences
import com.mrcomanda.possmart.network.RetrofitClient
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import com.mrcomanda.possmart.R
private data class DialogoCobroEstado(
    val titulo: String,
    val mensaje: String,
    val paymentIdAprobado: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleMesaScreen(mesa: Mesa, onVolver: () -> Unit) {
    val context = LocalContext.current
    var itemsFactura by remember { mutableStateOf<List<ItemFacturaDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }
    var dialogoCobro by remember { mutableStateOf<DialogoCobroEstado?>(null) }
    var enviandoPagoApi by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

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
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        OutlinedButton(
                                            onClick = {
                                                android.util.Log.d("MrComandaLog", "-> [Pre-cuenta BT] Clic en mesa ${mesa.mesa}")
                                                PrinterHelper.imprimirPrecuenta(
                                                    context = context,
                                                    items = itemsFactura,
                                                    total = totalCalculado,
                                                    numeroMesa = mesa.mesa
                                                )
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_precuenta_bt),
                                                contentDescription = "Pre-cuenta Bluetooth",
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                android.util.Log.d("MrComandaLog", "-> [Pre-cuenta Integrada] Clic en mesa ${mesa.mesa}")
                                                PrinterHelper.imprimirPrecuentaPrinterIntegrada(
                                                    context = context,
                                                    items = itemsFactura,
                                                    total = totalCalculado,
                                                    numeroMesa = mesa.mesa,
                                                    leyendaExtra = if (mesa.cerrando) "ESTADO: COBRADA" else null
                                                )
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_precuenta_pos),
                                                contentDescription = "Pre-cuenta Integrada",
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        // 3. Cobrar / Cobrada
                                        if (mesa.cerrando) {
                                            OutlinedButton(
                                                onClick = {},
                                                enabled = false,
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                                modifier = Modifier.weight(1.3f)
                                            ) {
                                                Text("Cobrada", fontSize = 13.sp, color = Color(0xFF2E7D32))
                                            }
                                        } else {
                                            BotonCobrar(
                                                mesa = mesa,
                                                totalCalculado = totalCalculado,
                                                onAprobado = { paymentId ->
                                                    android.util.Log.d("MrComandaLog", "-> Pago Aprobado con ID: $paymentId (esperando OK)")
                                                    dialogoCobro = DialogoCobroEstado(
                                                        titulo = "Pago aprobado",
                                                        mensaje = "Cobro aprobado (ID: $paymentId).\nPresione OK para registrar el pago en el servidor y cerrar la mesa.",
                                                        paymentIdAprobado = paymentId
                                                    )
                                                },
                                                onRechazadoOMas = { motivo ->
                                                    android.util.Log.d("MrComandaLog", "-> Pago Rechazado / Cancelado / Error. Motivo: $motivo")
                                                    dialogoCobro = DialogoCobroEstado(
                                                        titulo = "Error en el pago",
                                                        mensaje = motivo,
                                                        paymentIdAprobado = null
                                                    )
                                                },
                                                modifier = Modifier.weight(1.3f)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }

            val dialogo = dialogoCobro
            if (dialogo != null) {
                AlertDialog(
                    onDismissRequest = {
                        if (dialogo.paymentIdAprobado == null) {
                            dialogoCobro = null
                        }
                    },
                    title = { Text(dialogo.titulo, fontWeight = FontWeight.Bold) },
                    text = { Text(dialogo.mensaje) },
                    confirmButton = {
                        Button(
                            onClick = {
                                val idPago = dialogo.paymentIdAprobado
                                dialogoCobro = null

                                if (idPago != null) {
                                    coroutineScope.launch {
                                        enviandoPagoApi = true
                                        try {
                                            RetrofitClient.apiService.postRegistrarPago(
                                                numeroMesa = mesa.mesa,
                                                apiKey = AppPreferences.apiKey
                                            )
                                            PrinterHelper.imprimirPrecuenta(
                                                context = context,
                                                items = itemsFactura,
                                                total = totalCalculado,
                                                numeroMesa = mesa.mesa
                                            )
                                            enviandoPagoApi = false
                                            onVolver()
                                        } catch (e: Exception) {
                                            enviandoPagoApi = false
                                            dialogoCobro = DialogoCobroEstado(
                                                titulo = "Error al registrar en API",
                                                mensaje = "Pago aprobado (ID: $idPago), pero falló el envío al servidor:\n${e.localizedMessage ?: e.message}",
                                                paymentIdAprobado = null
                                            )
                                        }
                                    }
                                }
                            }
                        ) {
                            Text("OK")
                        }
                    }
                )
            }

            if (enviandoPagoApi) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Registrando pago en servidor...")
                        }
                    }
                }
            }
        }
    }
}