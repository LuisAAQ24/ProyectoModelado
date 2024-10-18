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
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeUtils.applyTheme(this)
        setContentView(R.layout.activity_main3)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    // Cambiar a la actividad Home
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.navigation_mapa -> {
                    // Cambiar a la actividad de Mapa
                    startActivity(Intent(this, LanguageSelectionActivity::class.java))
                    true
                }
                R.id.navigation_otros -> {
                    // Cambiar a otra actividad
                    startActivity(Intent(this, MainActivity2::class.java))
                    true
                }
                else -> false
            }
        }
        // Configurar el botón
        val buttonIrASeleccionDeIdioma: Button = findViewById(R.id.buttonIrASeleccionDeIdioma)
        buttonIrASeleccionDeIdioma.setOnClickListener {
            // Iniciar LanguageSelectionActivity
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            startActivity(intent)
        }
        val buttonMapa: Button = findViewById(R.id.buttonMapa)
        buttonMapa.setOnClickListener {
            // Iniciar LanguageSelectionActivity
            val mapa = Intent(this, MapActivity::class.java)
            startActivity(mapa)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    }

}
