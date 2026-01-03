package com.p3.recibop3.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.p3.recibop3.R
import com.p3.recibop3.data.entity.DetalleReciboEntity
import com.p3.recibop3.data.entity.ReciboEntity
import com.p3.recibop3.databinding.ActivityNuevoReciboBinding
import com.p3.recibop3.databinding.DialogAddItemBinding
import com.p3.recibop3.ui.adapter.DetallesAdapter
import com.p3.recibop3.ui.viewmodel.EmpresaViewModel
import com.p3.recibop3.ui.viewmodel.ReciboViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NuevoReciboActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNuevoReciboBinding
    private val reciboViewModel: ReciboViewModel by viewModels()
    private val empresaViewModel: EmpresaViewModel by viewModels()
    private lateinit var detallesAdapter: DetallesAdapter
    
    private var numeroRecibo = ""
    private var tipoDocumento = "RECIBO"
    private var fechaHora = Calendar.getInstance()
    private var fechaPersonalizada: String? = null
    private var empresaId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuevoReciboBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        setupRecyclerView()
        setupTipoDocumentoDropdown()
        setupClickListeners()
        loadInitialData()
    }

    private fun setupRecyclerView() {
        detallesAdapter = DetallesAdapter { detalle, position ->
            detallesAdapter.removeDetalle(position)
            calcularTotales()
        }

        binding.recyclerViewDetalles.apply {
            layoutManager = LinearLayoutManager(this@NuevoReciboActivity)
            adapter = detallesAdapter
        }
    }

    private fun setupTipoDocumentoDropdown() {
        val tiposDocumento = listOf(
            getString(R.string.tipo_recibo),
            getString(R.string.tipo_proforma),
            getString(R.string.tipo_nota_venta),
            getString(R.string.tipo_comanda)
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tiposDocumento)
        binding.actvTipoDocumento.setAdapter(adapter)

        binding.actvTipoDocumento.setOnItemClickListener { _, _, position, _ ->
            tipoDocumento = when (position) {
                0 -> "RECIBO"
                1 -> "PROFORMA"
                2 -> "NOTA_VENTA"
                3 -> "COMANDA"
                else -> "RECIBO"
            }
            // Regenerar número de recibo con el nuevo tipo
            lifecycleScope.launch {
                numeroRecibo = reciboViewModel.generarNumeroRecibo(tipoDocumento)
                binding.tvNumeroRecibo.text = numeroRecibo
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnAgregarItem.setOnClickListener {
            showAddItemDialog()
        }

        binding.btnCambiarFecha.setOnClickListener {
            showDateTimePicker()
        }

        binding.rgPagoTipo.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbPagoTotal -> {
                    val total = calcularMontoTotal()
                    binding.etMontoPagado.setText(String.format("%.2f", total))
                }
                R.id.rbPagoParcial -> {
                    binding.etMontoPagado.setText("")
                }
            }
        }

        binding.etMontoPagado.addTextChangedListener {
            calcularSaldo()
        }

        binding.btnGenerarPdf.setOnClickListener {
            guardarRecibo()
        }
    }

    private fun loadInitialData() {
        lifecycleScope.launch {
            // Generar número de recibo con tipo por defecto
            numeroRecibo = reciboViewModel.generarNumeroRecibo(tipoDocumento)
            binding.tvNumeroRecibo.text = numeroRecibo

            // Mostrar fecha actual
            updateFechaDisplay()

            // Obtener empresa
            val empresa = empresaViewModel.getEmpresaActivaSync()
            if (empresa == null) {
                Toast.makeText(this@NuevoReciboActivity, R.string.empresa_sin_datos, Toast.LENGTH_LONG).show()
                finish()
                return@launch
            }
            empresaId = empresa.idEmpresa
        }
    }

    private fun showAddItemDialog() {
        val dialogBinding = DialogAddItemBinding.inflate(LayoutInflater.from(this))
        
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .create()

        var totalItem = 0.0

        val calcularTotal = {
            val cantidad = dialogBinding.etCantidad.text.toString().toDoubleOrNull() ?: 0.0
            val precio = dialogBinding.etPrecioUnitario.text.toString().toDoubleOrNull() ?: 0.0
            totalItem = cantidad * precio
            dialogBinding.tvTotal.text = String.format("Total: S/ %.2f", totalItem)
        }

        dialogBinding.etCantidad.addTextChangedListener { calcularTotal() }
        dialogBinding.etPrecioUnitario.addTextChangedListener { calcularTotal() }

        dialogBinding.btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnAgregar.setOnClickListener {
            val descripcion = dialogBinding.etDescripcion.text.toString().trim()
            val cantidad = dialogBinding.etCantidad.text.toString().toDoubleOrNull()
            val precio = dialogBinding.etPrecioUnitario.text.toString().toDoubleOrNull()

            if (descripcion.isEmpty() || cantidad == null || precio == null) {
                Toast.makeText(this, R.string.error_campos_vacios, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val detalle = DetalleReciboEntity(
                idRecibo = 0,
                descripcionProducto = descripcion,
                cantidad = cantidad,
                precioUnitario = precio,
                precioTotalLinea = totalItem
            )

            detallesAdapter.addDetalle(detalle)
            calcularTotales()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        
        DatePickerDialog(this, { _, year, month, day ->
            TimePickerDialog(this, { _, hour, minute ->
                fechaHora.set(year, month, day, hour, minute)
                val sdf = SimpleDateFormat("dd/MM/yyyy - hh:mm a", Locale.getDefault())
                fechaPersonalizada = sdf.format(fechaHora.time)
                updateFechaDisplay()
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateFechaDisplay() {
        val sdf = SimpleDateFormat("dd/MM/yyyy - hh:mm a", Locale.getDefault())
        binding.tvFecha.text = fechaPersonalizada ?: sdf.format(fechaHora.time)
    }

    private fun calcularMontoTotal(): Double {
        return detallesAdapter.getDetalles().sumOf { it.precioTotalLinea }
    }

    private fun calcularTotales() {
        val total = calcularMontoTotal()
        binding.tvMontoTotal.text = String.format("Monto Total: S/ %.2f", total)
        
        if (binding.rbPagoTotal.isChecked) {
            binding.etMontoPagado.setText(String.format("%.2f", total))
        }
        
        calcularSaldo()
    }

    private fun calcularSaldo() {
        val total = calcularMontoTotal()
        val pagado = binding.etMontoPagado.text.toString().toDoubleOrNull() ?: 0.0
        val saldo = total - pagado
        binding.tvSaldoPendiente.text = String.format("Saldo Pendiente: S/ %.2f", saldo)
    }

    private fun guardarRecibo() {
        val clienteNombre = binding.etClienteNombre.text.toString().trim()
        val clienteDocumento = binding.etClienteDocumento.text.toString().trim()
        val clienteTelefono = binding.etClienteTelefono.text.toString().trim()
        val clienteDireccion = binding.etClienteDireccion.text.toString().trim()

        if (clienteNombre.isEmpty() || clienteDocumento.isEmpty() || 
            clienteTelefono.isEmpty() || clienteDireccion.isEmpty()) {
            Toast.makeText(this, R.string.error_campos_vacios, Toast.LENGTH_SHORT).show()
            return
        }

        if (detallesAdapter.getDetalles().isEmpty()) {
            Toast.makeText(this, R.string.error_sin_items, Toast.LENGTH_SHORT).show()
            return
        }

        val montoTotal = calcularMontoTotal()
        val montoPagado = binding.etMontoPagado.text.toString().toDoubleOrNull() ?: 0.0

        if (montoPagado > montoTotal) {
            Toast.makeText(this, R.string.error_monto_invalido, Toast.LENGTH_SHORT).show()
            return
        }

        val pagoTipo = if (binding.rbPagoTotal.isChecked) "TOTAL" else "PARCIAL"
        val saldoPendiente = montoTotal - montoPagado

        val recibo = ReciboEntity(
            numeroRecibo = numeroRecibo,
            tipoDocumento = tipoDocumento,
            idEmpresa = empresaId,
            fechaHoraEmision = fechaHora.timeInMillis,
            fechaPersonalizada = fechaPersonalizada,
            clienteNombre = clienteNombre,
            clienteDocumento = clienteDocumento,
            clienteTelefono = clienteTelefono,
            clienteDireccion = clienteDireccion,
            pagoTipo = pagoTipo,
            montoTotal = montoTotal,
            montoPagado = montoPagado,
            saldoPendiente = saldoPendiente
        )

        // Guardar recibo y generar PDF
        lifecycleScope.launch {
            val empresa = empresaViewModel.getEmpresaActivaSync()
            if (empresa == null) {
                Toast.makeText(this@NuevoReciboActivity, "Error: No se encontró la empresa", Toast.LENGTH_SHORT).show()
                return@launch
            }

            reciboViewModel.insertReciboConDetalles(recibo, detallesAdapter.getDetalles()) { reciboId ->
                lifecycleScope.launch {
                    // Generar PDF
                    val pdfFileName = "${numeroRecibo}_${System.currentTimeMillis()}.pdf"
                    val pdfFile = com.p3.recibop3.utils.PdfGenerator.generateReciboPdf(
                        this@NuevoReciboActivity,
                        recibo,
                        detallesAdapter.getDetalles(),
                        empresa,
                        pdfFileName
                    )

                    if (pdfFile != null) {
                        // Actualizar recibo con ruta del PDF
                        val reciboActualizado = reciboViewModel.getReciboById(reciboId.toInt())
                        reciboActualizado?.let {
                            val updated = it.copy(pdfPath = pdfFile.absolutePath)
                            reciboViewModel.updateRecibo(updated)
                        }

                        // Mostrar diálogo de éxito con opciones
                        mostrarDialogoExito(pdfFile)
                    } else {
                        Toast.makeText(this@NuevoReciboActivity, R.string.error_generar_pdf, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun mostrarDialogoExito(pdfFile: java.io.File) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.exito)
            .setMessage(R.string.pdf_generado)
            .setPositiveButton(R.string.ver_pdf) { _, _ ->
                verPdf(pdfFile)
            }
            .setNegativeButton(R.string.compartir_pdf) { _, _ ->
                compartirPdf(pdfFile)
            }
            .setNeutralButton(R.string.volver) { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun verPdf(file: java.io.File) {
        try {
            android.util.Log.d("NuevoRecibo", "Opening PDF in built-in viewer: ${file.absolutePath}")
            android.util.Log.d("NuevoRecibo", "File exists: ${file.exists()}, Size: ${file.length()} bytes")
            
            if (!file.exists()) {
                Toast.makeText(this, "El archivo PDF no existe", Toast.LENGTH_LONG).show()
                finish()
                return
            }
            
            val intent = android.content.Intent(this, PdfViewerActivity::class.java).apply {
                putExtra(PdfViewerActivity.EXTRA_PDF_PATH, file.absolutePath)
                putExtra(PdfViewerActivity.EXTRA_PDF_TITLE, "Recibo $numeroRecibo")
            }
            
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            android.util.Log.e("NuevoRecibo", "Error opening PDF viewer: ${e.message}", e)
            Toast.makeText(this, "Error al abrir PDF: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun compartirPdf(file: java.io.File) {
        try {
            android.util.Log.d("NuevoRecibo", "Attempting to share PDF: ${file.absolutePath}")
            android.util.Log.d("NuevoRecibo", "File exists: ${file.exists()}, Size: ${file.length()} bytes")
            
            if (!file.exists()) {
                Toast.makeText(this, "El archivo PDF no existe", Toast.LENGTH_LONG).show()
                finish()
                return
            }
            
            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
            
            android.util.Log.d("NuevoRecibo", "FileProvider URI for sharing: $uri")

            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                putExtra(android.content.Intent.EXTRA_SUBJECT, "Recibo $numeroRecibo")
                putExtra(android.content.Intent.EXTRA_TEXT, "Adjunto recibo de pago $numeroRecibo")
                flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            val chooserIntent = android.content.Intent.createChooser(shareIntent, "Compartir Recibo")
            startActivity(chooserIntent)
            finish()
        } catch (e: Exception) {
            android.util.Log.e("NuevoRecibo", "Error sharing PDF: ${e.message}", e)
            Toast.makeText(this, "Error al compartir PDF: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
