package com.mrcomanda.possmart.network

import com.mrcomanda.possmart.model.ItemFacturaDto
import com.mrcomanda.possmart.model.Mesa
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("api/mesas/abiertas")
    suspend fun getMesasAbiertas(): List<Mesa>

    @GET("api/mesas/{numeroMesa}/factura")
    suspend fun getFacturaMesa(
        @Path("numeroMesa") numeroMesa: Int
    ): List<ItemFacturaDto>
}