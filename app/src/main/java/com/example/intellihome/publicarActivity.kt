package com.example.intellihome

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import java.util.*
import androidx.lifecycle.Observer
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView

class publicarActivity : BaseActivity() {
    private val imagenesSeleccionadas = mutableListOf<Uri>() // Lista para almacenar las imágenes seleccionadas
    private lateinit var botonAgregarImagen: Button
    private lateinit var contenedorImagenes: LinearLayout
    private val IMAGENES_MAXIMAS = 10
    private lateinit var socketViewModel: SocketViewModel
    private lateinit var capacidadInput: EditText
    private lateinit var descripcionInput: EditText
    private lateinit var reglasInput: EditText
    private lateinit var seek_bar_capacidad: SeekBar
    private lateinit var ubicacionTexto: TextView
    private lateinit var finalizarButton: Button
    private lateinit var ubicacionButton: Button
    private lateinit var amenidadesButton: Button
    private lateinit var precioTextView: TextView
    private lateinit var seekBarPrecio: SeekBar
    private var precioSeleccionado: Int = 0
    private lateinit var amenidadesSeleccionadasTextView: TextView
    var capacidadSeleccionada: Int = 0 // Variable para almacenar la capacidad seleccionada
    private lateinit var fechaInicioButton: Button
    private lateinit var fechaFinButton: Button
    private lateinit var fechaInicioTexto: TextView
    private lateinit var fechaFinTexto: TextView
    private var fechaInicio: Calendar = Calendar.getInstance()
    private var fechaFin: Calendar = Calendar.getInstance()

    private val amenidadesArray = arrayOf(
        "Wi-Fi gratuito", "Cocina equipada", "Aire acondicionado", "Calefacción",
        "Televisión por cable", "Equipo de lavandería", "Piscina", "Patio",
        "Parrilla", "Terraza", "Gimnasio", "Garaje", "Sistema de seguridad",
        "Habitaciones con baño", "Muebles de exterior", "Microondas",
        "Lavavajillas", "Cafetera", "Ropa de cama", "Áreas comunes",
        "Camas adicionales", "Servicio de limpieza", "Acceso a transporte público",
        "Mascotas permitidas", "Cercanía a restaurantes", "Sistema de calefacción",
        "Escritorio", "Sistemas de entretenimiento", "Chimenea", "Internet de alta velocidad"
    )

    private val amenidadesSeleccionadas = BooleanArray(amenidadesArray.size) { false }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publicar)

        // Inicializar el ViewModel
        socketViewModel = ViewModelProvider(this).get(SocketViewModel::class.java)

        // Inicializar las vistas
        contenedorImagenes = findViewById(R.id.contenedor_imagen)
        ubicacionTexto = findViewById(R.id.ubicacion_texto)
        reglasInput = findViewById(R.id.reglas_input)
        descripcionInput = findViewById(R.id.descripcion_input)
        finalizarButton = findViewById(R.id.finalizar_button)
        fechaInicioButton = findViewById(R.id.fechainicio_button)
        fechaFinButton = findViewById(R.id.fechafin_button)
        fechaInicioTexto = findViewById(R.id.fechainicio_texto)
        fechaFinTexto = findViewById(R.id.fechafin_texto)
        ubicacionButton = findViewById(R.id.ubicacion_button)
        amenidadesButton = findViewById(R.id.amenidades_button)
        precioTextView = findViewById(R.id.precio_text_view)
        seekBarPrecio = findViewById(R.id.seek_bar_precio)
        seek_bar_capacidad = findViewById(R.id.seek_bar_capacidad)
        amenidadesSeleccionadasTextView = findViewById(R.id.amenidades_seleccionadas_text_view)

        // Iniciar conexión al servidor
        socketViewModel.connectToServer("172.18.116.167", 6060)

        // Ver las respuestas del servidor
        socketViewModel.serverResponse.observe(this, Observer { response ->
            handleServerResponse(response)
        })

        // Configuración de SeekBar para el precio
        seekBarPrecio.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                precioSeleccionado = progress // Almacenar el precio seleccionado
                precioTextView.text = "Precio: $$precioSeleccionado" // Actualizar el texto
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Inicialización del SeekBar y TextView de capacidad
        val capacidadTextView = findViewById<TextView>(R.id.capacidad_text_view)

        seek_bar_capacidad.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                capacidadSeleccionada = progress
                capacidadTextView.text = "Capacidad: $capacidadSeleccionada" // Actualiza el TextView
                Log.d("publicarActivity", "Capacidad seleccionada: $capacidadSeleccionada")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Configuración del botón para agregar imágenes
        botonAgregarImagen = findViewById(R.id.agregar_imagen)
        contenedorImagenes = findViewById(R.id.contenedor_imagen)

        botonAgregarImagen.setOnClickListener {
            if (imagenesSeleccionadas.size < IMAGENES_MAXIMAS) {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
                startActivityForResult(Intent.createChooser(intent, "Selecciona imagen"), 2)
            } else {
                Toast.makeText(this, "Solo puedes subir hasta 10 imágenes", Toast.LENGTH_SHORT).show()
            }
        }


        // Configurar listener para abrir el mapa
        ubicacionButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivityForResult(intent, 1)
        }

        // Configurar listener para el botón de selección de amenidades
        amenidadesButton.setOnClickListener {
            showAmenidadesDialog()
        }

        // Configurar listener para el botón "Finalizar"

        finalizarButton.setOnClickListener {
            enviarDatosAlServidor()

        }

        // Configurar DatePicker para seleccionar fechas
        fechaInicioButton.setOnClickListener {
            mostrarDatePickerDialog(fechaInicio) { fechaSeleccionada ->
                fechaInicio = fechaSeleccionada
                actualizarFechaTexto(fechaInicioTexto, fechaInicio)
                validarFechas()
            }
        }

        fechaFinButton.setOnClickListener {
            mostrarDatePickerDialog(fechaFin) { fechaSeleccionada ->
                fechaFin = fechaSeleccionada
                actualizarFechaTexto(fechaFinTexto, fechaFin)
                validarFechas()
            }
        }

        // Restaurar el estado si fue guardado
        if (savedInstanceState != null) {
            capacidadSeleccionada = savedInstanceState.getInt("capacidad", 0) // Restaurar capacidad
            capacidadInput.setText(capacidadSeleccionada.toString()) // Mostrar la capacidad restaurada
            seek_bar_capacidad.progress = capacidadSeleccionada // Ajustar el SeekBar
            ubicacionTexto.text = savedInstanceState.getString("ubicacionTexto")
        }

        // Configurar padding para los insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Mostrar el diálogo de selección de amenidades
    private fun showAmenidadesDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona las amenidades")
        builder.setMultiChoiceItems(amenidadesArray, amenidadesSeleccionadas) { _, which, isChecked ->
            amenidadesSeleccionadas[which] = isChecked
        }

        builder.setPositiveButton("Aceptar") { _, _ ->
            mostrarAmenidadesSeleccionadas()
        }
        builder.setNegativeButton("Cancelar", null)
        builder.create().show()
    }

    // Mostrar las amenidades seleccionadas en un TextView
    private fun mostrarAmenidadesSeleccionadas() {
        val amenidadesSeleccionadasTexto = mutableListOf<String>()
        for (i in amenidadesArray.indices) {
            if (amenidadesSeleccionadas[i]) {
                amenidadesSeleccionadasTexto.add(amenidadesArray[i])
            }
        }
        amenidadesSeleccionadasTextView.text = amenidadesSeleccionadasTexto.joinToString(" ")
    }

    private fun getDatosIngresados(): String {
        val capacidad = capacidadSeleccionada.toString()
        val ubicacionTexto = ubicacionTexto.text.toString().ifBlank { "Ubicacion no proporcionada" }
        val descripcion = descripcionInput.text.toString()
        val reglas = reglasInput.text.toString()
        val amenidades = amenidadesSeleccionadasTextView.text.toString()
        var precio = precioSeleccionado.toString()

        val formatoFecha = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaInicioString = formatoFecha.format(fechaInicio.time)
        val fechaFinString = formatoFecha.format(fechaFin.time)
        val diaInicio = fechaInicio.get(Calendar.DAY_OF_MONTH)
        val mesInicio = fechaInicio.get(Calendar.MONTH) + 1
        val nuevaCantidad = calcularNuevaCantidad(diaInicio, mesInicio, 13.0, 2.0, precioSeleccionado.toDouble())
        Toast.makeText(this, "Cantidad ajustada: $nuevaCantidad", Toast.LENGTH_SHORT).show()
        return "publicar,$descripcion,$capacidad,$ubicacionTexto,$amenidades,$nuevaCantidad,$reglas,$fechaInicioString,$fechaFinString"
    }

    private fun enviarDatosAlServidor() {
        if (validarCampos()) {
            val datos = getDatosIngresados()
            socketViewModel.sendMessage(datos)
            val intent = Intent(this, MainActivity3::class.java)
            startActivity(intent)
        }
    }

    private fun validarCampos(): Boolean {
        if (descripcionInput.text.isBlank()) {
            Toast.makeText(this, "Debe ingresar una descripción", Toast.LENGTH_SHORT).show()
            return false
        }

        if (reglasInput.text.isBlank()) {
            Toast.makeText(this, "Debe ingresar las reglas", Toast.LENGTH_SHORT).show()
            return false
        }

        if (ubicacionTexto.text.isBlank()) {
            Toast.makeText(this, "Debe seleccionar una ubicación", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun mostrarDatePickerDialog(calendar: Calendar, callback: (Calendar) -> Unit) {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val nuevaFecha = Calendar.getInstance()
                nuevaFecha.set(year, month, dayOfMonth)
                callback(nuevaFecha)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun actualizarFechaTexto(textView: TextView, calendar: Calendar) {
        val formatoFecha = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        textView.text = formatoFecha.format(calendar.time)
    }

    private fun validarFechas() {
        if (fechaInicio.after(fechaFin)) {
            Toast.makeText(this, "La fecha de inicio no puede ser posterior a la fecha de fin", Toast.LENGTH_SHORT).show()
        }
    }
    fun calcularNuevaCantidad(dia: Int, mes: Int, porcentajeImpuesto: Double, comision: Double, montoTotal: Double): Double {
        val limiteMaximo = 0.10 * montoTotal
        var mediaArmonica: Double

        // Calcular la media armónica
        if (porcentajeImpuesto + comision > 0) {
            mediaArmonica = 2 / ((1 / porcentajeImpuesto) + (1 / comision))
        } else {
            mediaArmonica = 0.0
        }

        // Calcular el factor de ajuste
        val factorAjuste = (dia + mes) / 100.0

        // Ajustar la media armónica
        var mediaArmonicaAjustada = mediaArmonica * factorAjuste

        // Asegurarse de que no exceda el límite
        if (mediaArmonicaAjustada > limiteMaximo) {
            mediaArmonicaAjustada = limiteMaximo
        }

        return mediaArmonicaAjustada
    }

    private fun handleServerResponse(response: String) {
        // Procesar la respuesta del servidor aquí
        Toast.makeText(this, "Respuesta del servidor: $response", Toast.LENGTH_SHORT).show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("capacidad", capacidadSeleccionada)
        outState.putString("ubicacionTexto", ubicacionTexto.text.toString())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            val ubicacion = data?.getStringExtra("ubicacion") ?: ""
            ubicacionTexto.text = ubicacion
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                if (imagenesSeleccionadas.size < IMAGENES_MAXIMAS) {
                    imagenesSeleccionadas.add(uri)
                    val imageView = ImageView(this)
                    imageView.setImageURI(uri)
                    contenedorImagenes.addView(imageView)
                }
            }
        }
    }
}


