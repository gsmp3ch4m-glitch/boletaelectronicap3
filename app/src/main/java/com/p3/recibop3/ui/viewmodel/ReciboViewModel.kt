package com.p3.recibop3.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.p3.recibop3.data.AppDatabase
import com.p3.recibop3.data.entity.DetalleReciboEntity
import com.p3.recibop3.data.entity.ReciboEntity
import com.p3.recibop3.data.repository.ReciboRepository
import kotlinx.coroutines.launch

class ReciboViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: ReciboRepository
    val allRecibos: LiveData<List<ReciboEntity>>
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = ReciboRepository(database.reciboDao(), database.detalleReciboDao())
        allRecibos = repository.allRecibos
    }
    
    fun insertReciboConDetalles(
        recibo: ReciboEntity,
        detalles: List<DetalleReciboEntity>,
        onComplete: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val id = repository.insertReciboConDetalles(recibo, detalles)
            onComplete(id)
        }
    }
    
    fun updateRecibo(recibo: ReciboEntity) {
        viewModelScope.launch {
            repository.updateRecibo(recibo)
        }
    }
    
    suspend fun getReciboById(id: Int): ReciboEntity? {
        return repository.getReciboById(id)
    }
    
    fun getReciboByIdLive(id: Int): LiveData<ReciboEntity?> {
        return repository.getReciboByIdLive(id)
    }
    
    fun getDetallesByRecibo(reciboId: Int): LiveData<List<DetalleReciboEntity>> {
        return repository.getDetallesByRecibo(reciboId)
    }
    
    suspend fun getDetallesByReciboSync(reciboId: Int): List<DetalleReciboEntity> {
        return repository.getDetallesByReciboSync(reciboId)
    }
    
    fun getRecibosPorFecha(fechaInicio: Long, fechaFin: Long): LiveData<List<ReciboEntity>> {
        return repository.getRecibosPorFecha(fechaInicio, fechaFin)
    }
    
    fun anularRecibo(id: Int) {
        viewModelScope.launch {
            repository.anularRecibo(id)
        }
    }
    
    suspend fun generarNumeroRecibo(tipoDocumento: String): String {
        return repository.generarNumeroRecibo(tipoDocumento)
    }
    
    fun getRecibosPorTipo(tipo: String): LiveData<List<ReciboEntity>> {
        return repository.getRecibosPorTipo(tipo)
    }
    
    fun deleteRecibo(recibo: ReciboEntity) {
        viewModelScope.launch {
            repository.deleteRecibo(recibo)
        }
    }
}
