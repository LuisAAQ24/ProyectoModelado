package com.example.intellihome

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

class PropertyDetailsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var socketViewModel: SocketViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_property_details)

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        socketViewModel = ViewModelProvider(this).get(SocketViewModel::class.java)
        socketViewModel.connectToServer("172.18.51.181", 6060)

        // Obtener los detalles de la propiedad
        val propertyDetails = intent.getStringExtra("propertyDetails")

        // Mostrar los detalles en la interfaz
        val propertyData = propertyDetails?.split(",")
        if (propertyData != null) {
            findViewById<TextView>(R.id.ubicación).text = propertyData[1]  // Ubicación
            findViewById<TextView>(R.id.precio).text = propertyData[5]    // Precio
            findViewById<TextView>(R.id.amenidades).text = propertyData[4] // Amenidades
        }

        // Configurar el botón "Alquilar"
        val buttonRent = findViewById<Button>(R.id.buttonRent)
        buttonRent.setOnClickListener {
            // Recuperar datos del usuario
            val username = sharedPreferences.getString("username", null)
            val phoneNumber = sharedPreferences.getString("phoneNumber", null)
            val totalAmount = propertyData?.get(5) // Asumiendo que el monto total está en la posición 5

            if (!username.isNullOrEmpty() && !phoneNumber.isNullOrEmpty() && totalAmount != null) {
                // Construir el mensaje
                val message = "alquilar,$username,$phoneNumber,${propertyData[1]},$totalAmount"

                // Enviar el mensaje a través del ViewModel
                socketViewModel.sendMessage(message)

                // Mostrar mensaje de éxito
                Toast.makeText(this, "Solicitud de alquiler enviada.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Faltan datos del usuario o detalles de la propiedad.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
