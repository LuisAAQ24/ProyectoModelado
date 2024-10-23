package com.example.intellihome

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.intellihome.databinding.ItemPropiedadBinding

class PropiedadAdapter : ListAdapter<Propiedad, PropiedadAdapter.PropiedadViewHolder>(PropiedadDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropiedadViewHolder {
        // Inflar el layout utilizando Data Binding
        val binding =
            ItemPropiedadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PropiedadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PropiedadViewHolder, position: Int) {
        // Enlazar el item de propiedad con el ViewHolder
        holder.bind(getItem(position))
    }

    inner class PropiedadViewHolder(private val binding: ItemPropiedadBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(propiedad: Propiedad) {
            // Asignar los valores a las vistas
            binding.textViewNombre.text = propiedad.nombre
            binding.textViewUbicacion.text = propiedad.ubicacion
            binding.textViewPrecio.text = "$${propiedad.precio}"
            binding.textViewDescripcion.text = propiedad.descripcion

            // Cargar la imagen usando Glide
            Glide.with(binding.imageViewPropiedad.context)
                .load(propiedad.imagenUrl)
                .into(binding.imageViewPropiedad)
            binding.root.setBackgroundColor(propiedad.colorFondo)
        }
    }

    class PropiedadDiffCallback : DiffUtil.ItemCallback<Propiedad>() {
        override fun areItemsTheSame(oldItem: Propiedad, newItem: Propiedad): Boolean {
            // Comparar si los ids son iguales
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Propiedad, newItem: Propiedad): Boolean {
            // Comparar si el contenido de los objetos es el mismo
            return oldItem == newItem
        }
    }
}
