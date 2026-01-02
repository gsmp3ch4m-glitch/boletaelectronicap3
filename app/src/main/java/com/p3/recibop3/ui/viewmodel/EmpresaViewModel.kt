package com.p3.recibop3.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.p3.recibop3.data.AppDatabase
import com.p3.recibop3.data.entity.EmpresaEntity
import com.p3.recibop3.data.repository.EmpresaRepository
import kotlinx.coroutines.launch

class EmpresaViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: EmpresaRepository
    val empresaActiva: LiveData<EmpresaEntity?>
    
    init {
        val empresaDao = AppDatabase.getDatabase(application).empresaDao()
        repository = EmpresaRepository(empresaDao)
        empresaActiva = repository.empresaActiva
    }
    
    fun insertOrUpdate(empresa: EmpresaEntity, onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.insertOrUpdate(empresa)
            onComplete(id)
        }
    }
    
    suspend fun getEmpresaActivaSync(): EmpresaEntity? {
        return repository.getEmpresaActivaSync()
    }
    
    fun delete(empresa: EmpresaEntity) {
        viewModelScope.launch {
            repository.delete(empresa)
        }
    }
    
    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }
}
