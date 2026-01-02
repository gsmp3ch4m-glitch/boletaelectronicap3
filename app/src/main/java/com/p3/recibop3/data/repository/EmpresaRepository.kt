package com.p3.recibop3.data.repository

import androidx.lifecycle.LiveData
import com.p3.recibop3.data.dao.EmpresaDao
import com.p3.recibop3.data.entity.EmpresaEntity

class EmpresaRepository(private val empresaDao: EmpresaDao) {
    
    val empresaActiva: LiveData<EmpresaEntity?> = empresaDao.getEmpresaActiva()
    
    suspend fun insertOrUpdate(empresa: EmpresaEntity): Long {
        return empresaDao.insertOrUpdate(empresa)
    }
    
    suspend fun getEmpresaActivaSync(): EmpresaEntity? {
        return empresaDao.getEmpresaActivaSync()
    }
    
    suspend fun delete(empresa: EmpresaEntity) {
        empresaDao.delete(empresa)
    }
    
    suspend fun deleteAll() {
        empresaDao.deleteAll()
    }
}
