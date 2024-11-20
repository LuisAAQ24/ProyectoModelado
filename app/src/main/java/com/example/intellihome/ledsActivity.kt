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
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Activity for controlling LED lights and handling biometric authentication
class ledsActivity : BaseActivity() {
    private lateinit var socketViewModel: SocketViewModel
    private lateinit var cuadroParpadeanteFuego: View
    private lateinit var cuadroParpadeanteTerremoto: View
    private lateinit var handlerFuego: Handler
    private lateinit var handlerTerremoto: Handler
    private lateinit var runnableFuego: Runnable
    private lateinit var runnableTerremoto: Runnable
    private var isFlashingFuego = false
    private var isFlashingTerremoto = false
    private var isVibratingTerremoto = false
    private var vibrationCounterFuego = 0
    private var vibrationCounterTerremoto = 0
    private lateinit var vibratorFuego: Vibrator  // Vibrator for fire alerts
    private lateinit var vibratorTerremoto: Vibrator
    private lateinit var btnAutenticacion: Button
    private var isGarageOpen = false
    private lateinit var textViewAgua: TextView
    private lateinit var sharedPreferences: SharedPreferences // Declara SharedPreferences
    private var storedPassword: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enables edge-to-edge display
        setContentView(R.layout.activity_leds)

        socketViewModel = ViewModelProvider(this).get(SocketViewModel::class.java)
        setupWindowInsets()

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        storedPassword = sharedPreferences.getString("password", null)
        // Button initialization

        val btnHumedad = findViewById<Button>(R.id.btnagua)
        btnHumedad.setOnClickListener {
            // Enviar mensaje al servidor para obtener la humedad
            socketViewModel.sendMessage("leds,humedad")
        }
        val btnBano2 = findViewById<Button>(R.id.btnBano2)
        val btnCuarto1 = findViewById<Button>(R.id.btnCuarto1)
        val btnSala = findViewById<Button>(R.id.btnSala)
        val btnCuarto2 = findViewById<Button>(R.id.btnCuarto2)

        val retrocederButton = findViewById<Button>(R.id.retrocederhome)
        val myImageView = findViewById<ImageView>(R.id.myImageView)


        btnAutenticacion = findViewById(R.id.btnAutenticacion)




        // Connect to socket server
        socketViewModel.connectToServer("172.18.126.148", 6060)

        // Set an image programmatically
        myImageView.setImageResource(R.drawable.casa)
        vibratorFuego = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibratorTerremoto = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        cuadroParpadeanteFuego = findViewById(R.id.cuadroParpadeante)
        cuadroParpadeanteTerremoto = findViewById(R.id.cuadroParpadeante2)
        // Initialize handlers for flashing effects
        handlerFuego = Handler(Looper.getMainLooper())
        handlerTerremoto = Handler(Looper.getMainLooper())

        btnAutenticacion.setOnClickListener {
            showBiometricPrompt()
        }

        // Define flashing behaviors
        setupFlashingRunnables()

        // Button click listeners




        // Back button to MainActivity3
        retrocederButton.setOnClickListener {
            startActivity(Intent(this, MainActivity3::class.java))
            finish()
        }

        // Observe socket responses
        socketViewModel.serverResponse.observe(this, Observer { response -> handleServerResponse(response) })

        // LED control buttons
        setupLedControlButtons(btnBano2, btnCuarto1, btnSala, btnCuarto2)


    }

    // Setup flashing runnables for fire and earthquake effects
    private fun setupFlashingRunnables() {
        runnableFuego = createFlashingRunnable(cuadroParpadeanteFuego, R.drawable.fondo_con_fuego_rojo, R.drawable.fondo_con_fuego, vibrationCounterFuego, vibratorFuego, handlerFuego)
        runnableTerremoto = createFlashingRunnable(cuadroParpadeanteTerremoto, R.drawable.fondo_terremoto_cafe, R.drawable.fondo_terremoto, vibrationCounterTerremoto, vibratorTerremoto, handlerTerremoto)
    }


    private fun createFlashingRunnable(view: View, background1: Int, background2: Int, vibrationCounter: Int, vibrator: Vibrator, handler: Handler): Runnable {
        return object : Runnable {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun run() {
                val currentBackground = if (view.tag == "fondo_blanco") background2 else background1
                view.setBackgroundResource(currentBackground)
                view.tag = if (currentBackground == background1) "fondo_blanco" else "fondo_rojo"

                // Control de vibración específico
                if (vibrationCounter % 4 == 0 && vibrator.hasVibrator()) {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                }

                handler.postDelayed(this, 100)
            }
        }
    }


    private fun toggleFlashingFuego() {
        if (isFlashingFuego) {
            handlerFuego.removeCallbacks(runnableFuego)
            cuadroParpadeanteFuego.setBackgroundResource(R.drawable.fondo_con_fuego)
            cuadroParpadeanteFuego.tag = "fondo_blanco"
            isVibratingTerremoto = false // Reiniciar estado de vibración
        } else {
            handlerFuego.post(runnableFuego)
            vibrationCounterFuego = 0 // Reiniciar contador de vibración para evitar acumulación
        }
        isFlashingFuego = !isFlashingFuego
    }

    private fun toggleFlashingTerremoto() {
        if (isFlashingTerremoto) {
            handlerTerremoto.removeCallbacks(runnableTerremoto)
            cuadroParpadeanteTerremoto.setBackgroundResource(R.drawable.fondo_terremoto)
            cuadroParpadeanteTerremoto.tag = "fondo_terremoto"
            isVibratingTerremoto = false // Reiniciar estado de vibración
        } else {
            handlerTerremoto.post(runnableTerremoto)
            vibrationCounterTerremoto = 0 // Reiniciar contador de vibración para evitar acumulación
        }
        isFlashingTerremoto = !isFlashingTerremoto
    }


    // Setup LED control buttons
    private fun setupLedControlButtons(vararg buttons: Button) {
        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                socketViewModel.sendMessage("leds,LED${index + 1}")
                Toast.makeText(this, "Comando enviado", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)

                // Cambio 3: Alterna el estado de la cochera
                isGarageOpen = !isGarageOpen

                // Cambio 4: Actualización de imagen según el estado de la cochera
                val drawableId = if (isGarageOpen) {
                    R.drawable.cochera_abrir
                } else {
                    R.drawable.cochera_cerrar
                }

                btnAutenticacion.background = ContextCompat.getDrawable(this@ledsActivity, drawableId)

                // Cambio 5: Mensaje de confirmación según el estado de la cochera
                val message = if (isGarageOpen) "Cochera abierta" else "Cochera cerrada"
                socketViewModel.sendMessage("leds,puerta")
                Toast.makeText(this@ledsActivity, message, Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(this@ledsActivity, "Error de autenticación: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(this@ledsActivity, "Autenticación fallida", Toast.LENGTH_SHORT).show()
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación Biométrica")
            .setSubtitle("Autentícate para abrir la cochera")
            .setNegativeButtonText("Cancelar")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }


    // Handle server response for alerts
// Handle server response for alerts
    private fun handleServerResponse(response: String?) {
        Log.d("ServerResponse", "Mensaje recibido: $response")
        when (response) {

            "fuego" -> {
                toggleFlashingFuego()
                Toast.makeText(this, "Fuego detectado", Toast.LENGTH_SHORT).show()
                socketViewModel.sendMessage("desastre,$storedPassword,fuego") // Enviar mensaje
            }
            "sismo" -> {
                toggleFlashingTerremoto()
                Toast.makeText(this, "Sismo detectado", Toast.LENGTH_SHORT).show()
                socketViewModel.sendMessage("desastre,$storedPassword,sismo") // Enviar mensaje

            }
            "nosismo" -> {
                toggleFlashingTerremoto() // Esto detendrá el parpadeo
                cuadroParpadeanteTerremoto.setBackgroundResource(R.drawable.fondo_terremoto) // Restablecer imagen
                Toast.makeText(this, "Sismo finalizado", Toast.LENGTH_SHORT).show()

            }
            "nofuego" -> {
                toggleFlashingFuego() // Esto detendrá el parpadeo
                cuadroParpadeanteFuego.setBackgroundResource(R.drawable.fondo_con_fuego) // Restablecer imagen
                Toast.makeText(this, "Fuego extinguido", Toast.LENGTH_SHORT).show()
            }
            else -> {
                // Si el mensaje no es uno de los casos anteriores, actualizar el TextView
                val btnHumedad = findViewById<Button>(R.id.btnagua)
                if (response?.matches(Regex("\\d+")) == true) {
                    btnHumedad.text = "Humedad: $response%"
                } else if (response != "tipo de mensaje no válido" && response != "true") {
                    btnHumedad.text = response // Cambia el texto del botón al mensaje recibido
                }
            }
        }
    }


    // Vibrate device with specified amplitude
    private fun vibrateDevice(amplitude: Int) {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, amplitude))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }

    // Clean up resources
    override fun onDestroy() {
        super.onDestroy()
        if (::handlerFuego.isInitialized) {
            handlerFuego.removeCallbacks(runnableFuego)
        }
        if (::handlerTerremoto.isInitialized) {
            handlerTerremoto.removeCallbacks(runnableTerremoto)
        }
    }


    private fun setupWindowInsets() {
        val mainView = findViewById<View>(R.id.main)
        mainView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                WindowInsetsCompat.CONSUMED // Prevent further propagation of the insets
            }
        }
    }
}




