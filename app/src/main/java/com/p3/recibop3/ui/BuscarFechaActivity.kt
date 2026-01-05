package com.p3.recibop3.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.p3.recibop3.databinding.ActivityBuscarFechaBinding
import com.p3.recibop3.ui.adapter.RecibosAdapter
import com.p3.recibop3.ui.viewmodel.ReciboViewModel
import java.text.SimpleDateFormat
import java.util.*

class BuscarFechaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBuscarFechaBinding
    private val reciboViewModel: ReciboViewModel by viewModels()
    private lateinit var adapter: RecibosAdapter
    
    private var fechaDesde: Calendar = Calendar.getInstance()
    private var fechaHasta: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuscarFechaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        setupDatePickers()
        setupCalendarView()
        setupClickListeners()
    }

    private fun setupDatePickers() {
        binding.etFechaDesde.setOnClickListener {
            showDatePicker(fechaDesde) { calendar ->
                fechaDesde = calendar
                binding.etFechaDesde.setText(dateFormat.format(calendar.time))
            }
        }

        binding.etFechaHasta.setOnClickListener {
            showDatePicker(fechaHasta) { calendar ->
                fechaHasta = calendar
                binding.etFechaHasta.setText(dateFormat.format(calendar.time))
            }
        }
    }

    private var fechaSeleccionadaDia: Calendar = Calendar.getInstance()

    private fun setupCalendarView() {
        // Set calendar date change listener for Single Day Search only
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth, 0, 0, 0)
            selectedDate.set(Calendar.MILLISECOND, 0)
            fechaSeleccionadaDia = selectedDate
        }
    }

    private fun setupClickListeners() {
        binding.btnBuscarRango.setOnClickListener {
            mostrarDialogoFiltro(isRango = true)
        }

        binding.btnBuscarDia.setOnClickListener {
            mostrarDialogoFiltro(isRango = false)
        }
    }

    private fun mostrarDialogoFiltro(isRango: Boolean) {
        val opciones = arrayOf("Todo", "Recibo", "Proforma", "Nota de Venta", "Comanda")
        
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Seleccione Tipo de Documento")
            .setItems(opciones) { _, which ->
                val tipoSeleccionado = if (which == 0) null else {
                    when(which) {
                        1 -> "RECIBO"
                        2 -> "PROFORMA"
                        3 -> "NOTA_VENTA"
                        4 -> "COMANDA"
                        else -> null
                    }
                }
                
                if (isRango) {
                    buscarPorRango(tipoSeleccionado)
                } else {
                    buscarPorDia(tipoSeleccionado)
                }
            }
            .show()
    }

    private fun buscarPorRango(tipo: String?) {
        val desde = fechaDesde.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val hasta = fechaHasta.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        lanzarResultados(desde, hasta, tipo)
    }

    private fun buscarPorDia(tipo: String?) {
        val inicio = fechaSeleccionadaDia.clone() as Calendar
        inicio.set(Calendar.HOUR_OF_DAY, 0)
        inicio.set(Calendar.MINUTE, 0)
        inicio.set(Calendar.SECOND, 0)
        inicio.set(Calendar.MILLISECOND, 0)

        val fin = fechaSeleccionadaDia.clone() as Calendar
        fin.set(Calendar.HOUR_OF_DAY, 23)
        fin.set(Calendar.MINUTE, 59)
        fin.set(Calendar.SECOND, 59)
        fin.set(Calendar.MILLISECOND, 999)

        lanzarResultados(inicio.timeInMillis, fin.timeInMillis, tipo)
    }

    private fun lanzarResultados(desde: Long, hasta: Long, tipo: String?) {
        val intent = Intent(this, RecibosListActivity::class.java).apply {
            putExtra("EXTRA_FECHA_INICIO", desde)
            putExtra("EXTRA_FECHA_FIN", hasta)
            if (tipo != null) {
                putExtra("EXTRA_TIPO_DOC", tipo)
            }
        }
        startActivity(intent)
    }
    private fun showDatePicker(initialDate: Calendar, onDateSelected: (Calendar) -> Unit) {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, day, 0, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                onDateSelected(calendar)
            },
            initialDate.get(Calendar.YEAR),
            initialDate.get(Calendar.MONTH),
            initialDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
