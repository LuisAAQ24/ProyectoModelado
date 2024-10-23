package com.example.intellihome

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.widget.Button
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.intellihome.utils.ThemeUtils

class MainActivity3 : BaseActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
        ThemeUtils.applyTheme(this)
        drawerLayout = findViewById(R.id.drawer_layout)

        // Configurar el NavigationView
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_publicar -> {
                    val publicar = Intent(this, publicarActivity::class.java)
                    startActivity(publicar)
                    true
                }
                R.id.menu_alquilar -> {
                    val alquilar = Intent(this, alquilarActivity::class.java)
                    startActivity(alquilar)
                    true
                }
                else -> false
            }
        }

        // Configuración del BottomNavigationView
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    // Cambiar a la actividad Home
                    startActivity(Intent(this, MainActivity3::class.java))
                    true
                }
                R.id.navigation_mapa -> {
                    // Cambiar a la actividad de Mapa
                    startActivity(Intent(this, MapActivity::class.java))
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

        // Botón para abrir el menú hamburguesa
        val buttonAbrirMenu: Button = findViewById(R.id.buttonAbrirMenu)
        buttonAbrirMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START) // Abre el menú hamburguesa
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
