package com.p3.recibop3.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.p3.recibop3.databinding.ActivitySplashBinding
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val splashDuration = 2000L // 2 segundos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ocultar action bar
        supportActionBar?.hide()

        // Prepare Initial State (Hidden)
        binding.cvLogoContainer.apply {
            alpha = 0f
            scaleX = 0f
            scaleY = 0f
        }
        binding.tvAppName.apply {
            alpha = 0f
            translationY = 100f
        }
        binding.tvSubtitle.alpha = 0f
        
        // Start Magic Animation Sequence
        startMagicReveal()
    }

    private fun startMagicReveal() {
        // Step 1: Pop Logo (Bounce effect)
        binding.cvLogoContainer.animate()
            .alpha(1f)
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(800)
            .setInterpolator(android.view.animation.OvershootInterpolator(2f)) // Big bounce
            .withEndAction {
                // Step 2: Slide Up Title
                binding.tvAppName.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setInterpolator(android.view.animation.DecelerateInterpolator())
                    .start()
                    
                binding.tvSubtitle.animate()
                    .alpha(1f)
                    .setStartDelay(200) // Slight delay after title
                    .setDuration(500)
                    .start()
            }
            .start()

        // Schedule Navigation (synced with animation end roughly)
        Handler(Looper.getMainLooper()).postDelayed({
            checkAppLock()
        }, 2500) // Total duration covers animation time
    }

    private fun checkAppLock() {
        // Run in background to access DB
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val db = com.p3.recibop3.data.AppDatabase.getDatabase(applicationContext)
            // Need to create a DAO method or access repo directly.
            // Since we might not have DI setup for simple splash, accessing DAO directly is easiest.
            val empresa = db.empresaDao().getEmpresaActivaSync() // Assuming this exists or we use LiveData in VM
            
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                if (empresa != null && empresa.isAppLockEnabled) {
                    val intent = Intent(this@SplashActivity, LockScreenActivity::class.java)
                    startActivity(intent)
                } else {
                    val intent = Intent(this@SplashActivity, MainActivity::class.java)
                    startActivity(intent)
                }
                finish()
            }
        }
    }
}
