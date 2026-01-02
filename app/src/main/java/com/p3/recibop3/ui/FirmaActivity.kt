package com.p3.recibop3.ui

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.p3.recibop3.R
import com.p3.recibop3.databinding.ActivityFirmaBinding
import com.p3.recibop3.utils.PdfGenerator

class FirmaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFirmaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirmaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLimpiar.setOnClickListener {
            binding.signatureView.clear()
        }

        binding.btnGuardar.setOnClickListener {
            if (binding.signatureView.isEmpty()) {
                Toast.makeText(this, R.string.firma_vacia, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bitmap = binding.signatureView.getSignatureBitmap()
            if (bitmap != null) {
                val fileName = "firma_${System.currentTimeMillis()}.png"
                val file = PdfGenerator.saveBitmapToFile(this, bitmap, fileName)
                
                if (file != null) {
                    val resultIntent = Intent().apply {
                        putExtra("firma_path", file.absolutePath)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                } else {
                    Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
