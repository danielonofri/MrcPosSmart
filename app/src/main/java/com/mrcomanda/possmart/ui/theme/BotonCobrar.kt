package com.mrcomanda.possmart.ui.theme

import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.mrcomanda.possmart.model.Mesa
import com.mrcomanda.possmart.network.MercadoPagoPaymentManager
import java.util.UUID

@Composable
fun BotonCobrar(
    mesa: Mesa,
    totalCalculado: Double,
    onAprobado: (paymentId: String) -> Unit,
    onRechazadoOMas: (motivo: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    android.util.Log.d(
        "MrComandaLog",
        "Componiendo BotonCobrar | Mesa: ${mesa.mesa} | cerrando=${mesa.cerrando} | total=$totalCalculado"
    )

    Button(
        onClick = {
            val guidReferencia = UUID.randomUUID().toString()
            android.util.Log.d(
                "MrComandaLog",
                "-> [BotonCobrar] Clic | Mesa: ${mesa.mesa} | Monto: $$totalCalculado | Ref: $guidReferencia"
            )

            MercadoPagoPaymentManager.iniciarCobro(
                monto = totalCalculado,
                externalReference = guidReferencia,
                onAprobado = onAprobado,
                onRechazadoOMas = { motivo ->
                    Toast.makeText(context, motivo, Toast.LENGTH_LONG).show()
                    onRechazadoOMas(motivo)
                }
            )
        },
        modifier = modifier,
        enabled = !mesa.cerrando
    ) {
        Text(if (mesa.cerrando) "En Proceso" else "Cobrar", fontSize = 13.sp)
    }
}