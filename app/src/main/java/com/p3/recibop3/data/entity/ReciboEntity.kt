package com.p3.recibop3.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recibo",
    foreignKeys = [
        ForeignKey(
            entity = EmpresaEntity::class,
            parentColumns = ["idEmpresa"],
            childColumns = ["idEmpresa"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("idEmpresa"), Index("numeroRecibo")]
)
data class ReciboEntity(
    @PrimaryKey(autoGenerate = true)
    val idRecibo: Int = 0,
    val numeroRecibo: String,
    val idEmpresa: Int,
    val fechaHoraEmision: Long,
    val fechaPersonalizada: String? = null,
    val clienteNombre: String,
    val clienteDocumento: String,
    val clienteTelefono: String,
    val clienteDireccion: String,
    val pagoTipo: String, // "TOTAL" o "PARCIAL"
    val montoTotal: Double,
    val montoPagado: Double,
    val saldoPendiente: Double,
    val estado: String = "EMITIDO", // "EMITIDO" o "ANULADO"
    val pdfPath: String? = null
)
