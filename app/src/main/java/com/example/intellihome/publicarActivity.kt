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

class publicarActivity : BaseActivity() {
    private val imagenesSeleccionadas = mutableListOf<Uri>() // Lista para almacenar las imágenes seleccionadas
    private lateinit var botonAgregarImagen: Button
    private lateinit var contenedorImagenes: LinearLayout
    private val IMAGENES_MAXIMAS = 10
    private lateinit var socketViewModel: SocketViewModel
    private lateinit var ubicacionInput: EditText
    private lateinit var capacidadInput: EditText

    private lateinit var ubicacionTexto: TextView
    private lateinit var finalizarButton: Button
    private lateinit var ubicacionButton: Button
    private lateinit var amenidadesButton: Button
    private lateinit var precioTextView: TextView
    private lateinit var seekBarPrecio: SeekBar
    private var precioSeleccionado: Int = 0
    private lateinit var amenidadesSeleccionadasTextView: TextView

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
        capacidadInput = findViewById(R.id.capacidad_input)
        contenedorImagenes = findViewById(R.id.contenedor_imagen)
        ubicacionTexto = findViewById(R.id.ubicacion_texto)
        finalizarButton = findViewById(R.id.finalizar_button)
        ubicacionButton = findViewById(R.id.ubicacion_button)
        amenidadesButton = findViewById(R.id.amenidades_button)
        precioTextView = findViewById(R.id.precio_text_view)
        seekBarPrecio = findViewById(R.id.seek_bar_precio)
        amenidadesSeleccionadasTextView = findViewById(R.id.amenidades_seleccionadas_text_view)

        // Iniciar conexión al servidor
        socketViewModel.connectToServer("172.18.51.181", 6060)

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

        // Configurar listener para el botón "Finalizar"
        finalizarButton.setOnClickListener {
            enviarDatosAlServidor()
        }

        // Restaurar el estado si fue guardado
        if (savedInstanceState != null) {
            ubicacionInput.setText(savedInstanceState.getString("ubicacion"))
            capacidadInput.setText(savedInstanceState.getString("capacidad"))
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
        amenidadesSeleccionadasTextView.text = amenidadesSeleccionadasTexto.joinToString(", ")
    }

    // Función para obtener los datos ingresados por el usuario
    private fun getDatosIngresados(): String {
        val ubicacion = ubicacionInput.text.toString()
        val capacidad = capacidadInput.text.toString()

        val ubicacionTexto = ubicacionTexto.text.toString()
        val precio = precioSeleccionado.toString()
        val amenidades = amenidadesSeleccionadasTextView.text.toString()

        // Formatear los datos como un String para enviar al servidor
        return "publicar,$capacidad,[$ubicacionTexto],$amenidades,$precio"
    }

    // Función para enviar los datos al servidor
    private fun enviarDatosAlServidor() {
        val datos = getDatosIngresados()
        socketViewModel.sendMessage(datos)  // Enviar los datos al servidor
    }

    // Guardar el estado actual antes de destruir la actividad
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("ubicacion", ubicacionInput.text.toString())
        outState.putString("capacidad", capacidadInput.text.toString())
        outState.putString("ubicacionTexto", ubicacionTexto.text.toString())
    }

    // Recibir los datos de la ubicación del mapa y de las imágenes seleccionadas
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val ubicacion = data?.getStringExtra("ubicacion")
            ubicacionTexto.text = ubicacion // Mostrar la ubicación seleccionada
        } else if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            if (data.clipData != null) { // Varias imágenes seleccionadas
                for (i in 0 until data.clipData!!.itemCount) {
                    if (imagenesSeleccionadas.size < IMAGENES_MAXIMAS) {
                        val uri = data.clipData!!.getItemAt(i).uri
                        imagenesSeleccionadas.add(uri)
                        agregarImagenAlContenedor(uri)
                    }
                }
            } else if (data.data != null) { // Solo una imagen seleccionada
                val uri = data.data
                uri?.let {
                    if (imagenesSeleccionadas.size < IMAGENES_MAXIMAS) {
                        imagenesSeleccionadas.add(uri)
                        agregarImagenAlContenedor(uri)
                    }
                }
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
