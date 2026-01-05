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
import androidx.core.content.FileProvider
import com.p3.recibop3.R
import com.p3.recibop3.data.entity.EmpresaEntity
import com.p3.recibop3.databinding.ActivityEmpresaBinding
import com.p3.recibop3.ui.viewmodel.EmpresaViewModel
import com.p3.recibop3.utils.applyBounceAnimation
import java.io.File
import java.io.FileOutputStream

class EmpresaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmpresaBinding
    private val empresaViewModel: EmpresaViewModel by viewModels()
    private var firmaPath: String? = null
    private var logoPath: String? = null
    private var empresaActual: EmpresaEntity? = null
    private var tempPhotoUri: Uri? = null
    private var tempImageForEdit: Uri? = null

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
            val path = saveImageFromUri(it, "Firmas", "firma")
            if (path != null) {
                firmaPath = path
                displayFirma(path)
            }
        }
    }

    private val logoGaleriaLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            tempImageForEdit = it
            openImageEditor(it)
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            tempImageForEdit = tempPhotoUri
            openImageEditor(tempPhotoUri!!)
        }
    }

    private val editImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { editedUri ->
                val path = saveImageFromUri(editedUri, "Logos", "logo")
                if (path != null) {
                    logoPath = path
                    displayLogo(path)
                }
            }
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
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
        // Apply Bounce Animations
        binding.btnFirmarAhora.applyBounceAnimation()
        binding.btnCargarFirma.applyBounceAnimation()
        binding.btnTomarFoto.applyBounceAnimation()
        binding.btnSubirGaleria.applyBounceAnimation()
        binding.btnGuardar.applyBounceAnimation()
        
        binding.btnFirmarAhora.setOnClickListener {
            firmaLauncher.launch(Intent(this, FirmaActivity::class.java))
        }

        binding.btnCargarFirma.setOnClickListener {
            galeriaLauncher.launch("image/*")
        }

        binding.btnTomarFoto.setOnClickListener {
            checkCameraPermissionAndLaunch()
        }

        binding.btnSubirGaleria.setOnClickListener {
            logoGaleriaLauncher.launch("image/*")
        }

        binding.btnGuardar.setOnClickListener {
            guardarEmpresa()
        }
        
        setupFactoryReset()
    }

    private fun loadEmpresaData(empresa: EmpresaEntity) {
        binding.apply {
            etNombre.setText(empresa.nombre)
            etRuc.setText(empresa.ruc)
            etDireccion.setText(empresa.direccion)
            etTelefono.setText(empresa.telefono)
            etRedesSociales.setText(empresa.redesSociales ?: "")
            etPin.setText(empresa.password ?: "")
            switchAppLock.isChecked = empresa.isAppLockEnabled
            
            empresa.logoPath?.let {
                logoPath = it
                displayLogo(it)
            }
            
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
        val pin = binding.etPin.text.toString().trim()
        val isAppLock = binding.switchAppLock.isChecked

        // Solo nombre, dirección, teléfono y PIN son obligatorios
        if (nombre.isEmpty() || direccion.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "Por favor complete: Nombre, Dirección y Teléfono", Toast.LENGTH_SHORT).show()
            return
        }

        if (pin.length != 6) {
            Toast.makeText(this, "El PIN de seguridad debe tener 6 dígitos", Toast.LENGTH_SHORT).show()
            return
        }

        val empresa = EmpresaEntity(
            idEmpresa = empresaActual?.idEmpresa ?: 0,
            nombre = nombre,
            ruc = ruc.ifEmpty { "Sin RUC" },
            direccion = direccion,
            telefono = telefono,
            redesSociales = redes.ifEmpty { null },
            logoPath = logoPath,
            firmaPath = firmaPath,
            password = pin,
            isAppLockEnabled = isAppLock
        )

        empresaViewModel.insertOrUpdate(empresa) {
            Toast.makeText(this, R.string.empresa_guardada, Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    // Setup listener for Factory Reset Button
    private fun setupFactoryReset() {
        binding.btnFactoryReset.setOnClickListener {
            showFactoryResetConfirmation()
        }
    }

    private fun showFactoryResetConfirmation() {
        val input = com.google.android.material.textfield.TextInputEditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        input.hint = "Ingrese su PIN actual"
        
        // Add container to provide padding
        val container = android.widget.FrameLayout(this)
        val params = android.widget.FrameLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.leftMargin = 50
        params.rightMargin = 50
        input.layoutParams = params
        container.addView(input)

        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("RESTABLECER DE FÁBRICA")
            .setMessage("Esta acción borrará TODOS los datos (recibos, logos, configuraciones). Esta acción es irreversible.\n\nIngrese su PIN para confirmar:")
            .setView(container)
            .setPositiveButton("BORRAR TODO") { _, _ ->
                val enteredPin = input.text.toString()
                if (enteredPin == empresaActual?.password) {
                    performFactoryReset()
                } else {
                    Toast.makeText(this, "PIN Incorrecto", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun performFactoryReset() {
        empresaViewModel.factoryReset {
            Toast.makeText(this, "Aplicación Restablecida", Toast.LENGTH_LONG).show()
            // Restart App
            val intent = Intent(applicationContext, SplashActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
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

    private fun displayLogo(path: String) {
        val file = File(path)
        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(path)
            binding.ivLogoPreview.setImageBitmap(bitmap)
        }
    }

    private fun checkCameraPermissionAndLaunch() {
        when {
            checkSelfPermission(android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    private fun launchCamera() {
        try {
            val photoFile = File(filesDir, "Logos").apply { mkdirs() }
            val file = File(photoFile, "logo_${System.currentTimeMillis()}.jpg")
            tempPhotoUri = FileProvider.getUriForFile(
                this,
                "com.p3.recibop3.v2.fileprovider",
                file
            )
            cameraLauncher.launch(tempPhotoUri)
        } catch (e: Exception) {
            android.util.Log.e("EmpresaActivity", "Error launching camera: ${e.message}", e)
            Toast.makeText(this, "Error al abrir la cámara: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openImageEditor(imageUri: Uri) {
        val intent = Intent(this, EditImageActivity::class.java)
        intent.putExtra("image_uri", imageUri)
        editImageLauncher.launch(intent)
    }

    private fun saveImageFromUri(uri: Uri, folder: String, prefix: String): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val directory = File(filesDir, folder)
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val fileName = "${prefix}_${System.currentTimeMillis()}.png"
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
