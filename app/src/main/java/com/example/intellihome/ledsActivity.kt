package com.example.intellihome

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.intellihome.utils.ThemeUtils

class ledsActivity : BaseActivity() {
    private lateinit var socketViewModel: SocketViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_leds)
        ThemeUtils.applyTheme(this)
        // Configuración de insets de la ventana
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Iniciar socket
        socketViewModel = ViewModelProvider(this).get(SocketViewModel::class.java)

        // Conectar al servidor
        socketViewModel.connectToServer("172.18.51.181", 6060)

        // Configurar botones
        setupButtons()

        // Observar respuestas del servidor
        socketViewModel.serverResponse.observe(this, Observer { response ->
            handleServerResponse(response)
        })
    }

    private fun setupButtons() {
        val btnSala = findViewById<Button>(R.id.btnSala)
        val btnCocina = findViewById<Button>(R.id.btnCocina)
        val btnBano1 = findViewById<Button>(R.id.btnBano1)
        val btnBano2 = findViewById<Button>(R.id.btnBano2)
        val btnCochera = findViewById<Button>(R.id.btnCochera)

        // Configurar acciones para los botones
        btnSala.setOnClickListener { sendMessageToServer("leds,LED1") }
        btnCocina.setOnClickListener { sendMessageToServer("leds,LED2") }
        btnBano1.setOnClickListener { sendMessageToServer("leds,LED3") }
        btnBano2.setOnClickListener { sendMessageToServer("leds,LED4") }
        btnCochera.setOnClickListener { sendMessageToServer("leds,LED5") }
    }

    private fun sendMessageToServer(command: String) {
        socketViewModel.sendMessage(command)
        Toast.makeText(this, "Comando enviado: $command", Toast.LENGTH_SHORT).show()
    }

    private fun handleServerResponse(response: String?) {
        println("Response: $response")
        // Aquí puedes manejar las respuestas específicas del servidor si es necesario
    }

    override fun onDestroy() {
        super.onDestroy()
        // El ViewModel cerrará la conexión automáticamente en onCleared
    }
}
