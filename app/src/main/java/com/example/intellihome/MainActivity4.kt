package com.example.intellihome

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.intellihome.utils.ThemeUtils

class MainActivity4 : BaseActivity() {
    private lateinit var socketViewModel: SocketViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main4)
        ThemeUtils.applyTheme(this)
        //applyCustomColors()
        socketViewModel = ViewModelProvider(this).get(SocketViewModel::class.java)
        socketViewModel.connectToServer("10.0.2.2", 6060)
        socketViewModel.serverResponse.observe(this, Observer { response ->
            handleServerResponse(response)
        })
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val botonunico = findViewById<Button>(R.id.boton1)
        val email = findViewById<EditText>(R.id.EmailAddress)
        val nueva = findViewById<EditText>(R.id.nuevaContraseña)
        botonunico.setOnClickListener{
            val correo = email.text.toString()
            val contrasena = nueva.text.toString()
            if (!isContrasenaValida(contrasena)){
                Toast.makeText(this, getString(R.string.res25), Toast.LENGTH_SHORT).show()
            }
            if (correo.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, getString(R.string.res23), Toast.LENGTH_SHORT).show() // Mensaje de completar campos
            } else {
                socketViewModel.sendMessage("confirmación,$correo,$contrasena") // Enviar mensaje
                println("Yo: $correo,$contrasena")
            }

        }

    }
    private fun isContrasenaValida(contrasena: String): Boolean {
        val minLength = 8
        val hasUpperCase = Regex("[A-Z]").containsMatchIn(contrasena)
        val hasNumber = Regex("\\d").containsMatchIn(contrasena)
        val hasSpecialChar = Regex("[!@#$%&*]").containsMatchIn(contrasena)

        return contrasena.length >= minLength && hasUpperCase && hasNumber && hasSpecialChar
    }
    private fun handleServerResponse(response: String?) {
        println("Response: $response")
        if (response == "rew") {
            Toast.makeText(this, getString(R.string.con6), Toast.LENGTH_SHORT).show()
            val login = Intent(this, MainActivity::class.java)
            startActivity(login)
        } else {
            println("Fallo34")
            Toast.makeText(this, getString(R.string.Login4), Toast.LENGTH_SHORT).show() // Mensaje de autenticación fallida
        }
    }

    //private fun applyCustomColors() {
        //val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        //val selectedColor = sharedPreferences.getInt("Selected_Color", Color.WHITE)

        // Obtener colores
        //val darkColor = ThemeUtils.darkenColor(selectedColor)
        //val lightColor = ThemeUtils.lightenColor(selectedColor)

        // Establecer color de fondo y de botones
       // val mainLayout = findViewById<View>(R.id.main) // Cambia esto por el ID real de tu layout
        //mainLayout.setBackgroundColor(lightColor)

        //val botonmenu = findViewById<AppCompatButton>(R.id.btnmenu)
        //botonmenu.setBackgroundColor(darkColor)
    //}


}