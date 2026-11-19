package com.mrcomanda.possmart.network

import android.util.Log
import com.mercadolibre.android.point_integration_sdk.nativesdk.MPManager
import com.mercadolibre.android.point_integration_sdk.nativesdk.payment.data.PaymentFlowRequestData
import com.mercadolibre.android.point_integration_sdk.nativesdk.payment.data.PaymentTransactionMetadata

object MercadoPagoPaymentManager {

    private const val TAG = "MrComandaLog"

    fun iniciarCobro(
        monto: Double,
        externalReference: String,
        onAprobado: (paymentId: String) -> Unit,
        onRechazadoOMas: (motivo: String) -> Unit
    ) {
        try {
            val requestData = PaymentFlowRequestData(
                amount = monto,
                paymentTransactionMetadata = PaymentTransactionMetadata(
                    externalReference = externalReference
                )
            )

            MPManager.paymentFlow.launchPaymentFlow(requestData) { response ->
                Log.d(TAG, "<- Respuesta SDK: $response")

                val statusName = response.status?.toString()?.uppercase()
                val data = response.data
                val sinError = response.error == null && data?.paymentStatusError.isNullOrEmpty()
                val idPago = data?.paymentReference ?: externalReference

                if (statusName == "SUCCESS" && sinError) {
                    onAprobado(idPago)
                } else {
                    onRechazadoOMas("Estado: ${response.status} | Error: ${response.error} | SubError: ${data?.paymentStatusError}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al iniciar cobro con SDK", e)
            onRechazadoOMas("Error al iniciar cobro: ${e.localizedMessage ?: e.message}")
        }
    }
}