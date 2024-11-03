package com.example.intellihome

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

//2
class ledsActivity : BaseActivity() {
    private lateinit var socketViewModel: SocketViewModel
    private lateinit var cuadroParpadeante: View
    private lateinit var handler: Handler
    private var isFlashing = false
    private lateinit var runnable: Runnable
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_leds)

        socketViewModel = ViewModelProvider(this).get(SocketViewModel::class.java)
        setupWindowInsets()

        // Botones
        val btnBano2 = findViewById<Button>(R.id.btnBano2)
        val btnCuarto1 = findViewById<Button>(R.id.btnCuarto1)
        val btnSala = findViewById<Button>(R.id.btnSala)
        val btnCuarto2 = findViewById<Button>(R.id.btnCuarto2)
        val btnLuces = findViewById<Button>(R.id.btnluces)
        val retrocederButton = findViewById<Button>(R.id.retrocederhome)
        val myImageView = findViewById<ImageView>(R.id.myImageView)
        val btnAutenticacion = findViewById<Button>(R.id.btnAutenticacion) // Nuevo botón de autenticación

        // Configura conexión de socket
        socketViewModel.connectToServer("172.18.116.167", 6060)
        setupButtons()

        // Imagen programática
        myImageView.setImageResource(R.drawable.casa)

        // Configuración del cuadro parpadeante
        cuadroParpadeante = findViewById(R.id.cuadroParpadeante)
        handler = Handler(Looper.getMainLooper())

        // Configura el runnable para alternar el color del cuadro
        runnable = object : Runnable {
            override fun run() {
                val color = if (cuadroParpadeante.tag == "white") Color.BLACK else Color.WHITE
                cuadroParpadeante.setBackgroundColor(color)
                cuadroParpadeante.tag = if (color == Color.WHITE) "white" else "black"
                handler.postDelayed(this, 500) // Cambia cada 500 ms
            }
        }

        // Inicia o detiene el parpadeo al presionar el botón Luces
        btnLuces.setOnClickListener {
            if (isFlashing) {
                handler.removeCallbacks(runnable) // Detiene el parpadeo
            } else {
                handler.post(runnable) // Inicia el parpadeo
            }
            isFlashing = !isFlashing // Alterna el estado de parpadeo
        }

        // Botón retroceder a MainActivity3
        retrocederButton.setOnClickListener {
            val intent = Intent(this, MainActivity3::class.java)
            startActivity(intent)
            finish()
        }

        // Observador del socket
        socketViewModel.serverResponse.observe(this, Observer { response ->
            handleServerResponse(response)
        })

        // Configuración de los botones de luces
        btnBano2.setOnClickListener {
            socketViewModel.sendMessage("leds,LED1")
            Toast.makeText(this, "Comando enviado", Toast.LENGTH_SHORT).show()
        }
        btnCuarto1.setOnClickListener {
            socketViewModel.sendMessage("leds,LED2")
            Toast.makeText(this, "Comando enviado", Toast.LENGTH_SHORT).show()
        }
        btnSala.setOnClickListener {
            socketViewModel.sendMessage("leds,LED3")
            Toast.makeText(this, "Comando enviado", Toast.LENGTH_SHORT).show()
        }
        btnCuarto2.setOnClickListener {
            socketViewModel.sendMessage("leds,LED4")
            Toast.makeText(this, "Comando enviado", Toast.LENGTH_SHORT).show()
        }

        // Configuración del BiometricPrompt para autenticación biométrica
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(applicationContext, "Éxito", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(applicationContext, "Error de autenticación: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(applicationContext, "Autenticación fallida", Toast.LENGTH_SHORT).show()
            }
        })

        // Configura el diálogo de autenticación
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación Biométrica")
            .setSubtitle("Usa tu huella para autenticarse")
            .setNegativeButtonText("Cancelar")
            .build()

        // Inicia la autenticación biométrica al presionar el botón
        btnAutenticacion.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }
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
    }

    private fun handleServerResponse(response: String?) {
        println("Response: $response")
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable) // Asegura detener el runnable al destruir la actividad
    }
}





