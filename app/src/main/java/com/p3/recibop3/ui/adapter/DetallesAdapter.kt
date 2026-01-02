package com.p3.recibop3.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.p3.recibop3.data.entity.DetalleReciboEntity
import com.p3.recibop3.databinding.ItemDetalleProductoBinding

class DetallesAdapter(
    private val onDeleteClick: (DetalleReciboEntity, Int) -> Unit
) : RecyclerView.Adapter<DetallesAdapter.DetalleViewHolder>() {

    private val detalles = mutableListOf<DetalleReciboEntity>()

    fun setDetalles(newDetalles: List<DetalleReciboEntity>) {
        detalles.clear()
        detalles.addAll(newDetalles)
        notifyDataSetChanged()
    }

    fun addDetalle(detalle: DetalleReciboEntity) {
        detalles.add(detalle)
        notifyItemInserted(detalles.size - 1)
    }

    fun removeDetalle(position: Int) {
        if (position >= 0 && position < detalles.size) {
            detalles.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getDetalles(): List<DetalleReciboEntity> = detalles.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetalleViewHolder {
        val binding = ItemDetalleProductoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DetalleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetalleViewHolder, position: Int) {
        holder.bind(detalles[position], position)
    }

    override fun getItemCount(): Int = detalles.size

    inner class DetalleViewHolder(
        private val binding: ItemDetalleProductoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(detalle: DetalleReciboEntity, position: Int) {
            binding.apply {
                tvDescripcion.text = detalle.descripcionProducto
                tvDetalle.text = String.format(
                    "Cant: %.0f x S/ %.2f",
                    detalle.cantidad,
                    detalle.precioUnitario
                )
                tvTotal.text = String.format("S/ %.2f", detalle.precioTotalLinea)
                
                btnEliminar.setOnClickListener {
                    onDeleteClick(detalle, position)
                }
            }
        }
    }
}
