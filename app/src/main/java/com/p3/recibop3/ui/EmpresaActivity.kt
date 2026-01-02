package com.p3.recibop3.ui

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.p3.recibop3.R
import com.p3.recibop3.data.entity.EmpresaEntity
import com.p3.recibop3.databinding.ActivityEmpresaBinding
import com.p3.recibop3.ui.viewmodel.EmpresaViewModel
import java.io.File
import java.io.FileOutputStream

class EmpresaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmpresaBinding
    private val empresaViewModel: EmpresaViewModel by viewModels()
    private var firmaPath: String? = null
    private var empresaActual: EmpresaEntity? = null

    private val firmaLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val path = result.data?.getStringExtra("firma_path")
            if (path != null) {
                firmaPath = path
                displayFirma(path)
            }
        }
    }

    private val galeriaLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val path = saveImageFromUri(it)
            if (path != null) {
                firmaPath = path
                displayFirma(path)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmpresaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Observe empresa actual
        empresaViewModel.empresaActiva.observe(this) { empresa ->
            empresa?.let {
                empresaActual = it
                loadEmpresaData(it)
            }
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnFirmarAhora.setOnClickListener {
            firmaLauncher.launch(Intent(this, FirmaActivity::class.java))
        }

        binding.btnCargarFirma.setOnClickListener {
            galeriaLauncher.launch("image/*")
        }

        binding.btnGuardar.setOnClickListener {
            guardarEmpresa()
        }
    }

    private fun loadEmpresaData(empresa: EmpresaEntity) {
        binding.apply {
            etNombre.setText(empresa.nombre)
            etRuc.setText(empresa.ruc)
            etDireccion.setText(empresa.direccion)
            etTelefono.setText(empresa.telefono)
            etRedesSociales.setText(empresa.redesSociales ?: "")
            
            empresa.firmaPath?.let {
                firmaPath = it
                displayFirma(it)
            }
        }
    }

    private fun guardarEmpresa() {
        val nombre = binding.etNombre.text.toString().trim()
        val ruc = binding.etRuc.text.toString().trim()
        val direccion = binding.etDireccion.text.toString().trim()
        val telefono = binding.etTelefono.text.toString().trim()
        val redes = binding.etRedesSociales.text.toString().trim()

        if (nombre.isEmpty() || ruc.isEmpty() || direccion.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, R.string.error_campos_vacios, Toast.LENGTH_SHORT).show()
            return
        }

        val empresa = EmpresaEntity(
            idEmpresa = empresaActual?.idEmpresa ?: 0,
            nombre = nombre,
            ruc = ruc,
            direccion = direccion,
            telefono = telefono,
            redesSociales = redes.ifEmpty { null },
            firmaPath = firmaPath
        )

        empresaViewModel.insertOrUpdate(empresa) {
            Toast.makeText(this, R.string.empresa_guardada, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun displayFirma(path: String) {
        val file = File(path)
        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(path)
            binding.ivFirmaPreview.setImageBitmap(bitmap)
        }
    }

    private fun saveImageFromUri(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val directory = File(filesDir, "Firmas")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val fileName = "firma_${System.currentTimeMillis()}.png"
            val file = File(directory, fileName)
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
