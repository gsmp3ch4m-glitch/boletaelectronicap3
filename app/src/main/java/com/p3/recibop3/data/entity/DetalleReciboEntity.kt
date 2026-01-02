package com.p3.recibop3.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "detalle_recibo",
    foreignKeys = [
        ForeignKey(
            entity = ReciboEntity::class,
            parentColumns = ["idRecibo"],
            childColumns = ["idRecibo"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("idRecibo")]
)
data class DetalleReciboEntity(
    @PrimaryKey(autoGenerate = true)
    val idDetalle: Int = 0,
    val idRecibo: Int,
    val descripcionProducto: String,
    val cantidad: Double,
    val precioUnitario: Double,
    val precioTotalLinea: Double
)
