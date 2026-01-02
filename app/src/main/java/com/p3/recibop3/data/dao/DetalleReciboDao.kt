package com.p3.recibop3.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.p3.recibop3.data.entity.DetalleReciboEntity

@Dao
interface DetalleReciboDao {
    
    @Insert
    suspend fun insertAll(detalles: List<DetalleReciboEntity>)
    
    @Insert
    suspend fun insert(detalle: DetalleReciboEntity): Long
    
    @Query("SELECT * FROM detalle_recibo WHERE idRecibo = :reciboId ORDER BY idDetalle")
    fun getDetallesByRecibo(reciboId: Int): LiveData<List<DetalleReciboEntity>>
    
    @Query("SELECT * FROM detalle_recibo WHERE idRecibo = :reciboId ORDER BY idDetalle")
    suspend fun getDetallesByReciboSync(reciboId: Int): List<DetalleReciboEntity>
    
    @Delete
    suspend fun delete(detalle: DetalleReciboEntity)
    
    @Query("DELETE FROM detalle_recibo WHERE idRecibo = :reciboId")
    suspend fun deleteByRecibo(reciboId: Int)
}
