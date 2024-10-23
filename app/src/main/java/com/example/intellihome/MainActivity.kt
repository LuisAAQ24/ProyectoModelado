package com.example.intellihome

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.AppCompatButton
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.intellihome.utils.ThemeUtils

class MainActivity : BaseActivity() { // Cambiado a BaseActivity
    private lateinit var socketViewModel: SocketViewModel
    private lateinit var sharedPreferences: SharedPreferences // Declara SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Llama a onCreate de BaseActivity
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Aplicar el tema
        ThemeUtils.applyTheme(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Iniciar socket
        socketViewModel = ViewModelProvider(this).get(SocketViewModel::class.java)

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        val paso1login = findViewById<EditText>(R.id.edit1)
        val paso2login = findViewById<EditText>(R.id.edit2)
        val botonmenu = findViewById<AppCompatButton>(R.id.btnmenu)
        val botonConfir = findViewById<AppCompatButton>(R.id.botonreccontrasena)
        botonConfir.setOnClickListener {
            val lanzar1 = Intent(this, MainActivity4::class.java)
            startActivity(lanzar1)
        }
        // errores
        botonmenu.setOnClickListener {
            val usuarioocoreo = paso1login.text.toString()
            val contraseña = paso2login.text.toString()
            if (usuarioocoreo.isEmpty() || contraseña.isEmpty()) {
                Toast.makeText(this, getString(R.string.res23), Toast.LENGTH_SHORT).show() // Mensaje de completar campos
            } else {
                socketViewModel.sendMessage("login,$usuarioocoreo,$contraseña") // Enviar mensaje
                println("Yo: $usuarioocoreo,$contraseña")
            }
        }

        // Configuración del botón para ir al registro
        val botonregistro = findViewById<AppCompatButton>(R.id.botregistro)
        botonregistro.setOnClickListener {
            val lanzar = Intent(this, MainActivity2::class.java)
            startActivity(lanzar)
        }

        // Iniciar conexión al servidor
        socketViewModel.connectToServer("192.168.0.114", 6060)

        // ver las respuestas del servidor
        socketViewModel.serverResponse.observe(this, Observer { response ->
            handleServerResponse(response)
        })
    }

    // Cargar el idioma cada vez que la actividad se reanuda
    override fun onResume() {
        super.onResume()
        loadLocale() // Llama a loadLocale para asegurarte de que se aplique el idioma correcto
    }

    // Manejar la respuesta del servidor
    private fun handleServerResponse(response: String?) {
        println("Response: $response")
        if (response == "true") {
            // Guardar la contraseña en SharedPreferences
            val contraseña = findViewById<EditText>(R.id.edit2).text.toString()
            val editor = sharedPreferences.edit()
            editor.putString("password", contraseña) // Guardar la contraseña
            editor.apply() // o editor.commit()

            // Iniciar la actividad de menú
            val menu = Intent(this, MainActivity3::class.java)
            startActivity(menu)
        } else {
            println("Fallo34")
            Toast.makeText(this, getString(R.string.Login4), Toast.LENGTH_SHORT).show() // Mensaje de autenticación fallida
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // El ViewModel cerrará la conexión automáticamente en onCleared
    }
}
