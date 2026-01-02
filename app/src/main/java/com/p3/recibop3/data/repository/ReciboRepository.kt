package com.p3.recibop3.data.repository

import androidx.lifecycle.LiveData
import com.p3.recibop3.data.dao.DetalleReciboDao
import com.p3.recibop3.data.dao.ReciboDao
import com.p3.recibop3.data.entity.DetalleReciboEntity
import com.p3.recibop3.data.entity.ReciboEntity

class ReciboRepository(
    private val reciboDao: ReciboDao,
    private val detalleReciboDao: DetalleReciboDao
) {
    
    val allRecibos: LiveData<List<ReciboEntity>> = reciboDao.getAllRecibos()
    
    suspend fun insertReciboConDetalles(recibo: ReciboEntity, detalles: List<DetalleReciboEntity>): Long {
        val reciboId = reciboDao.insert(recibo)
        val detallesConId = detalles.map { it.copy(idRecibo = reciboId.toInt()) }
        detalleReciboDao.insertAll(detallesConId)
        return reciboId
    }
    
    suspend fun updateRecibo(recibo: ReciboEntity) {
        reciboDao.update(recibo)
    }
    
    suspend fun getReciboById(id: Int): ReciboEntity? {
        return reciboDao.getReciboById(id)
    }
    
    fun getReciboByIdLive(id: Int): LiveData<ReciboEntity?> {
        return reciboDao.getReciboByIdLive(id)
    }
    
    fun getDetallesByRecibo(reciboId: Int): LiveData<List<DetalleReciboEntity>> {
        return detalleReciboDao.getDetallesByRecibo(reciboId)
    }
    
    suspend fun getDetallesByReciboSync(reciboId: Int): List<DetalleReciboEntity> {
        return detalleReciboDao.getDetallesByReciboSync(reciboId)
    }
    
    fun getRecibosPorFecha(fechaInicio: Long, fechaFin: Long): LiveData<List<ReciboEntity>> {
        return reciboDao.getRecibosPorFecha(fechaInicio, fechaFin)
    }
    
    suspend fun anularRecibo(id: Int) {
        reciboDao.anularRecibo(id)
    }
    
    suspend fun generarNumeroRecibo(): String {
        val ultimoNumero = reciboDao.getUltimoNumeroRecibo() ?: 0
        val nuevoNumero = ultimoNumero + 1
        return String.format("REC-%06d", nuevoNumero)
    }
    
    suspend fun deleteRecibo(recibo: ReciboEntity) {
        reciboDao.delete(recibo)
    }
}
