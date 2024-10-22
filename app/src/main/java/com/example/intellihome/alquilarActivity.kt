package com.example.intellihome

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.intellihome.databinding.ActivityAlquilarBinding

class alquilarActivity : BaseActivity() {

    private lateinit var binding: ActivityAlquilarBinding
    private lateinit var propiedadAdapter: PropiedadAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlquilarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        propiedadAdapter = PropiedadAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@alquilarActivity)
            adapter = propiedadAdapter
        }

        // Aquí puedes obtener los datos de tu servidor y enviarlos al adaptador
        val propiedades = listOf(
            Propiedad("1", "Casa en la playa", "Playa del Carmen", 250000.0, "Casa hermosa cerca de la playa", "https://example.com/casa.jpg", Color.WHITE),
            Propiedad("2", "Apartamento en el centro", "Ciudad de México", 150000.0, "Apartamento moderno en el corazón de la ciudad", "https://example.com/apartamento.jpg", Color.WHITE)
        )


        propiedadAdapter.submitList(propiedades)
    }
}
