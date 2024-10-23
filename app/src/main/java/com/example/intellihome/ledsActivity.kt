package com.example.intellihome

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

class ledsActivity : BaseActivity() {
    private lateinit var socketViewModel: SocketViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_leds)

        setupWindowInsets()

        // Iniciar el socket y conectarse al servidor
        socketViewModel = ViewModelProvider(this).get(SocketViewModel::class.java)
        socketViewModel.connectToServer("172.18.51.181", 6060)

        setupButtons()

        // Observar las respuestas del servidor
        socketViewModel.serverResponse.observe(this, Observer { response ->
            handleServerResponse(response)
        })
    }

    private fun setupWindowInsets() {
        val mainView = findViewById<View>(R.id.main)
        mainView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        } ?: run {
            Toast.makeText(this, "Error: No se encontró la vista principal", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupButtons() {
        val btnSala = findViewById<Button>(R.id.btnSala)
        val btnCocina = findViewById<Button>(R.id.btnCocina)
        val btnBano1 = findViewById<Button>(R.id.btnBano1)
        val btnBano2 = findViewById<Button>(R.id.btnBano2)
        val btnCochera = findViewById<Button>(R.id.btnCochera)

        // Configurar las acciones para los botones
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
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}


