package com.p3.recibop3.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.p3.recibop3.R
import com.p3.recibop3.databinding.ActivityMainBinding
import com.p3.recibop3.ui.viewmodel.EmpresaViewModel
import com.p3.recibop3.utils.applyBounceAnimation
import com.p3.recibop3.utils.animatePopIn

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val empresaViewModel: EmpresaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Hide default title as requested
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Observe empresa
        empresaViewModel.empresaActiva.observe(this) { empresa ->
            if (empresa != null) {
                binding.tvEmpresaNombre.text = empresa.nombre
                binding.tvEmpresaNombre.visibility = android.view.View.VISIBLE
            } else {
                binding.tvEmpresaNombre.text = "Registrar Empresa"
            }
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Apply Bounce Animation to all cards
        binding.cardNuevoRecibo.applyBounceAnimation()
        binding.cardRecibosRealizados.applyBounceAnimation()
        binding.cardBuscarFecha.applyBounceAnimation()
        binding.cardReportes.applyBounceAnimation()

        // Existing Click Logic preserved
        binding.cardNuevoRecibo.setOnClickListener {
            startActivity(Intent(this, NuevoReciboActivity::class.java))
        }

        binding.cardRecibosRealizados.setOnClickListener {
            startActivity(Intent(this, RecibosListActivity::class.java))
        }

        binding.cardBuscarFecha.setOnClickListener {
            startActivity(Intent(this, BuscarFechaActivity::class.java))
        }

        binding.cardReportes.setOnClickListener {
            startActivity(Intent(this, ReportesActivity::class.java))
        }
    }

    private fun animateDashboardEntrance() {
        // Hide initially
        binding.cardNuevoRecibo.alpha = 0f
        binding.cardRecibosRealizados.alpha = 0f
        binding.cardBuscarFecha.alpha = 0f
        binding.cardReportes.alpha = 0f

        // Staggered Pop-In
        binding.cardNuevoRecibo.animatePopIn(delay = 100)
        binding.cardRecibosRealizados.animatePopIn(delay = 200)
        binding.cardBuscarFecha.animatePopIn(delay = 300)
        binding.cardReportes.animatePopIn(delay = 400)
    }

    override fun onStart() {
        super.onStart()
        // Trigger animation every time the activity starts/returns
        binding.gridLayout.post {
            animateDashboardEntrance()
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, EmpresaActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
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
        // Refresh empresa data
        empresaViewModel.empresaActiva.value?.let {
            binding.tvEmpresaNombre.text = it.nombre
        }
    }
}
