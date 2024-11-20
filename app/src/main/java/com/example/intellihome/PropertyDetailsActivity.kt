package com.example.intellihome

import android.app.DatePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.intellihome.utils.ThemeUtils
import java.text.SimpleDateFormat
import java.util.*

class PropertyDetailsActivity : BaseActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var socketViewModel: SocketViewModel
    private lateinit var startDate: String
    private lateinit var endDate: String
    private lateinit var startCalendar: Calendar
    private lateinit var endCalendar: Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_property_details)
        ThemeUtils.applyTheme(this)

        socketViewModel = ViewModelProvider(this).get(SocketViewModel::class.java)
        socketViewModel.connectToServer("172.18.116.167", 6060)
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        // Obtener los detalles de la propiedad
        val propertyDetails = intent.getStringExtra("propertyDetails")
        val propertyData = propertyDetails?.split(",")

        if (propertyData != null) {
            findViewById<TextView>(R.id.descripcion).text = "Descripción: ${propertyData[0]}"
            findViewById<TextView>(R.id.capacidad).text = "Capacidad: ${propertyData[1]}"
            findViewById<TextView>(R.id.ubicacion).text = "Ubicación: ${propertyData[2]}"
            findViewById<TextView>(R.id.amenidades).text = "Amenidades: ${propertyData[3]}"
            findViewById<TextView>(R.id.precio).text = "Precio: ${propertyData[4]}"
            findViewById<TextView>(R.id.reglas).text = "Reglas: ${propertyData[5]}"
            findViewById<TextView>(R.id.fechafin).text = "Inicio de disponibilidad: ${propertyData[6]}"
            findViewById<TextView>(R.id.fechainicio).text = "Fin de disponibilidad: ${propertyData[7]}"

            // Convertir las fechas de inicio y fin a Date
            val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            startCalendar = Calendar.getInstance()
            endCalendar = Calendar.getInstance()

            startCalendar.time = sdf.parse(propertyData[6])!!
            endCalendar.time = sdf.parse(propertyData[7])!!
        }

        // Configurar el botón para seleccionar la fecha de inicio
        val selectStartDateButton = findViewById<Button>(R.id.selectStartDate)
        selectStartDateButton.setOnClickListener {
            showDatePickerDialog(true) // Llamamos al picker de fecha de inicio
        }

        // Configurar el botón para seleccionar la fecha de fin
        val selectEndDateButton = findViewById<Button>(R.id.selectEndDate)
        selectEndDateButton.setOnClickListener {
            showDatePickerDialog(false) // Llamamos al picker de fecha de fin
        }

        // Configurar el botón "Alquilar"
        val buttonRent = findViewById<Button>(R.id.buttonRent)
        buttonRent.setOnClickListener {
            if (::startDate.isInitialized && ::endDate.isInitialized) {
                val storedPassword = sharedPreferences.getString("password", null)
                if (storedPassword != null) {
                    // Construir el mensaje
                    val message = "alquilar,$storedPassword,$propertyDetails,$startDate,$endDate"
                    socketViewModel.sendMessage(message) // Enviar el mensaje al servidor
                    // Para propósitos de demostración, muestra el mensaje
                    Toast.makeText(this, "Mensaje enviado: $message", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No se encontró la contraseña.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor, selecciona un periodo de alquiler válido.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Método para mostrar el DatePickerDialog
    private fun showDatePickerDialog(isStartDate: Boolean) {
        val calendar = if (isStartDate) startCalendar else endCalendar
        val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${month + 1}/$year"
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)

            // Verificar si la fecha seleccionada es válida
            if (isStartDate) {
                startDate = selectedDate
                findViewById<Button>(R.id.selectStartDate).text = "Fecha inicio: $startDate"
            } else {
                if (selectedCalendar.after(startCalendar)) {
                    endDate = selectedDate
                    findViewById<Button>(R.id.selectEndDate).text = "Fecha fin: $endDate"
                } else {
                    Toast.makeText(this, "La fecha de fin debe ser posterior a la fecha de inicio.", Toast.LENGTH_SHORT).show()
                }
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        datePickerDialog.datePicker.minDate = startCalendar.timeInMillis
        datePickerDialog.datePicker.maxDate = endCalendar.timeInMillis
        datePickerDialog.show()
    }
}


