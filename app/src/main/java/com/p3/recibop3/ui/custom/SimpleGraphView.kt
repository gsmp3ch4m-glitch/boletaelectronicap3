package com.p3.recibop3.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import java.text.SimpleDateFormat
import java.util.Locale

class SimpleGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val dataPoints = mutableListOf<Pair<String, Double>>()
    
    // Paints
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#448AFF")
        strokeWidth = 8f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#20448AFF") 
        style = Paint.Style.FILL
    }
    
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CCCCCC")
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 30f
        textAlign = Paint.Align.CENTER
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#448AFF")
        style = Paint.Style.FILL
    }

    private val dotInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    // Settings
    private val padding = 80f
    private val bottomMargin = 100f
    
    fun setData(newData: List<Pair<Long, Double>>) {
        // Convert timestamps to date strings for simplicity in this demo view
        val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
        dataPoints.clear()
        
        // Aggregate by day to ensure clean graph
        val aggregated = newData.groupBy { 
            sdf.format(it.first) 
        }.mapValues { entry ->
            entry.value.sumOf { it.second }
        }.toList().sortedBy { it.first } // String sorting might be weak if crossing months, but fine for simple daily view

        // Limit to max 7-10 points for readability on mobile width, or just take them all if few
        dataPoints.addAll(aggregated.map { Pair(it.first, it.second) })
        
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (dataPoints.isEmpty()) {
            canvas.drawText("Sin datos para el rango seleccionado", width / 2f, height / 2f, textPaint)
            return
        }

        val w = width.toFloat()
        val h = height.toFloat()
        
        val maxVal = dataPoints.maxOfOrNull { it.second } ?: 1.0
        val maxY = if (maxVal == 0.0) 100.0 else maxVal * 1.2 // Add headroom
        
        val graphHeight = h - padding - bottomMargin
        val graphWidth = w - (padding * 2)
        
        // Draw Axis Lines (3 horizontal lines)
        val steps = 3
        for (i in 0..steps) {
            val y = padding + (graphHeight * i / steps)
            canvas.drawLine(padding, y, w - padding, y, axisPaint)
            
            // Y-Axis Labels
            val value = maxY - (maxY * i / steps)
            val label = String.format("%.0f", value) // Simplified
            val textX = padding - 10f
            val textY = y - 10f
            
            // paint config for axis labels
            textPaint.textAlign = Paint.Align.RIGHT
            textPaint.textSize = 24f
            canvas.drawText(label, textX, y + 8f, textPaint)
        }
        
        if (dataPoints.size < 2) {
             // Just draw a single bar or dot if 1 point
             // ... for now handle >1 logic mainly or simplified
        }

        val stepX = graphWidth / (dataPoints.size.coerceAtLeast(1))

        val path = Path()
        val fillPath = Path()
        
        fillPath.moveTo(padding, h - bottomMargin)

        dataPoints.forEachIndexed { index, point ->
            val x = padding + (stepX * index) + (stepX / 2) // Center in slot
            val y = (h - bottomMargin) - ((point.second / maxY) * graphHeight).toFloat()
            
            if (index == 0) {
                path.moveTo(x, y)
                fillPath.lineTo(x, y)
            } else {
                
                // Bezier curve for smoothness
                val prevX = padding + (stepX * (index - 1)) + (stepX / 2)
                val prevY = (h - bottomMargin) - ((dataPoints[index-1].second / maxY) * graphHeight).toFloat()
                
                val midX = (prevX + x) / 2
                path.cubicTo(midX, prevY, midX, y, x, y)
                fillPath.cubicTo(midX, prevY, midX, y, x, y)
            }
            
            // X-Axis Labels (Date)
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = 26f
            canvas.drawText(point.first, x, h - bottomMargin + 40f, textPaint)
            
            // Value Labels above dots
             canvas.drawText(String.format("%.0f", point.second), x, y - 20f, textPaint)
        }
        
        fillPath.lineTo(padding + (stepX * (dataPoints.size - 1)) + (stepX/2), h - bottomMargin)
        fillPath.close()

        // Draw Fill
        canvas.drawPath(fillPath, fillPaint)
        
        // Draw Line
        canvas.drawPath(path, linePaint)
        
        // Draw Dots
        dataPoints.forEachIndexed { index, point ->
            val x = padding + (stepX * index) + (stepX / 2)
            val y = (h - bottomMargin) - ((point.second / maxY) * graphHeight).toFloat()
            canvas.drawCircle(x, y, 12f, dotPaint)
            canvas.drawCircle(x, y, 6f, dotInnerPaint)
        }
    }
}
