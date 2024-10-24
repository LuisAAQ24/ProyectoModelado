package com.example.intellihome

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView

class ledsActivity : BaseActivity() {
    private lateinit var socketViewModel: SocketViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_leds)

        setupWindowInsets()

        socketViewModel = ViewModelProvider(this).get(SocketViewModel::class.java)
        socketViewModel.connectToServer("172.18.116.167", 6060)

        setupButtons()

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
        val btnBano2= findViewById<Button>(R.id.btnBano2)
        val btnCuarto1 = findViewById<Button>(R.id.btnCuarto1)
        val btnSala = findViewById<Button>(R.id.btnSala)
        val btnCuarto2 = findViewById<Button>(R.id.btnCuarto2)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity3::class.java))
                    true
                }
                R.id.navigation_mapa -> {
                    startActivity(Intent(this, MapActivity::class.java))
                    true
                }
                R.id.navigation_otros -> {
                    startActivity(Intent(this, MainActivity2::class.java))
                    true
                }
                else -> false
            }
        }
        // Configurando el comportamiento de cada botón
        btnBano2.setOnClickListener { sendMessageToServer("leds,LED1") }
        btnCuarto1.setOnClickListener { sendMessageToServer("leds,LED2") }
        btnSala.setOnClickListener { sendMessageToServer("leds,LED3") }
        btnCuarto2.setOnClickListener { sendMessageToServer("leds,LED4") }


        val buttons = listOf(btnBano2, btnCuarto1, btnSala, btnCuarto2)

        buttons.forEach { button ->
            setupColorToggle(button)
        }

    }

    private fun setupColorToggle(button: Button) {
        var isGray = true

        button.setOnClickListener {
            val newBackground = if (isGray) {
                R.drawable.yellow_button_background
            } else {
                R.drawable.gray_button_background
            }
            button.setBackgroundResource(newBackground)
            isGray = !isGray
        }
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




