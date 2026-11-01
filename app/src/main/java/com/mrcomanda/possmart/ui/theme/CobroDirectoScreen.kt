package com.mrcomanda.possmart.ui.theme

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrcomanda.possmart.R
import com.mrcomanda.possmart.network.MercadoPagoPaymentManager

@Composable
fun CobroDirectoScreen() {
    val context = LocalContext.current
    var montoTexto by remember { mutableStateOf("") }
    var conceptoTexto by remember { mutableStateOf("") }

    val cobroDirectoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        MercadoPagoPaymentManager.procesarResultado(
            resultCode = result.resultCode,
            data = result.data,
            onAprobado = { paymentId ->
                android.util.Log.d("MrComandaLog", "-> [Cobro Directo] ¡Aprobado! ID: $paymentId")
                android.widget.Toast.makeText(context, "¡Cobro Aprobado! ID: $paymentId", android.widget.Toast.LENGTH_LONG).show()
                montoTexto = ""
                conceptoTexto = ""
            },
            onRechazadoOMas = { motivo ->
                android.util.Log.d("MrComandaLog", "-> [Cobro Directo] Rechazado/Cancelado: $motivo")
                android.widget.Toast.makeText(context, "Cobro Cancelado o Rechazado: $motivo", android.widget.Toast.LENGTH_LONG).show()
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Marca de agua central con el logo convertido (detrás de todo)
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

        // Contenido interactivo principal
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Cobro Libre con Point Smart",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = montoTexto,
                onValueChange = { montoTexto = it },
                label = { Text("Monto a cobrar ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = conceptoTexto,
                onValueChange = { conceptoTexto = it },
                label = { Text("Concepto / Descripción (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val montoDouble = montoTexto.replace(",", ".").toDoubleOrNull() ?: 0.0
                    if (montoDouble <= 0.0) {
                        android.widget.Toast.makeText(context, "Ingrese un monto válido", android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val guidReferencia = java.util.UUID.randomUUID().toString()
                    android.util.Log.d("MrComandaLog", "-> Iniciando Cobro Directo | Monto: $$montoDouble | Concepto: $conceptoTexto | Ref: $guidReferencia")

                    MercadoPagoPaymentManager.iniciarCobro(
                        launcher = cobroDirectoLauncher,
                        monto = montoDouble,
                        externalReference = guidReferencia
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Cobrar con Point", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}