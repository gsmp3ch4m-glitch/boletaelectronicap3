package com.p3.recibop3.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.p3.recibop3.R
import com.p3.recibop3.databinding.ActivityMainBinding
import com.p3.recibop3.ui.viewmodel.EmpresaViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val empresaViewModel: EmpresaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Observe empresa
        empresaViewModel.empresaActiva.observe(this) { empresa ->
            if (empresa != null) {
                binding.tvEmpresaNombre.text = empresa.nombre
            } else {
                // No hay empresa, mostrar diálogo y redirigir
                showNoEmpresaDialog()
            }
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.cardNuevoRecibo.setOnClickListener {
            startActivity(Intent(this, NuevoReciboActivity::class.java))
        }

        binding.cardRecibosRealizados.setOnClickListener {
            startActivity(Intent(this, RecibosListActivity::class.java))
        }

        binding.cardBuscarFecha.setOnClickListener {
            startActivity(Intent(this, BuscarFechaActivity::class.java))
        }

        binding.cardAjustes.setOnClickListener {
            startActivity(Intent(this, EmpresaActivity::class.java))
        }
    }

    private fun showNoEmpresaDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.empresa_titulo)
            .setMessage(R.string.empresa_sin_datos)
            .setPositiveButton(R.string.aceptar) { _, _ ->
                startActivity(Intent(this, EmpresaActivity::class.java))
            }
            .setCancelable(false)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Refresh empresa data when returning to main
        empresaViewModel.empresaActiva.value?.let {
            binding.tvEmpresaNombre.text = it.nombre
        }
    }
}
