package com.p3.recibop3.ui

import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import com.p3.recibop3.R
import com.p3.recibop3.databinding.ActivityEditImageBinding
import java.io.File
import java.io.FileOutputStream

class EditImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditImageBinding
    private var originalBitmap: Bitmap? = null
    private var currentRotation = 0f
    private var brightness = 0f
    private var contrast = 1f
    private var cropShape = CropShape.RECTANGULAR
    private var sourceUri: Uri? = null

    enum class CropShape {
        RECTANGULAR, SQUARE, CIRCLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        sourceUri = intent.getParcelableExtra("image_uri")
        if (sourceUri == null) {
            Toast.makeText(this, "Error: No se recibió la imagen", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadImage()
        setupClickListeners()
    }

    private fun loadImage() {
        try {
            val inputStream = contentResolver.openInputStream(sourceUri!!)
            originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            updateImageView()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al cargar imagen: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupClickListeners() {
        // Shape selection
        binding.chipGroupShape.setOnCheckedStateChangeListener { _, checkedIds ->
            cropShape = when (checkedIds.firstOrNull()) {
                R.id.chipSquare -> CropShape.SQUARE
                R.id.chipCircle -> CropShape.CIRCLE
                else -> CropShape.RECTANGULAR
            }
            updateImageView()
        }

        // Brightness slider
        binding.sliderBrightness.addOnChangeListener { _, value, _ ->
            brightness = value
            updateImageView()
        }

        // Contrast slider
        binding.sliderContrast.addOnChangeListener { _, value, _ ->
            contrast = value
            updateImageView()
        }

        // Rotation buttons
        binding.btnRotateLeft.setOnClickListener {
            rotateImage(-90f)
        }

        binding.btnRotateRight.setOnClickListener {
            rotateImage(90f)
        }

        // Action buttons
        binding.btnCancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        binding.btnSave.setOnClickListener {
            saveEditedImage()
        }
    }

    private fun rotateImage(degrees: Float) {
        currentRotation += degrees
        currentRotation %= 360
        updateImageView()
    }

    private fun updateImageView() {
        originalBitmap?.let { bitmap ->
            var processedBitmap = bitmap

            // Apply rotation
            if (currentRotation != 0f) {
                val matrix = Matrix()
                matrix.postRotate(currentRotation)
                processedBitmap = Bitmap.createBitmap(
                    processedBitmap,
                    0,
                    0,
                    processedBitmap.width,
                    processedBitmap.height,
                    matrix,
                    true
                )
            }

            // Apply brightness and contrast
            if (brightness != 0f || contrast != 1f) {
                processedBitmap = adjustBrightnessContrast(processedBitmap, brightness, contrast)
            }

            // Apply shape mask for preview
            when (cropShape) {
                CropShape.RECTANGULAR -> {
                    processedBitmap = cropToRectangle(processedBitmap)
                }
                CropShape.SQUARE -> {
                    processedBitmap = cropToSquare(processedBitmap)
                }
                CropShape.CIRCLE -> {
                    processedBitmap = applyCircleMask(processedBitmap)
                }
            }

            binding.ivPreview.setImageBitmap(processedBitmap)
        }
    }

    private fun adjustBrightnessContrast(bitmap: Bitmap, brightness: Float, contrast: Float): Bitmap {
        val colorMatrix = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, brightness,
                0f, contrast, 0f, 0f, brightness,
                0f, 0f, contrast, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
            )
        )

        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val canvas = Canvas(result)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    private fun cropToRectangle(bitmap: Bitmap): Bitmap {
        // Crop to 4:2 aspect ratio (width:height = 2:1)
        val targetRatio = 2f // 4:2 = 2:1
        val currentRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

        return if (currentRatio > targetRatio) {
            // Image is too wide, crop width
            val newWidth = (bitmap.height * targetRatio).toInt()
            val x = (bitmap.width - newWidth) / 2
            Bitmap.createBitmap(bitmap, x, 0, newWidth, bitmap.height)
        } else {
            // Image is too tall, crop height
            val newHeight = (bitmap.width / targetRatio).toInt()
            val y = (bitmap.height - newHeight) / 2
            Bitmap.createBitmap(bitmap, 0, y, bitmap.width, newHeight)
        }
    }

    private fun cropToSquare(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2
        return Bitmap.createBitmap(bitmap, x, y, size, size)
    }

    private fun applyCircleMask(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = Color.BLACK

        val rect = Rect(0, 0, size, size)
        val rectF = RectF(rect)

        canvas.drawOval(rectF, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2
        val source = Bitmap.createBitmap(bitmap, x, y, size, size)
        canvas.drawBitmap(source, 0f, 0f, paint)

        return output
    }

    private fun saveEditedImage() {
        try {
            originalBitmap?.let { bitmap ->
                var finalBitmap = bitmap

                // Apply rotation
                if (currentRotation != 0f) {
                    val matrix = Matrix()
                    matrix.postRotate(currentRotation)
                    finalBitmap = Bitmap.createBitmap(
                        finalBitmap,
                        0,
                        0,
                        finalBitmap.width,
                        finalBitmap.height,
                        matrix,
                        true
                    )
                }

                // Apply brightness and contrast
                if (brightness != 0f || contrast != 1f) {
                    finalBitmap = adjustBrightnessContrast(finalBitmap, brightness, contrast)
                }

                // Apply shape
                finalBitmap = when (cropShape) {
                    CropShape.RECTANGULAR -> cropToRectangle(finalBitmap)
                    CropShape.SQUARE -> cropToSquare(finalBitmap)
                    CropShape.CIRCLE -> applyCircleMask(finalBitmap)
                }

                // Save to temp file
                val tempFile = File(cacheDir, "edited_logo_${System.currentTimeMillis()}.png")
                val outputStream = FileOutputStream(tempFile)
                finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()

                // Return result
                val resultIntent = Intent()
                resultIntent.data = Uri.fromFile(tempFile)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        originalBitmap?.recycle()
    }

    companion object {
        const val REQUEST_EDIT_IMAGE = 1001
    }
}
