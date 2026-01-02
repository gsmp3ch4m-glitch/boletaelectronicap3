package com.p3.recibop3.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.p3.recibop3.R
import com.p3.recibop3.databinding.ActivityReciboDetalleBinding
import com.p3.recibop3.ui.adapter.DetallesAdapter
import com.p3.recibop3.ui.viewmodel.ReciboViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ReciboDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReciboDetalleBinding
    private val reciboViewModel: ReciboViewModel by viewModels()
    private lateinit var detallesAdapter: DetallesAdapter
    private var reciboId: Int = 0
    private var pdfPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReciboDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        reciboId = intent.getIntExtra("recibo_id", 0)
        if (reciboId == 0) {
            finish()
            return
        }

        setupRecyclerView()
        loadReciboData()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        detallesAdapter = DetallesAdapter { _, _ -> }
        binding.recyclerViewDetalles.apply {
            layoutManager = LinearLayoutManager(this@ReciboDetalleActivity)
            adapter = detallesAdapter
        }
    }

    private fun loadReciboData() {
        reciboViewModel.getReciboByIdLive(reciboId).observe(this) { recibo ->
            recibo?.let {
                binding.apply {
                    tvNumeroRecibo.text = it.numeroRecibo
                    
                    val fecha = if (it.fechaPersonalizada != null) {
                        it.fechaPersonalizada
                    } else {
                        val sdf = SimpleDateFormat("dd/MM/yyyy - hh:mm a", Locale.getDefault())
                        sdf.format(Date(it.fechaHoraEmision))
                    }
                    tvFecha.text = fecha
                    
                    chipEstado.text = it.estado
                    if (it.estado == "EMITIDO") {
                        chipEstado.setChipBackgroundColorResource(R.color.estado_emitido)
                        btnAnular.isEnabled = true
                    } else {
                        chipEstado.setChipBackgroundColorResource(R.color.estado_anulado)
                        btnAnular.isEnabled = false
                    }
                    
                    tvClienteNombre.text = "Nombre: ${it.clienteNombre}"
                    tvClienteDocumento.text = "DNI/RUC: ${it.clienteDocumento}"
                    tvClienteTelefono.text = "Teléfono: ${it.clienteTelefono}"
                    tvClienteDireccion.text = "Dirección: ${it.clienteDireccion}"
                    
                    tvMontoTotal.text = String.format("Monto Total: S/ %.2f", it.montoTotal)
                    tvPagoTipo.text = "Tipo de Pago: ${it.pagoTipo}"
                    tvMontoPagado.text = String.format("Monto Pagado: S/ %.2f", it.montoPagado)
                    tvSaldoPendiente.text = String.format("Saldo Pendiente: S/ %.2f", it.saldoPendiente)
                    
                    pdfPath = it.pdfPath
                }
            }
        }

        reciboViewModel.getDetallesByRecibo(reciboId).observe(this) { detalles ->
            detallesAdapter.setDetalles(detalles)
        }
    }

    private fun setupClickListeners() {
        binding.btnVerPdf.setOnClickListener {
            verPdf()
        }

        binding.btnCompartirPdf.setOnClickListener {
            compartirPdf()
        }

        binding.btnAnular.setOnClickListener {
            confirmarAnulacion()
        }
    }

    private fun verPdf() {
        if (pdfPath == null) {
            Toast.makeText(this, "PDF no disponible", Toast.LENGTH_SHORT).show()
            return
        }

        val file = File(pdfPath!!)
        if (!file.exists()) {
            Toast.makeText(this, R.string.error_abrir_pdf, Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val intent = Intent(this, PdfViewerActivity::class.java).apply {
                putExtra(PdfViewerActivity.EXTRA_PDF_PATH, file.absolutePath)
                putExtra(PdfViewerActivity.EXTRA_PDF_TITLE, binding.tvNumeroRecibo.text.toString())
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, R.string.error_abrir_pdf, Toast.LENGTH_SHORT).show()
        }
    }

    private fun compartirPdf() {
        if (pdfPath == null) {
            Toast.makeText(this, "PDF no disponible", Toast.LENGTH_SHORT).show()
            return
        }

        val file = File(pdfPath!!)
        if (!file.exists()) {
            Toast.makeText(this, R.string.error_abrir_pdf, Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            startActivity(Intent.createChooser(intent, "Compartir Recibo"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error al compartir PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmarAnulacion() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.anular_recibo)
            .setMessage(R.string.confirmar_anular)
            .setPositiveButton(R.string.si) { _, _ ->
                reciboViewModel.anularRecibo(reciboId)
                Toast.makeText(this, R.string.recibo_anulado, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
}
