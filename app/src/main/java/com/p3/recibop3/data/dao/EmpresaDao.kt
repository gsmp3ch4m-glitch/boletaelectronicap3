package com.p3.recibop3.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.p3.recibop3.data.entity.EmpresaEntity

@Dao
interface EmpresaDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(empresa: EmpresaEntity): Long
    
    @Query("SELECT * FROM empresa LIMIT 1")
    fun getEmpresaActiva(): LiveData<EmpresaEntity?>
    
    @Query("SELECT * FROM empresa LIMIT 1")
    suspend fun getEmpresaActivaSync(): EmpresaEntity?
    
    @Delete
    suspend fun delete(empresa: EmpresaEntity)
    
    @Query("DELETE FROM empresa")
    suspend fun deleteAll()
}
