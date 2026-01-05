package com.p3.recibop3.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.p3.recibop3.data.AppDatabase
import com.p3.recibop3.data.entity.DetalleReciboEntity
import com.p3.recibop3.data.entity.ReciboEntity
import com.p3.recibop3.data.repository.ReciboRepository
import com.p3.recibop3.data.repository.EmpresaRepository
import kotlinx.coroutines.launch

class ReciboViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: ReciboRepository
    private val empresaRepository: EmpresaRepository
    val allRecibos: LiveData<List<ReciboEntity>>
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = ReciboRepository(database.reciboDao(), database.detalleReciboDao())
        empresaRepository = EmpresaRepository(database.empresaDao()) // Initialize EmpresaRepository
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
            // 1. Anular en BD
            repository.anularRecibo(id)
            
            // 2. Obtener datos actualizados para regenerar PDF con marca de agua
            val recibo = repository.getReciboById(id)
            val detalles = repository.getDetallesByReciboSync(id)
            val empresa = empresaRepository.getEmpresaActivaSync()
            
            if (recibo != null && empresa != null) {
                try {
                    // Regenerar PDF (esto sobreescribirá el archivo existente o creará uno nuevo)
                    // El nombre del archivo se puede derivar o crear uno nuevo
                    val fileName = "Recibo_${recibo.numeroRecibo}_ANULADO.pdf"
                    
                    // FORZAR estado ANULADO en el objeto para asegurar que la marca de agua salga
                    // independientemente de si la BD ya refrescó el cambio o no.
                    val reciboParaPdf = recibo.copy(estado = "ANULADO")

                    val pdfFile = com.p3.recibop3.utils.PdfGenerator.generateReciboPdf(
                        getApplication(),
                        reciboParaPdf, 
                        detalles,
                        empresa,
                        fileName
                    )
                    
                    if (pdfFile != null && pdfFile.exists()) {
                         // Actualizar ruta en BD si cambió el nombre
                         val updatedRecibo = recibo.copy(pdfPath = pdfFile.absolutePath)
                         repository.updateRecibo(updatedRecibo)
                    }
                } catch(e: Exception) {
                    e.printStackTrace()
                }
        }
    }
    }

    fun regenerarPdfAnulado(id: Int) {
        viewModelScope.launch {
            val recibo = repository.getReciboById(id)
            val detalles = repository.getDetallesByReciboSync(id)
            val empresa = empresaRepository.getEmpresaActivaSync()
            
            if (recibo != null && empresa != null) {
                try {
                    val fileName = "Recibo_${recibo.numeroRecibo}_ANULADO.pdf"
                    
                    val reciboParaPdf = recibo.copy(estado = "ANULADO")

                    val pdfFile = com.p3.recibop3.utils.PdfGenerator.generateReciboPdf(
                        getApplication(),
                        reciboParaPdf, 
                        detalles,
                        empresa,
                        fileName
                    )
                    
                    if (pdfFile != null && pdfFile.exists()) {
                         val updatedRecibo = recibo.copy(pdfPath = pdfFile.absolutePath)
                         repository.updateRecibo(updatedRecibo)
                    }
                } catch(e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    fun desanularRecibo(id: Int) {
        viewModelScope.launch {
            // 1. Restaurar en BD
            repository.desanularRecibo(id)
            
            // 2. Regenerar PDF para quitar la marca de agua
            val recibo = repository.getReciboById(id)
            val detalles = repository.getDetallesByReciboSync(id)
            val empresa = empresaRepository.getEmpresaActivaSync()
            
            if (recibo != null && empresa != null) {
                try {
                    val fileName = "Recibo_${recibo.numeroRecibo}.pdf" // Nombre normal sin _ANULADO
                    
                    // Asegurar estado EMITIDO para que NO salga la marca de agua
                    val reciboParaPdf = recibo.copy(estado = "EMITIDO")

                    val pdfFile = com.p3.recibop3.utils.PdfGenerator.generateReciboPdf(
                        getApplication(),
                        reciboParaPdf,
                        detalles,
                        empresa,
                        fileName
                    )
                    
                    if (pdfFile != null && pdfFile.exists()) {
                         val updatedRecibo = recibo.copy(pdfPath = pdfFile.absolutePath)
                         repository.updateRecibo(updatedRecibo)
                    }
                } catch(e: Exception) {
                    e.printStackTrace()
                }
            }
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
