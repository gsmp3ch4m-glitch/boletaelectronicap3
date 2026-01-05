package com.p3.recibop3.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.p3.recibop3.R
import com.p3.recibop3.data.entity.DetalleReciboEntity
import com.p3.recibop3.data.entity.EmpresaEntity
import com.p3.recibop3.data.entity.ReciboEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {

    fun generateReciboPdf(
        context: Context,
        recibo: ReciboEntity,
        detalles: List<DetalleReciboEntity>,
        empresa: EmpresaEntity,
        fileName: String
    ): File? {
        return try {
            android.util.Log.d("PdfGenerator", "Starting PDF generation for: $fileName")
            
            val inflater = LayoutInflater.from(context)
            val pdfView = inflater.inflate(R.layout.layout_recibo_pdf, null, false)
            
            // Set dynamic title based on document type
            val titulo = when (recibo.tipoDocumento) {
                "RECIBO" -> "RECIBO DE VENTA"
                "PROFORMA" -> "PROFORMA"
                "NOTA_VENTA" -> "NOTA DE VENTA"
                "COMANDA" -> "COMANDA"
                else -> "RECIBO DE PAGO"
            }
            pdfView.findViewById<TextView>(R.id.pdfTitulo)?.text = titulo
            
            // Set company logo if available
            empresa.logoPath?.let { path ->
                val logoFile = File(path)
                if (logoFile.exists()) {
                    try {
                        val logoBitmap = BitmapFactory.decodeFile(path)
                        val logoView = pdfView.findViewById<ImageView>(R.id.pdfLogo)
                        logoView?.setImageBitmap(logoBitmap)
                        logoView?.visibility = View.VISIBLE
                        android.util.Log.d("PdfGenerator", "Logo loaded successfully")
                    } catch (e: Exception) {
                        android.util.Log.e("PdfGenerator", "Error loading logo: ${e.message}")
                    }
                }
            }
            
            // Set empresa data
            pdfView.findViewById<TextView>(R.id.pdfEmpresaNombre)?.text = empresa.nombre
            pdfView.findViewById<TextView>(R.id.pdfEmpresaRuc)?.text = "RUC: ${empresa.ruc}"
            pdfView.findViewById<TextView>(R.id.pdfEmpresaDireccion)?.text = "Dirección: ${empresa.direccion}"
            pdfView.findViewById<TextView>(R.id.pdfEmpresaTelefono)?.text = "Teléfono: ${empresa.telefono}"
            
            val redesView = pdfView.findViewById<TextView>(R.id.pdfEmpresaRedes)
            if (empresa.redesSociales.isNullOrEmpty()) {
                redesView?.visibility = View.GONE
            } else {
                redesView?.text = "Redes: ${empresa.redesSociales}"
            }
            
            // Set recibo number and date
            pdfView.findViewById<TextView>(R.id.pdfNumeroRecibo)?.text = "N° ${recibo.numeroRecibo}"
            
            val fecha = if (recibo.fechaPersonalizada != null) {
                recibo.fechaPersonalizada
            } else {
                val sdf = SimpleDateFormat("dd/MM/yyyy - hh:mm a", Locale.getDefault())
                sdf.format(Date(recibo.fechaHoraEmision))
            }
            pdfView.findViewById<TextView>(R.id.pdfFecha)?.text = "Fecha: $fecha"
            
            // Set cliente data
            pdfView.findViewById<TextView>(R.id.pdfClienteNombre)?.text = "Cliente: ${recibo.clienteNombre}"
            pdfView.findViewById<TextView>(R.id.pdfClienteDocumento)?.text = "DNI/RUC: ${recibo.clienteDocumento}"
            pdfView.findViewById<TextView>(R.id.pdfClienteTelefono)?.text = "Teléfono: ${recibo.clienteTelefono}"
            pdfView.findViewById<TextView>(R.id.pdfClienteDireccion)?.text = "Dirección: ${recibo.clienteDireccion}"
            
            // Add detalles
            val detallesContainer = pdfView.findViewById<LinearLayout>(R.id.pdfDetallesContainer)
            detalles.forEach { detalle ->
                val itemView = inflater.inflate(R.layout.item_pdf_detalle, null, false)
                itemView.findViewById<TextView>(R.id.pdfItemDescripcion)?.text = detalle.descripcionProducto
                itemView.findViewById<TextView>(R.id.pdfItemCantidad)?.text = String.format("%.0f", detalle.cantidad)
                itemView.findViewById<TextView>(R.id.pdfItemPrecioUnit)?.text = String.format("S/ %.2f", detalle.precioUnitario)
                itemView.findViewById<TextView>(R.id.pdfItemTotal)?.text = String.format("S/ %.2f", detalle.precioTotalLinea)
                detallesContainer?.addView(itemView)
            }
            
            // Set totales
            pdfView.findViewById<TextView>(R.id.pdfMontoTotal)?.text = String.format("S/ %.2f", recibo.montoTotal)
            pdfView.findViewById<TextView>(R.id.pdfPagoTipoLabel)?.text = 
                if (recibo.pagoTipo == "TOTAL") "PAGO TOTAL:" else "PAGO PARCIAL:"
            pdfView.findViewById<TextView>(R.id.pdfMontoPagado)?.text = String.format("S/ %.2f", recibo.montoPagado)
            
            val saldoContainer = pdfView.findViewById<LinearLayout>(R.id.pdfSaldoContainer)
            if (recibo.saldoPendiente > 0) {
                saldoContainer?.visibility = View.VISIBLE
                pdfView.findViewById<TextView>(R.id.pdfSaldoPendiente)?.text = 
                    String.format("S/ %.2f", recibo.saldoPendiente)
            }

            // DEBUG: Log status for watermark
            android.util.Log.d("PdfGenerator", "Checking watermark for status: '${recibo.estado}'")

            // Set watermark if status is ANULADO
            if (recibo.estado.equals("ANULADO", ignoreCase = true)) {
                android.util.Log.d("PdfGenerator", "Status matches ANULADO - Showing watermark")
                pdfView.findViewById<TextView>(R.id.pdfWatermarkAnulado)?.visibility = View.VISIBLE
            } else {
                 android.util.Log.d("PdfGenerator", "Status does NOT match ANULADO")
            }
            
            // Set firma
            empresa.firmaPath?.let { path ->
                val firmaFile = File(path)
                if (firmaFile.exists()) {
                    try {
                        val bitmap = BitmapFactory.decodeFile(path)
                        pdfView.findViewById<ImageView>(R.id.pdfFirma)?.setImageBitmap(bitmap)
                        android.util.Log.d("PdfGenerator", "Firma loaded successfully")
                    } catch (e: Exception) {
                        android.util.Log.e("PdfGenerator", "Error loading firma: ${e.message}")
                    }
                }
            }
            
            android.util.Log.d("PdfGenerator", "View populated, generating PDF file")
            
            // Generate PDF
            val pdfFile = generatePdfFromView(context, pdfView, fileName)
            
            if (pdfFile != null && pdfFile.exists()) {
                android.util.Log.d("PdfGenerator", "PDF generated successfully at: ${pdfFile.absolutePath}")
            } else {
                android.util.Log.e("PdfGenerator", "PDF file is null or doesn't exist")
            }
            
            pdfFile
        } catch (e: Exception) {
            android.util.Log.e("PdfGenerator", "Error generating PDF: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }

    private fun generatePdfFromView(
        context: Context,
        view: View,
        fileName: String,
        width: Int = 595,
        height: Int = 842
    ): File? {
        return try {
            view.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
            )
            view.layout(0, 0, width, height)

            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(width, height, 1).create()
            val page = pdfDocument.startPage(pageInfo)

            view.draw(page.canvas)
            pdfDocument.finishPage(page)

            val directory = File(context.filesDir, "ReciboP3")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(directory, fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveBitmapToFile(context: Context, bitmap: Bitmap, fileName: String): File? {
        return try {
            val directory = File(context.filesDir, "Firmas")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(directory, fileName)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
