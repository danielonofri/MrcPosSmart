package com.mrcomanda.possmart.model

data class ItemFacturaDto(
    val idk: Long,
    val mesa: Int,
    val cantidad: Double,
    val codigo: String,
    val descrip: String,
    val precio: Double,
    val total: Double
)