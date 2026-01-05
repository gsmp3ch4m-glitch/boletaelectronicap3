package com.p3.recibop3.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.p3.recibop3.databinding.ActivityRecibosListBinding
import com.p3.recibop3.ui.adapter.RecibosAdapter
import com.p3.recibop3.ui.viewmodel.ReciboViewModel

class RecibosListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecibosListBinding
    private val reciboViewModel: ReciboViewModel by viewModels()
    private lateinit var adapter: RecibosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecibosListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        setupRecyclerView()
        observeRecibos()
    }

    private fun setupRecyclerView() {
        adapter = RecibosAdapter { recibo ->
            val intent = Intent(this, ReciboDetalleActivity::class.java)
            intent.putExtra("recibo_id", recibo.idRecibo)
            startActivity(intent)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@RecibosListActivity)
            adapter = this@RecibosListActivity.adapter
        }
    }

    private fun observeRecibos() {
        val fechaInicio = intent.getLongExtra("EXTRA_FECHA_INICIO", 0)
        val fechaFin = intent.getLongExtra("EXTRA_FECHA_FIN", 0)
        val tipoDoc = intent.getStringExtra("EXTRA_TIPO_DOC")

        if (fechaInicio != 0L && fechaFin != 0L) {
            // Modo Búsqueda
            val tipoTitulo = tipoDoc ?: "Todo"
            supportActionBar?.title = "Buscar: $tipoTitulo"
            
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val dateRange = "${sdf.format(fechaInicio)} - ${sdf.format(fechaFin)}"
            supportActionBar?.subtitle = dateRange

            if (tipoDoc != null) {
                // Filtrar por Fecha Y Tipo
                reciboViewModel.getRecibosPorFechaYTipo(fechaInicio, fechaFin, tipoDoc).observe(this) { recibos ->
                    updateList(recibos)
                }
            } else {
                // Filtrar solo por Fecha (Tipo = Todo)
                reciboViewModel.getRecibosPorFecha(fechaInicio, fechaFin).observe(this) { recibos ->
                    updateList(recibos)
                }
            }
        } else {
            // Modo Normal (Todos)
            reciboViewModel.allRecibos.observe(this) { recibos ->
                updateList(recibos)
            }
        }
    }

    private fun updateList(recibos: List<com.p3.recibop3.data.entity.ReciboEntity>) {
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
