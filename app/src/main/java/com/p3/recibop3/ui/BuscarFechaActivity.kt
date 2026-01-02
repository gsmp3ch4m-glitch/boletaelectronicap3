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

        setupRecyclerView()
        setupDatePickers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = RecibosAdapter { recibo ->
            val intent = Intent(this, ReciboDetalleActivity::class.java)
            intent.putExtra("recibo_id", recibo.idRecibo)
            startActivity(intent)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@BuscarFechaActivity)
            adapter = this@BuscarFechaActivity.adapter
        }
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

    private fun setupClickListeners() {
        binding.btnBuscar.setOnClickListener {
            buscarRecibos()
        }
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

    private fun buscarRecibos() {
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

        reciboViewModel.getRecibosPorFecha(desde, hasta).observe(this) { recibos ->
            if (recibos.isEmpty()) {
                binding.recyclerView.visibility = View.GONE
                binding.tvEmpty.visibility = View.VISIBLE
            } else {
                binding.recyclerView.visibility = View.VISIBLE
                binding.tvEmpty.visibility = View.GONE
                adapter.submitList(recibos)
            }
        }
    }
}
