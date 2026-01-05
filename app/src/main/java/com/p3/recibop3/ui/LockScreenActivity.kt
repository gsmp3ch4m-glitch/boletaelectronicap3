package com.p3.recibop3.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.p3.recibop3.databinding.ActivityLockScreenBinding
import com.p3.recibop3.ui.viewmodel.EmpresaViewModel

class LockScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockScreenBinding
    private val empresaViewModel: EmpresaViewModel by viewModels()
    private var correctPin: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Observe stored PIN
        empresaViewModel.empresaActiva.observe(this) { empresa ->
            if (empresa != null) {
                correctPin = empresa.password
            } else {
                // If no company/password exists, no need to lock.
                proceedToMain()
            }
        }

        binding.btnUnlock.setOnClickListener {
            validatePin()
        }
    }

    private fun validatePin() {
        val inputPin = binding.etPin.text.toString()
        
        if (correctPin.isNullOrEmpty()) {
            // Should not happen if LockScreen is launched correctly, but safe fallback
            proceedToMain()
            return
        }

        if (inputPin == correctPin) {
            proceedToMain()
        } else {
            binding.tilPin.error = "PIN Incorrecto"
            binding.etPin.text?.clear()
        }
    }

    private fun proceedToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
