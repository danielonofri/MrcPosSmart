package com.mrcomanda.possmart.network

import java.time.LocalDate

suspend fun obtenerAccessTokenDiario(apiService: ApiService): String {
    val hoy =
        LocalDate.now().toString()

    // Si ya tenemos el access_token de Mercado Pago guardado y es de hoy, lo devolvemos directamente
    if (AppPreferences.accessTokenMp.isNotBlank() && AppPreferences.fechaTokenMp == hoy) {
        return AppPreferences.accessTokenMp
    }

    // Si es un nuevo día o no está guardado, lo vamos a buscar
    val url = AppPreferences.urlTokenMp
    val tokenMrcHeader = AppPreferences.tokenMrc // El token estático configurado
    val integracion = AppPreferences.integracion
    val licencia = AppPreferences.licencia
    val cuit = AppPreferences.cuit

    try {
        val response = apiService.obtenerTokenMp(
            url = url,
            authorization = tokenMrcHeader, // O "Bearer $tokenMrcHeader" si el backend lo requiere con prefijo
            integracion = integracion,
            licencia = licencia,
            cuit = cuit
        )

        // Guardamos el access_token de Mercado Pago y la fecha de hoy
        AppPreferences.accessTokenMp = response.access_token
        AppPreferences.fechaTokenMp = hoy

        return response.access_token
    } catch (e: Exception) {
        // En caso de error, retornamos el token anterior como respaldo si existe
        return AppPreferences.accessTokenMp
    }
}