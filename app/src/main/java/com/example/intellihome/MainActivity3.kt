package com.example.intellihome

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.example.intellihome.utils.ThemeUtils

class MainActivity3 : BaseActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var socketViewModel: SocketViewModel
    private lateinit var buttonRefresh: Button
    private lateinit var buttonContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
        ThemeUtils.applyTheme(this)
        drawerLayout = findViewById(R.id.drawer_layout)

        // Inicializar el ViewModel para manejar la comunicación con el servidor
        socketViewModel = ViewModelProvider(this).get(SocketViewModel::class.java)

        // Conectar al servidor
        socketViewModel.connectToServer("172.18.116.167", 6060)

        // Contenedor donde se agregarán dinámicamente los botones
        buttonContainer = findViewById(R.id.button_container)

        // Configurar el botón de refrescar
        buttonRefresh = findViewById(R.id.button_refresh)
        buttonRefresh.setOnClickListener {
            // Enviar mensaje al servidor al hacer clic en refrescar
            socketViewModel.sendMessage("obtener_alquileres")
        }

        // Observar la respuesta del servidor
        socketViewModel.serverResponse.observe(this, Observer { response ->
            Log.d("ServerResponse", response)
            handleServerResponse(response)
        })

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

        // Botón para abrir el menú hamburguesa
        val buttonAbrirMenu: Button = findViewById(R.id.buttonAbrirMenu)
        buttonAbrirMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Manejar la respuesta del servidor
    private fun handleServerResponse(response: String) {
        // Limpiar los botones previamente generados
        buttonContainer.removeAllViews()

        // Separar las propiedades recibidas en líneas
        val propertyList = response.split("\n") // Separar por líneas

        for (property in propertyList) {
            // Limpiar las comillas y separar los datos por coma
            val cleanedProperty = property.replace("\"", "").trim() // Quitar comillas y espacios
            val propertyData = cleanedProperty.split(",")

            // Asegúrate de que haya suficientes datos (por ejemplo, al menos un nombre de propiedad)
            if (propertyData.isEmpty()) continue

            val descripcion = propertyData[0] // Obtener la descripción de la propiedad (alquiler)

            // Crear un nuevo botón
            val newButton = Button(this).apply {
                text = descripcion // Usar la descripción como texto del botón
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            // Establecer el listener para enviar los detalles de la propiedad a otra actividad
            newButton.setOnClickListener {
                val intent = Intent(this@MainActivity3, PropertyDetailsActivity::class.java)
                intent.putExtra("propertyDetails", cleanedProperty) // Pasar toda la propiedad
                startActivity(intent)
            }

            // Agregar el nuevo botón al contenedor
            buttonContainer.addView(newButton)
        }
    }


}


