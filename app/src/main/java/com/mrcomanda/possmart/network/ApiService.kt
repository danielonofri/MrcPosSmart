package com.mrcomanda.possmart.network

import com.mrcomanda.possmart.model.ItemFacturaDto
import com.mrcomanda.possmart.model.Mesa
import com.mrcomanda.possmart.model.RegistroPagoResponseDto
import com.mrcomanda.possmart.model.TokenMpResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface ApiService {
    @GET("api/mesas/abiertas")
    suspend fun getMesasAbiertas(
        @Header("X-API-Key") apiKey: String
    ): List<Mesa>

    @GET("api/mesas/{numeroMesa}/factura")
    suspend fun getFacturaMesa(
        @Path("numeroMesa") numeroMesa: Int,
        @Header("X-API-Key") apiKey: String
    ): List<ItemFacturaDto>

    @POST("api/mesas/{numeroMesa}/pagar")
    suspend fun postRegistrarPago(
        @Path("numeroMesa") numeroMesa: Int,
        @Header("X-API-Key") apiKey: String
    ): RegistroPagoResponseDto

    @GET
    suspend fun obtenerTokenMp(
        @Url url: String,
        @Header("Authorization") authorization: String, // Aquí se envía el token_mrc
        @Query("integracion") integracion: String,
        @Query("licencia") licencia: String,
        @Query("cuit") cuit: String
    ): TokenMpResponse
}