package com.p3.recibop3.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.p3.recibop3.data.AppDatabase
import com.p3.recibop3.data.entity.ReciboEntity
import com.p3.recibop3.data.repository.ReciboRepository
import java.util.Calendar

class ReporteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ReciboRepository = ReciboRepository(
        AppDatabase.getDatabase(application).reciboDao(),
        AppDatabase.getDatabase(application).detalleReciboDao()
    )
    
    // Filtros
    private val _fechaInicio = MutableLiveData<Long>()
    private val _fechaFin = MutableLiveData<Long>()
    private val _tipoFiltro = MutableLiveData<FilterType>(FilterType.ALL)

    // Result LiveData
    val recibosReporte: LiveData<List<ReciboEntity>> = _tipoFiltro.switchMap { filterType ->
        val start = _fechaInicio.value ?: 0L
        val end = _fechaFin.value ?: System.currentTimeMillis()
        
        when (filterType) {
            FilterType.ALL -> repository.getRecibosPorFecha(start, end)
            FilterType.SALES_ONLY -> repository.getRecibosByTypesInRange(
                listOf("RECIBO", "NOTA_VENTA", "COMANDA"), 
                start, 
                end
            )
            FilterType.PROFORMAS_ONLY -> repository.getRecibosPorFechaYTipo(start, end, "PROFORMA")
            else -> repository.getRecibosPorFecha(start, end)
        }
    }
    
    // Trigger update when dates change (internal hack as switchMap observer needs trigger)
    private val _trigger = MutableLiveData<Boolean>()
    val combinedTrigger = androidx.lifecycle.MediatorLiveData<Triple<Long, Long, FilterType>>().apply {
        addSource(_fechaInicio) { value = Triple(it, _fechaFin.value ?: 0L, _tipoFiltro.value ?: FilterType.ALL) }
        addSource(_fechaFin) { value = Triple(_fechaInicio.value ?: 0L, it, _tipoFiltro.value ?: FilterType.ALL) }
        addSource(_tipoFiltro) { value = Triple(_fechaInicio.value ?: 0L, _fechaFin.value ?: 0L, it) }
    }
    
    private val _queryParams = MutableLiveData<QueryParams>()
    
    val recibosResult: LiveData<List<ReciboEntity>> = _queryParams.switchMap { params ->
        when (params.filterType) {
            FilterType.ALL -> repository.getRecibosPorFecha(params.start, params.end)
            FilterType.SALES_ONLY -> repository.getRecibosByTypesInRange(
                listOf("RECIBO", "NOTA_VENTA", "COMANDA"), 
                params.start, 
                params.end
            )
            FilterType.PROFORMAS_ONLY -> repository.getRecibosPorFechaYTipo(params.start, params.end, "PROFORMA")
        }
    }

    init {
        // Default: Last 7 days
        val calendar = Calendar.getInstance()
        val end = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val start = calendar.timeInMillis
        
        _queryParams.value = QueryParams(start, end, FilterType.SALES_ONLY)
    }

    fun setDateRange(start: Long, end: Long) {
        val current = _queryParams.value ?: QueryParams(0, 0, FilterType.ALL)
        _queryParams.value = current.copy(start = start, end = end)
    }

    fun setFilterType(type: FilterType) {
        val current = _queryParams.value ?: QueryParams(0, 0, FilterType.ALL)
        _queryParams.value = current.copy(filterType = type)
    }
    
    enum class FilterType {
        ALL, SALES_ONLY, PROFORMAS_ONLY
    }
    
    data class QueryParams(val start: Long, val end: Long, val filterType: FilterType)
}
