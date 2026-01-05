package com.p3.recibop3.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.p3.recibop3.data.entity.ReciboEntity

@Dao
interface ReciboDao {
    
    @Insert
    suspend fun insert(recibo: ReciboEntity): Long
    
    @Update
    suspend fun update(recibo: ReciboEntity)
    
    @Query("SELECT * FROM recibo ORDER BY fechaHoraEmision DESC, numeroRecibo DESC")
    fun getAllRecibos(): LiveData<List<ReciboEntity>>
    
    @Query("SELECT * FROM recibo WHERE idRecibo = :id")
    suspend fun getReciboById(id: Int): ReciboEntity?
    
    @Query("SELECT * FROM recibo WHERE idRecibo = :id")
    fun getReciboByIdLive(id: Int): LiveData<ReciboEntity?>
    
    @Query("SELECT * FROM recibo WHERE fechaHoraEmision BETWEEN :fechaInicio AND :fechaFin ORDER BY fechaHoraEmision DESC")
    fun getRecibosPorFecha(fechaInicio: Long, fechaFin: Long): LiveData<List<ReciboEntity>>
    
    @Query("UPDATE recibo SET estado = 'ANULADO' WHERE idRecibo = :id")
    suspend fun anularRecibo(id: Int)
    
    @Query("UPDATE recibo SET estado = 'EMITIDO' WHERE idRecibo = :id")
    suspend fun desanularRecibo(id: Int)

    @Query("SELECT MAX(CAST(SUBSTR(numeroRecibo, 5) AS INTEGER)) FROM recibo WHERE tipoDocumento = :tipo")
    suspend fun getUltimoNumeroPorTipo(tipo: String): Int?
    
    @Query("SELECT * FROM recibo WHERE tipoDocumento = :tipo ORDER BY fechaHoraEmision DESC, numeroRecibo DESC")
    fun getRecibosPorTipo(tipo: String): LiveData<List<ReciboEntity>>
    
    @Delete
    suspend fun delete(recibo: ReciboEntity)
}
