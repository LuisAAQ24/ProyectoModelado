package com.example.intellihome

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import androidx.appcompat.widget.AppCompatButton
import com.example.intellihome.utils.ThemeUtils

class MainActivity3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main3)
        ThemeUtils.applyTheme(this)
        applyCustomColors()
        // Configurar el botón
        val buttonIrASeleccionDeIdioma: Button = findViewById(R.id.buttonIrASeleccionDeIdioma)
        buttonIrASeleccionDeIdioma.setOnClickListener {
            // Iniciar LanguageSelectionActivity
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val buttonSubirCasa: Button = findViewById(R.id.buttonIrASubirCasa)

        buttonSubirCasa.setOnClickListener {
            val intent = Intent(this, MainSubirCasa::class.java)
            startActivity(intent)
        }

    }
    private fun applyCustomColors() {
        val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        val selectedColor = sharedPreferences.getInt("Selected_Color", Color.WHITE)

        // Obtener colores
        val darkColor = ThemeUtils.darkenColor(selectedColor)
        val lightColor = ThemeUtils.lightenColor(selectedColor)

        // Establecer color de fondo y de botones
        val mainLayout = findViewById<View>(R.id.main) // Cambia esto por el ID real de tu layout
        mainLayout.setBackgroundColor(lightColor)
        val buttonIrASeleccionDeIdioma: Button = findViewById(R.id.buttonIrASeleccionDeIdioma)
        buttonIrASeleccionDeIdioma.setBackgroundColor(darkColor)
    }

}
