package com.example.intellihome

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import com.example.intellihome.utils.ThemeUtils

class MainActivity2 : BaseActivity() {
    private lateinit var editNombre: EditText
    private lateinit var editApellido: EditText
    private lateinit var editUsername: EditText
    private lateinit var editTelefono: EditText
    private lateinit var editHobbies: EditText
    private lateinit var editEmail: EditText
    private lateinit var editContrasena: EditText
    private lateinit var editConfirmarContrasena: EditText
    private lateinit var spinnerFormaPago: Spinner
    private lateinit var spinnerTipoUsuario: Spinner
    private lateinit var editNumeroTarjeta: EditText
    private lateinit var editFechaVencimiento: EditText
    private lateinit var editCVC: EditText
    private lateinit var textViewSeleccionFecha: TextView
    private lateinit var imageViewPerfil: ImageView
    private lateinit var buttonFechaNacimiento: Button
    private lateinit var buttonRegistrar: Button
    private lateinit var socketViewModel: SocketViewModel

    private lateinit var imageViewOjo: ImageView
    private var fechaNacimiento: String = ""
    private val palabrasProhibidas = listOf("coito", "coger", "sexo", "pene", "vagina", "tetas")
    private var selectedImageUri: Uri = Uri.EMPTY
    //seleccionar imagen (no lo entiendo bien)
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            imageViewPerfil.setImageURI(selectedImageUri)
        }
    }
    //está fallando
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            if (selectedImageUri != Uri.EMPTY) {
                imageViewPerfil.setImageURI(selectedImageUri)
            } else {
                Toast.makeText(this, getString(R.string.res10), Toast.LENGTH_SHORT).show() // Mensaje de error de URI
            }
        } else {
            Toast.makeText(this, getString(R.string.res11), Toast.LENGTH_SHORT).show() // Mensaje de error al capturar imagen
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        ThemeUtils.applyTheme(this)
        socketViewModel = ViewModelProvider(this).get(SocketViewModel::class.java)
        socketViewModel.connectToServer("172.18.173.122", 6060)
        socketViewModel.serverResponse.observe(this, androidx.lifecycle.Observer { response ->
            handleServerResponse(response)
        })
        imageViewOjo = findViewById(R.id.imageViewOjo)
        editNombre = findViewById(R.id.editNombre)
        editApellido = findViewById(R.id.editApellido)
        editUsername = findViewById(R.id.editUsername)
        editTelefono = findViewById(R.id.editTelefono)
        editHobbies = findViewById(R.id.editHobbies)
        editEmail = findViewById(R.id.editEmail)
        editContrasena = findViewById(R.id.editContrasena)
        editConfirmarContrasena = findViewById(R.id.editConfirmarContrasena)
        spinnerFormaPago = findViewById(R.id.spinnerFormaPago)
        spinnerTipoUsuario = findViewById(R.id.spinnerTipousuario)
        editNumeroTarjeta = findViewById(R.id.editNumeroTarjeta)
        editFechaVencimiento = findViewById(R.id.editFechaVencimiento)
        editCVC = findViewById(R.id.editCVC)
        textViewSeleccionFecha = findViewById(R.id.textViewSeleccionFecha)
        imageViewPerfil = findViewById(R.id.imageViewPerfil)
        buttonFechaNacimiento = findViewById(R.id.buttonFechaNacimiento)
        buttonRegistrar = findViewById(R.id.buttonRegistrar)
        buttonFechaNacimiento.setOnClickListener { mostrarFechaPicker() }
        buttonRegistrar.setOnClickListener { registrarUsuario() }
        applyCustomColors()
        imageViewOjo.setOnClickListener {
            // Cambiar la visibilidad de la contraseña
            val currentInputType = editContrasena.inputType

            // Verificar si la contraseña es visible
            if (currentInputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                editContrasena.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                editConfirmarContrasena.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD // Cambiar también editConfirmarContrasena
                imageViewOjo.setImageResource(R.drawable.ojo2) // Mantener el mismo ícono de ojo
            } else {
                editContrasena.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                editConfirmarContrasena.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD // Cambiar también editConfirmarContrasena
                imageViewOjo.setImageResource(R.drawable.ojo2) // Mantener el mismo ícono de ojo
            }

            // Mover el cursor al final del texto en ambos EditText
            editContrasena.setSelection(editContrasena.text.length)
            editConfirmarContrasena.setSelection(editConfirmarContrasena.text.length) // Mover el cursor también aquí
        }
        val formasPago = arrayOf(getString(R.string.res12), getString(R.string.res13), getString(R.string.res14), getString(R.string.res15))
        val tiposUsuario = arrayOf(getString(R.string.res17), getString(R.string.res18))
        val adapterTipoUsuario = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposUsuario)
        adapterTipoUsuario.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoUsuario.adapter = adapterTipoUsuario
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, formasPago)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFormaPago.adapter = adapter

        // Agregar el TextWatcher al campo de fecha de vencimiento
        editFechaVencimiento.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.length == 2 && before < count) {
                    editFechaVencimiento.append("/")
                }
                if (s != null && s.length > 5) {
                    editFechaVencimiento.setText(s.subSequence(0, 5))
                    editFechaVencimiento.setSelection(5)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


    }
    private fun applyCustomColors() {
        val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        val selectedColor = sharedPreferences.getInt("Selected_Color", Color.WHITE)

        // Obtener colores
        val darkColor = ThemeUtils.darkenColor(selectedColor)
        val lightColor = ThemeUtils.lightenColor(selectedColor)

        // Establecer color de fondo y de botones
        val mainLayout = findViewById<View>(R.id.main)
        mainLayout.setBackgroundColor(lightColor)
        buttonFechaNacimiento.setBackgroundColor(darkColor)
        buttonRegistrar.setBackgroundColor(darkColor)
    }
    private fun handleServerResponse(response: String?) {
        if (response == "true") {
            Toast.makeText(this, getString(R.string.res27), Toast.LENGTH_SHORT).show() // "Registro exitoso"
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, getString(R.string.Login4), Toast.LENGTH_SHORT).show() // "Registro fallido"
        }
    }
    private fun mostrarOpcionesDeImagen() {
        val opciones = arrayOf(getString(R.string.res19), getString(R.string.res20)) // "Tomar foto", "Seleccionar de galería"
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.res21)) // "Seleccionar imagen"
            .setItems(opciones) { dialog, which ->
                when (which) {
                    0 -> {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            selectedImageUri = Uri.fromFile(createImageFile())
                            cameraLauncher.launch(selectedImageUri)
                        } else {
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
                        }
                    }
                    1 -> imagePickerLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: throw IOException(getString(R.string.res10)) // "No se pudo acceder al directorio"
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            selectedImageUri = Uri.fromFile(this)
            Log.d("MainActivity2", "Archivo de imagen creado: $selectedImageUri")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mostrarOpcionesDeImagen()
            } else {
                Toast.makeText(this, getString(R.string.res22), Toast.LENGTH_SHORT).show() // "Permiso denegado para usar la cámara"
            }
        }
    }
    //calendario
    private fun mostrarFechaPicker() {
        val calendario = Calendar.getInstance()
        val year = calendario.get(Calendar.YEAR)
        val month = calendario.get(Calendar.MONTH)
        val day = calendario.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            fechaNacimiento = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            textViewSeleccionFecha.text = fechaNacimiento
        }, year, month, day)
        datePicker.show()
    }
    //hacer el registro
    private fun registrarUsuario() {
        val nombre = editNombre.text.toString().trim()
        val apellido = editApellido.text.toString().trim()
        val username = editUsername.text.toString().trim()
        val telefono = editTelefono.text.toString().trim()
        val hobbies = editHobbies.text.toString().trim()
        val email = editEmail.text.toString().trim()
        val contrasena = editContrasena.text.toString().trim()
        val confirmarContrasena = editConfirmarContrasena.text.toString().trim()
        val formaPago = spinnerFormaPago.selectedItem.toString().trim()
        val numeroTarjeta = editNumeroTarjeta.text.toString().trim()
        val fechaVencimiento = editFechaVencimiento.text.toString().trim()
        val cvc = editCVC.text.toString().trim()
        //ver que no esten vacías
        if (nombre.isEmpty() || apellido.isEmpty() || username.isEmpty() || telefono.isEmpty() ||
            hobbies.isEmpty() || email.isEmpty() || contrasena.isEmpty() || confirmarContrasena.isEmpty() ||
            formaPago.isEmpty() || fechaVencimiento.isEmpty() || cvc.isEmpty() || fechaNacimiento.isEmpty() ||
            numeroTarjeta.isEmpty()) {
            Toast.makeText(this, getString(R.string.res23), Toast.LENGTH_SHORT).show() // "Por favor, complete todos los campos."
            return
        }

        // Validar la contraseña
        if (!isContrasenaValida(contrasena)) {
            editContrasena.error = getString(R.string.res24) // "La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial"
            return
        }

        // Validar que la contraseña y la confirmación coincidan
        if (contrasena != confirmarContrasena) {
            editConfirmarContrasena.error = getString(R.string.res25) // "Las contraseñas no coinciden"
            return
        }

        // Validar que el nombre de usuario no contenga palabras prohibidas
        if (palabrasProhibidas.any { username.contains(it, ignoreCase = true) }) {
            editUsername.error = getString(R.string.res26) // "El nombre de usuario contiene palabras prohibidas"
            return
        }

        //mandar datos
        val usuarioData = "registro,$contrasena,$email,$username,$telefono,$apellido,$nombre,$nombre,$formaPago,$numeroTarjeta,$fechaVencimiento,$cvc,$fechaNacimiento,$hobbies"
        socketViewModel.sendMessage(usuarioData)

        //startActivity(intent)
        //finish()
    }

    // Función para validar la contraseña
    private fun isContrasenaValida(contrasena: String): Boolean {
        val minLength = 8
        val hasUpperCase = Regex("[A-Z]").containsMatchIn(contrasena)
        val hasNumber = Regex("\\d").containsMatchIn(contrasena)
        val hasSpecialChar = Regex("[!@#$%&*]").containsMatchIn(contrasena)

        return contrasena.length >= minLength && hasUpperCase && hasNumber && hasSpecialChar
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
    }
}

