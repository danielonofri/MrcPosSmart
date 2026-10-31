package com.mrcomanda.possmart.network

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher

object MercadoPagoPaymentManager {

    /**
     * Prepara y lanza la intención de pago hacia el Smart POS.
     * @param launcher El ActivityResultLauncher encargado de abrir la pasarela y recibir el callback.
     * @param monto El total a cobrar de la cuenta.
     * @param externalReference Identificador único de la transacción en tu sistema (ej: "CUENTA_6").
     */
    fun iniciarCobro(
        launcher: ActivityResultLauncher<Intent>,
        monto: Double,
        externalReference: String
    ) {
        try {
            // Intent estándar de integración para terminales Smart POS nativas
            val intent = Intent("com.mercadopago.pos.action.PAY").apply {
                putExtra("amount", monto)
                putExtra("external_reference", externalReference)
            }
            launcher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            // Acá podés manejar si la terminal no tiene instalado el servicio o lanza error
        }
    }

    /**
     * Procesa la respuesta devuelta por la terminal tras finalizar el flujo de pago.
     */
    fun procesarResultado(
        resultCode: Int,
        data: Intent?,
        onAprobado: (paymentId: String) -> Unit,     // <-- Con -> y nombre de parámetro
        onRechazadoOMas: (motivo: String) -> Unit  // <-- Con -> y nombre de parámetro
    ) {
        if (resultCode == Activity.RESULT_OK) {
            val paymentId = data?.getStringExtra("payment_id") ?: "SIN_ID"
            val status = data?.getStringExtra("status") ?: "approved"

            if (status.equals("approved", ignoreCase = true)) {
                onAprobado(paymentId)
            } else {
                onRechazadoOMas("Pago no aprobado. Estado: $status")
            }
        } else {
            onRechazadoOMas("Transacción cancelada por el usuario o la terminal.")
        }
    }
}