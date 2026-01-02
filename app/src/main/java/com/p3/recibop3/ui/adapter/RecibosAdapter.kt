package com.p3.recibop3.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.p3.recibop3.R
import com.p3.recibop3.data.entity.ReciboEntity
import com.p3.recibop3.databinding.ItemReciboBinding
import java.text.SimpleDateFormat
import java.util.*

class RecibosAdapter(
    private val onReciboClick: (ReciboEntity) -> Unit
) : ListAdapter<ReciboEntity, RecibosAdapter.ReciboViewHolder>(ReciboDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReciboViewHolder {
        val binding = ItemReciboBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReciboViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReciboViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReciboViewHolder(
        private val binding: ItemReciboBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(recibo: ReciboEntity) {
            binding.apply {
                tvNumeroRecibo.text = recibo.numeroRecibo
                
                val fecha = if (recibo.fechaPersonalizada != null) {
                    recibo.fechaPersonalizada
                } else {
                    val sdf = SimpleDateFormat("dd/MM/yyyy - hh:mm a", Locale.getDefault())
                    sdf.format(Date(recibo.fechaHoraEmision))
                }
                tvFecha.text = fecha
                
                tvCliente.text = "Cliente: ${recibo.clienteNombre}"
                tvMontoTotal.text = String.format("S/ %.2f", recibo.montoTotal)
                
                // Estado chip
                chipEstado.text = recibo.estado
                if (recibo.estado == "EMITIDO") {
                    chipEstado.setChipBackgroundColorResource(R.color.estado_emitido)
                } else {
                    chipEstado.setChipBackgroundColorResource(R.color.estado_anulado)
                }
                
                root.setOnClickListener {
                    onReciboClick(recibo)
                }
            }
        }
    }

    class ReciboDiffCallback : DiffUtil.ItemCallback<ReciboEntity>() {
        override fun areItemsTheSame(oldItem: ReciboEntity, newItem: ReciboEntity): Boolean {
            return oldItem.idRecibo == newItem.idRecibo
        }

        override fun areContentsTheSame(oldItem: ReciboEntity, newItem: ReciboEntity): Boolean {
            return oldItem == newItem
        }
    }
}
