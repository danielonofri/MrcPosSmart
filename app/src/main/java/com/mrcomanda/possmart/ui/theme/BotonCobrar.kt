package com.mrcomanda.possmart.ui.theme

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.mrcomanda.possmart.model.Mesa
import com.mrcomanda.possmart.network.MercadoPagoPaymentManager
import java.util.UUID

@Composable
fun BotonCobrar(
    mesa: Mesa,
    totalCalculado: Double,
    cobroLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    modifier: Modifier = Modifier
) {
    //val coroutineScope = rememberCoroutineScope()

    Button(
        onClick = {
            val guidReferencia = UUID.randomUUID().toString()
            android.util.Log.d("MrComandaLog", "-> [BotonCobrar] Clic detectado | Mesa: ${mesa.mesa} | Monto: $$totalCalculado | Ref (GUID): $guidReferencia")
            android.util.Log.d("MrComandaLog", "-> Iniciando cobro nativo con SDK | Mesa: ${mesa.mesa} | Monto: $$totalCalculado | Ref (GUID): $guidReferencia")

            // Flujo nativo directo con la terminal enlazada mediante el SDK
            MercadoPagoPaymentManager.iniciarCobro(
                launcher = cobroLauncher,
                monto = totalCalculado,
                externalReference = guidReferencia
            )
        },
        modifier = modifier,
        enabled = !mesa.cerrando
    ) {
        Text(if (mesa.cerrando) "En Proceso" else "Cobrar", fontSize = 13.sp)
    }
}