package com.mrcomanda.possmart.ui.theme

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrcomanda.possmart.R
import com.mrcomanda.possmart.network.MercadoPagoPaymentManager
import java.util.UUID

private data class EstadoResultadoCobro(
    val titulo: String,
    val detalle: String,
    val aprobado: Boolean,
    val paymentId: String? = null
)

@Composable
fun CobroDirectoScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val esquinaCinta = RoundedCornerShape(4.dp)

    var montoTexto by remember { mutableStateOf("") }
    var conceptoTexto by remember { mutableStateOf("") }
    var resultadoCobro by remember { mutableStateOf<EstadoResultadoCobro?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // Marca de agua central con el logo (detrás de todo)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo_mrc),
                contentDescription = "Marca de agua Logo",
                modifier = Modifier
                    .size(220.dp)
                    .alpha(0.06f)
            )
        }

        // Contenido dockeado arriba con scroll seguro cuando abre el teclado
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "COBRO LIBRE CON POINT SMART",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = montoTexto,
                onValueChange = { montoTexto = it },
                label = { Text("Monto a cobrar ($)") },
                shape = esquinaCinta,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = conceptoTexto,
                onValueChange = { conceptoTexto = it },
                label = { Text("Concepto / Descripción (Opcional)") },
                shape = esquinaCinta,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Cinta de acción superior con vértices apenas redondeados
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = esquinaCinta,
                color = Color(0xFFF3F4F6),
                border = BorderStroke(1.dp, Color(0xFFD0D5DD))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val montoDouble = montoTexto.replace(",", ".").toDoubleOrNull() ?: 0.0
                            if (montoDouble <= 0.0) {
                                Toast.makeText(context, "Ingrese un monto válido", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            resultadoCobro = null
                            val guidReferencia = UUID.randomUUID().toString()
                            Log.d("MrComandaLog", "-> Iniciando Cobro Directo | Monto: $$montoDouble | Concepto: $conceptoTexto | Ref: $guidReferencia")

                            MercadoPagoPaymentManager.iniciarCobro(
                                monto = montoDouble,
                                externalReference = guidReferencia,
                                onAprobado = { paymentId ->
                                    Log.d("MrComandaLog", "-> [Cobro Directo] ¡Aprobado! ID: $paymentId")
                                    val montoFormateado = String.format(java.util.Locale.getDefault(), "%.2f", montoDouble)
                                    // o si querés entero directo: montoDouble.toLong()
                                    resultadoCobro = EstadoResultadoCobro(
                                        titulo = "PAGO APROBADO $$montoFormateado",
                                        detalle = if (conceptoTexto.isNotBlank()) "Concepto: $conceptoTexto" else "Pago acreditado en Point Smart.",
                                        aprobado = true,
                                        paymentId = paymentId
                                    )
                                    montoTexto = ""
                                    conceptoTexto = ""
                                },
                                onRechazadoOMas = { motivo ->
                                    Log.d("MrComandaLog", "-> [Cobro Directo] Rechazado/Cancelado: $motivo")
                                    resultadoCobro = EstadoResultadoCobro(
                                        titulo = "COBRO NO COMPLETADO",
                                        detalle = motivo.ifBlank { "Operación cancelada o rechazada." },
                                        aprobado = false,
                                        paymentId = null
                                    )
                                }
                            )
                        },
                        shape = esquinaCinta,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text("Cobrar con Point", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            montoTexto = ""
                            conceptoTexto = ""
                            resultadoCobro = null
                        },
                        shape = esquinaCinta,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier.height(46.dp)
                    ) {
                        Text("Limpiar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Resultado de la cobranza
            resultadoCobro?.let { res ->
                val colorPrincipal = if (res.aprobado) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                val colorFondo = if (res.aprobado) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = esquinaCinta,
                    border = BorderStroke(1.5.dp, colorPrincipal),
                    colors = CardDefaults.cardColors(containerColor = colorFondo)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = res.titulo,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorPrincipal
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = res.detalle,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                        if (!res.paymentId.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ID Pago: ${res.paymentId}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { resultadoCobro = null },
                            shape = esquinaCinta,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Cerrar aviso", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}