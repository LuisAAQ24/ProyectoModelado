package com.example.intellihome

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import java.util.*

class publicarActivity : BaseActivity() {

    private lateinit var socketViewModel: SocketViewModel
    private lateinit var ubicacionInput: EditText
    private lateinit var capacidadInput: EditText
    private lateinit var fechaSeleccionada: TextView
    private lateinit var finalizarButton: Button
    private lateinit var fechaButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_publicar)

        // Inicializar el ViewModel
        socketViewModel = ViewModelProvider(this).get(SocketViewModel::class.java)

        // Conectar al servidor
        socketViewModel.connectToServer("172.18.51.181", 6060)

        // Inicializar las vistas
        ubicacionInput = findViewById(R.id.ubicacion_input)
        capacidadInput = findViewById(R.id.capacidad_input)
        fechaSeleccionada = findViewById(R.id.fecha_seleccionada)
        finalizarButton = findViewById(R.id.finalizar_button)
        fechaButton = findViewById(R.id.fecha_button)

        // Configurar listener para el botón de fecha
        fechaButton.setOnClickListener {
            showDatePickerDialog()
        }

        // Configurar listener para el botón "Finalizar"
        finalizarButton.setOnClickListener {
            enviarDatosAlServidor()
        }

        // Configurar padding para los insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Función para mostrar el selector de fecha
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // Mostrar la fecha seleccionada
            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            fechaSeleccionada.text = selectedDate
        }, year, month, day)

        datePickerDialog.show()
    }

    // Función para obtener los datos ingresados por el usuario
    private fun getDatosIngresados(): String {
        val ubicacion = ubicacionInput.text.toString()
        val capacidad = capacidadInput.text.toString()
        val fecha = fechaSeleccionada.text.toString()

        // Formatear los datos como un String para enviar al servidor
        return "publicar,$ubicacion,$capacidad,$fecha"
    }

    // Función para enviar los datos al servidor
    private fun enviarDatosAlServidor() {
        val datos = getDatosIngresados()
        socketViewModel.sendMessage(datos)  // Enviar los datos al servidor
    }
}
