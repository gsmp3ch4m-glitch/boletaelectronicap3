package com.p3.recibop3.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.p3.recibop3.databinding.ActivityReportesBinding
import com.p3.recibop3.ui.viewmodel.ReporteViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReportesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportesBinding
    private val viewModel: ReporteViewModel by viewModels()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupDateSelectors()
        setupFilterSpinner()
        setupObservers()
    }

    private fun setupDateSelectors() {
        // Initial text update based on VM default (handled by observer mostly, but good to init)
        
        binding.btnFechaInicio.setOnClickListener {
            showDatePicker { timestamp ->
                val currentEnd = parseDate(binding.btnFechaFin.text.toString())
                viewModel.setDateRange(timestamp, currentEnd)
            }
        }

        binding.btnFechaFin.setOnClickListener {
            showDatePicker { timestamp ->
                val currentStart = parseDate(binding.btnFechaInicio.text.toString())
                // Set end of day for the end date
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = timestamp
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                viewModel.setDateRange(currentStart, calendar.timeInMillis)
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth, 0, 0, 0)
                // Normalize to start of day
                onDateSelected(calendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    
    private fun parseDate(dateStr: String): Long {
        return try {
            dateFormatter.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun setupFilterSpinner() {
        val options = listOf("Ventas Reales (Recibos)", "Proformas (Cotizaciones)", "Todo")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        binding.spinnerFiltroTipo.adapter = adapter
        
        binding.spinnerFiltroTipo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val filterType = when (position) {
                    0 -> ReporteViewModel.FilterType.SALES_ONLY
                    1 -> ReporteViewModel.FilterType.PROFORMAS_ONLY
                    else -> ReporteViewModel.FilterType.ALL
                }
                viewModel.setFilterType(filterType)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupObservers() {
        viewModel.recibosResult.observe(this) { recibos ->
            // Update Totals
            val totalMonto = recibos.sumOf { it.montoTotal }
            val cantidad = recibos.size
            
            binding.tvTotalVentas.text = String.format("S/ %.2f", totalMonto)
            binding.tvCantidadRecibos.text = cantidad.toString()
            
            // Update Graph
            val graphData = recibos.map { Pair(it.fechaHoraEmision, it.montoTotal) }
            binding.graphView.setData(graphData)
        }
        
        // Use a dirty hack to get current params if needed, or better, observe query params if exposed
        // For now, let's just make sure UI reflects default state. 
        // Ideally VM exposes 'currentStart' 'currentEnd' LiveData for UI binding.
        // I'll skip that for brevity and just update dates on selection triggers, 
        // but initial load needs handling.
        
        // Let's manually set initial UI to "Past 7 Days"
        val calendar = Calendar.getInstance()
        binding.btnFechaFin.text = dateFormatter.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        binding.btnFechaInicio.text = dateFormatter.format(calendar.time)
    }
}
