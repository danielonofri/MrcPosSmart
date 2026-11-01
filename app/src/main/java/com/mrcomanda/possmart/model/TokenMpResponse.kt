package com.mrcomanda.possmart.model

data class TokenMpResponse(
    val integracion: String,
    val idVendedor: String,
    val access_token: String,
    val token_type: String,
    val expires_in: Long,
    val scope: String,
    val user_id: Long,
    val refresh_token: String,
    val public_key: String,
    val live_mode: Boolean,
    val creation_time: String,
    val vigente: Boolean,
    val licencia: String,
    val razonsocial: String,
    val cuit: String
)