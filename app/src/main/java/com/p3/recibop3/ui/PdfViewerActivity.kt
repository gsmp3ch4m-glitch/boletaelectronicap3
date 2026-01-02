package com.p3.recibop3.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.p3.recibop3.databinding.ActivityPdfViewerBinding
import java.io.File

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfViewerBinding
    private var pdfRenderer: PdfRenderer? = null
    private var currentPage: PdfRenderer.Page? = null
    private var pdfFile: File? = null
    private var currentPageIndex = 0

    companion object {
        const val EXTRA_PDF_PATH = "pdf_path"
        const val EXTRA_PDF_TITLE = "pdf_title"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pdfPath = intent.getStringExtra(EXTRA_PDF_PATH)
        val pdfTitle = intent.getStringExtra(EXTRA_PDF_TITLE) ?: "Recibo PDF"

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = pdfTitle

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        if (pdfPath == null) {
            Toast.makeText(this, "Error: No se especificó el archivo PDF", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        pdfFile = File(pdfPath)
        if (!pdfFile!!.exists()) {
            Toast.makeText(this, "Error: El archivo PDF no existe", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        openPdfRenderer()
        showPage(0)

        binding.fabCompartir.setOnClickListener {
            compartirPdf()
        }

        // Gestos para navegar páginas (si hay múltiples)
        binding.pdfImageView.setOnClickListener {
            if (pdfRenderer != null && pdfRenderer!!.pageCount > 1) {
                currentPageIndex = (currentPageIndex + 1) % pdfRenderer!!.pageCount
                showPage(currentPageIndex)
            }
        }
    }

    private fun openPdfRenderer() {
        try {
            val fileDescriptor = ParcelFileDescriptor.open(
                pdfFile,
                ParcelFileDescriptor.MODE_READ_ONLY
            )
            pdfRenderer = PdfRenderer(fileDescriptor)
            android.util.Log.d("PdfViewer", "PDF opened: ${pdfRenderer?.pageCount} pages")
        } catch (e: Exception) {
            android.util.Log.e("PdfViewer", "Error opening PDF: ${e.message}", e)
            Toast.makeText(this, "Error al abrir el PDF: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun showPage(index: Int) {
        pdfRenderer?.let { renderer ->
            if (index < 0 || index >= renderer.pageCount) return

            currentPage?.close()
            currentPage = renderer.openPage(index)

            currentPage?.let { page ->
                // Crear bitmap con el tamaño de la página
                val bitmap = Bitmap.createBitmap(
                    page.width * 2,  // Multiplicar por 2 para mejor calidad
                    page.height * 2,
                    Bitmap.Config.ARGB_8888
                )

                // Renderizar la página en el bitmap
                page.render(
                    bitmap,
                    null,
                    null,
                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                )

                // Mostrar el bitmap
                binding.pdfImageView.setImageBitmap(bitmap)
                binding.tvPageNumber.text = "Página ${index + 1} de ${renderer.pageCount}"

                android.util.Log.d("PdfViewer", "Showing page ${index + 1} of ${renderer.pageCount}")
            }
        }
    }

    private fun compartirPdf() {
        pdfFile?.let { file ->
            try {
                val uri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, supportActionBar?.title ?: "Recibo")
                    putExtra(Intent.EXTRA_TEXT, "Adjunto recibo de pago")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }

                val chooserIntent = Intent.createChooser(shareIntent, "Compartir Recibo")
                startActivity(chooserIntent)
            } catch (e: Exception) {
                android.util.Log.e("PdfViewer", "Error sharing PDF: ${e.message}", e)
                Toast.makeText(this, "Error al compartir: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        currentPage?.close()
        pdfRenderer?.close()
    }
}
