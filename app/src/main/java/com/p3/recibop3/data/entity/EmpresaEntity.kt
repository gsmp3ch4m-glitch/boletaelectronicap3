package com.p3.recibop3.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "empresa")
data class EmpresaEntity(
    @PrimaryKey(autoGenerate = true)
    val idEmpresa: Int = 0,
    val nombre: String,
    val ruc: String,
    val direccion: String,
    val telefono: String,
    val redesSociales: String? = null,
    val firmaPath: String? = null
)
