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



class publicarActivity : BaseActivity() {
    private val imagenesSeleccionadas = mutableListOf<Uri>() // Lista para almacenar las imágenes seleccionadas
    private lateinit var botonAgregarImagen: Button
    private lateinit var contenedorImagenes: LinearLayout
    private val IMAGENES_MAXIMAS = 10
    private lateinit var socketViewModel: SocketViewModel
    private lateinit var ubicacionInput: EditText
    private lateinit var capacidadInput: EditText
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

    // Variables para las fechas
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
        ubicacionInput = findViewById(R.id.ubicacion_input)
        contenedorImagenes = findViewById(R.id.contenedor_imagen)
        ubicacionTexto = findViewById(R.id.ubicacion_texto)
        finalizarButton = findViewById(R.id.finalizar_button)
        ubicacionButton = findViewById(R.id.ubicacion_button)
        amenidadesButton = findViewById(R.id.amenidades_button)
        precioTextView = findViewById(R.id.precio_text_view)
        seekBarPrecio = findViewById(R.id.seek_bar_precio)
        seek_bar_capacidad = findViewById(R.id.seek_bar_capacidad)
        amenidadesSeleccionadasTextView = findViewById(R.id.amenidades_seleccionadas_text_view)

        fechaInicioButton = findViewById(R.id.fecha_inicio_button)
        fechaFinButton = findViewById(R.id.fecha_fin_button)
        fechaInicioTexto = findViewById(R.id.fecha_inicio_texto)
        fechaFinTexto = findViewById(R.id.fecha_fin_texto)

        // Iniciar conexión al servidor
        socketViewModel.connectToServer("192.168.0.114", 6060)

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
                findViewById<TextView>(R.id.capacidad_text_view).text =
                    "Capacidad: $capacidadSeleccionada" // Actualiza el TextView
                Log.d("publicarActivity", "Capacidad seleccionada: $capacidadSeleccionada")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        //-----------------------------------------------------------------------
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
        //---------------------------------------------------------------------------



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
            startActivityForResult(intent, 1) // Request code 1 para el mapa
        }

        // Configurar listener para el botón de selección de amenidades
        amenidadesButton.setOnClickListener {
            showAmenidadesDialog()
        }

        finalizarButton.setOnClickListener {
            if (validarCampos()) {
                enviarDatosAlServidor()
            }
        }


        // Restaurar el estado si fue guardado
        if (savedInstanceState != null) {
            ubicacionInput.setText(savedInstanceState.getString("ubicacion"))
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
 //-------------------------------------------------------------------------------------------------------------------------------------------------------
    private fun mostrarDatePickerDialog(
        fecha: Calendar,
        onFechaSeleccionada: (Calendar) -> Unit
    ) {
        val year = fecha.get(Calendar.YEAR)
        val month = fecha.get(Calendar.MONTH)
        val day = fecha.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, año, mes, dia ->
                val fechaSeleccionada = Calendar.getInstance()
                fechaSeleccionada.set(año, mes, dia)
                onFechaSeleccionada(fechaSeleccionada)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun actualizarFechaTexto(textView: TextView, fecha: Calendar) {
        val formato = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        textView.text = formato.format(fecha.time)
    }

    private fun validarFechas() {
        if (fechaInicio.after(fechaFin)) {
            Toast.makeText(this, "La fecha de inicio debe ser menor o igual a la fecha de fin", Toast.LENGTH_SHORT).show()
            fechaFinTexto.text = "" // Limpia la fecha final si es incorrecta
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------------------------------------



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
        amenidadesSeleccionadasTextView.text = amenidadesSeleccionadasTexto.joinToString(", ")
    }

    private fun getDatosIngresados(): String {
        val ubicacion = ubicacionInput.text.toString().ifBlank { "Sin ubicación" }
        val capacidad = capacidadSeleccionada.toString()
        val ubicacionTexto = ubicacionTexto.text.toString().ifBlank { "Ubicacion no proporcionada" }
        val precio = precioSeleccionado.toString()
        val amenidades = amenidadesSeleccionadasTextView.text.toString().ifBlank { "Sin amenidades" }
        val Fecha1 = fechaInicioTexto.text.toString()
        val Fecha2 = fechaFinTexto .text.toString()

        Log.d("publicarActivity", "Capacidad: $capacidad")

        // Formatear los datos como un String para enviar al servidor
        return "publicar,$capacidad,[$ubicacionTexto],$amenidades,$precio,$Fecha1,$Fecha2"


    }

    private fun validarCampos(): Boolean {
        // Validar si la ubicación no está vacía
        if (ubicacionTexto.text.isBlank()) {
            Toast.makeText(this, "Debe seleccionar una ubicación", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validar si el precio es mayor a 0
        if (precioSeleccionado <= 0) {
            Toast.makeText(this, "El precio por noche debe ser mayor a 0", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validar si la capacidad es mayor a 0
        if (capacidadSeleccionada <= 0) {
            Toast.makeText(this, "La capacidad debe ser mayor a 0", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validar que al menos una imagen haya sido seleccionada
        if (imagenesSeleccionadas.isEmpty()) {
            Toast.makeText(this, "Debe agregar al menos una imagen", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validar que no haya más de 10 imágenes seleccionadas
        if (imagenesSeleccionadas.size > IMAGENES_MAXIMAS) {
            Toast.makeText(this, "No puede agregar más de 10 imágenes", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validar que la fecha de inicio no esté vacía
        if (fechaInicioTexto.text.isBlank()) {
            Toast.makeText(this, "Debe seleccionar una fecha de inicio", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validar que la fecha de fin no esté vacía
        if (fechaFinTexto.text.isBlank()) {
            Toast.makeText(this, "Debe seleccionar una fecha de fin", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validar que la fecha de inicio sea menor o igual a la fecha de fin
        if (fechaInicio.after(fechaFin)) {
            Toast.makeText(this, "La fecha de inicio debe ser menor o igual a la fecha de fin", Toast.LENGTH_SHORT).show()
            return false
        }

        // Si todas las validaciones pasan, retorna true
        return true
    }






    // Función para enviar los datos al servidor
    private fun enviarDatosAlServidor() {
        val datos = getDatosIngresados()  // Obtener los datos

        // Enviar el mensaje a través del ViewModel
        socketViewModel.sendMessage(datos)

        // Mostrar un Toast para confirmar que se está intentando enviar los datos
        Toast.makeText(this, "Datos enviados: $datos", Toast.LENGTH_SHORT).show()

        // Log para depuración en Logcat
        Log.d("publicarActivity", "Datos enviados: $datos")

        val intent = Intent(this, MainActivity3::class.java)
        startActivity(intent)
        finish()
    }


    // Guardar el estado actual antes de destruir la actividad
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("ubicacion", ubicacionInput.text.toString())
        outState.putInt("capacidad", capacidadSeleccionada) // Guardar capacidad seleccionada
        outState.putString("ubicacionTexto", ubicacionTexto.text.toString())
    }


    // Recibir los datos de la ubicación del mapa y de las imágenes seleccionadas
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) { // Verifica si es la ubicación
            val ubicacion = data?.getStringExtra("ubicacion") // Obtén la ubicación
            if (!ubicacion.isNullOrBlank()) {
                ubicacionTexto.text = ubicacion // Muestra la ubicación en el TextView
            }
        } else if (requestCode == 2 && resultCode == RESULT_OK && data != null) { // Manejo de imágenes
            val nuevasImagenes = mutableListOf<Uri>()

            if (data.clipData != null) { // Varias imágenes seleccionadas
                val totalItems = data.clipData!!.itemCount

                // Verificar si la selección supera el límite de 10 imágenes
                if (imagenesSeleccionadas.size + totalItems > IMAGENES_MAXIMAS) {
                    Toast.makeText(this, "Solo puedes agregar hasta 10 imágenes", Toast.LENGTH_SHORT).show()
                    return
                }

                // Agregar cada imagen seleccionada a la lista temporal
                for (i in 0 until totalItems) {
                    val uri = data.clipData!!.getItemAt(i).uri
                    nuevasImagenes.add(uri)
                }
            } else if (data.data != null) { // Solo una imagen seleccionada
                val uri = data.data

                // Verificar si agregar esta imagen supera el límite
                if (imagenesSeleccionadas.size + 1 > IMAGENES_MAXIMAS) {
                    Toast.makeText(this, "Solo puedes agregar hasta 10 imágenes", Toast.LENGTH_SHORT).show()
                    return
                }

                uri?.let { nuevasImagenes.add(it) }
            }

            // Agregar las imágenes seleccionadas al contenedor si no superan el límite
            nuevasImagenes.forEach { uri ->
                imagenesSeleccionadas.add(uri)
                agregarImagenAlContenedor(uri)
            }
        }
    }



    // Función para agregar la imagen seleccionada a un contenedor visual
    private fun agregarImagenAlContenedor(uri: Uri) {
        val imageView = ImageView(this)
        imageView.setImageURI(uri)
        imageView.layoutParams = LinearLayout.LayoutParams(200, 200)
        contenedorImagenes.addView(imageView) // Agregar la imagen al contenedor
    }

    // Manejar las respuestas del servidor
    private fun handleServerResponse(response: String) {
        // Aquí puedes manejar la respuesta del servidor según sea necesario
    }
}

