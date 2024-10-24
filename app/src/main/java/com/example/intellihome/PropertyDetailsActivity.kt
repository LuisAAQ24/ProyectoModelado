package com.example.intellihome

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.intellihome.utils.ThemeUtils

class PropertyDetailsActivity : BaseActivity() {

    private lateinit var sharedPreferences: SharedPreferences // Declara SharedPreferences
    private lateinit var socketViewModel: SocketViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_property_details)
        ThemeUtils.applyTheme(this)
        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        socketViewModel = ViewModelProvider(this).get(SocketViewModel::class.java)
        socketViewModel.connectToServer("172.18.51.181", 6060)

        // Obtener los detalles de la propiedad
        val propertyDetails = intent.getStringExtra("propertyDetails")

        // Mostrar los detalles en la interfaz
        val propertyData = propertyDetails?.split(",")
        if (propertyData != null) {
            findViewById<TextView>(R.id.reglas).text = "Reglas: ${propertyData[5]}"
            findViewById<TextView>(R.id.descripcion).text = "Descripción: ${propertyData[0]}"
            findViewById<TextView>(R.id.capacidad).text = "Capacidad: ${propertyData[1]}"
            findViewById<TextView>(R.id.ubicación).text = "Ubicación: ${propertyData[2]}"  // Ubicación (índice 1)
            findViewById<TextView>(R.id.precio).text = "Precio: ${propertyData[4]}"    // Precio (índice 5)
            findViewById<TextView>(R.id.amenidades).text = "Amenidades: ${propertyData[3]}"// Amenidades (índice 4)
        }

        // Configurar el botón "Alquilar"
        val buttonRent = findViewById<Button>(R.id.buttonRent) // Asegúrate de que tengas un botón en tu layout con este ID
        buttonRent.setOnClickListener {
            // Recuperar la contraseña
            val storedPassword = sharedPreferences.getString("password", null)
            if (storedPassword != null) {
                // Construir el mensaje
                val message = "alquilar,$storedPassword,$propertyDetails"

                // Enviar el mensaje a través del ViewModel
                socketViewModel.sendMessage(message) // Envía el mensaje al servidor
                // Para propósitos de demostración, muestra el mensaje
                Toast.makeText(this, "Mensaje enviado: $message", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No se encontró la contraseña.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
