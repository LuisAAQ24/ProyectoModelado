package com.example.intellihome

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.example.intellihome.utils.ThemeUtils
import java.util.logging.Handler

class MainActivity3 : BaseActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var socketViewModel: SocketViewModel
    private lateinit var buttonRefresh: Button
    private lateinit var buttonContainer: LinearLayout
    private lateinit var searchEditText: EditText  // EditText para la búsqueda

    // Handler y Runnable para actualizaciones periódicas
    private val handler = android.os.Handler()
    private val updateRunnable = object : Runnable {
        override fun run() {
            // Enviar mensaje al servidor para obtener alquileres
            socketViewModel.sendMessage("obtener_alquileres")
            // Programar la próxima ejecución en 10 segundos
            handler.postDelayed(this, 10000) // 10,000 ms = 10 segundos
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
        ThemeUtils.applyTheme(this)
        drawerLayout = findViewById(R.id.drawer_layout)

        // Inicializar el ViewModel
        socketViewModel = ViewModelProvider(this).get(SocketViewModel::class.java)
        socketViewModel.connectToServer("172.18.126.148", 6060)

        // Contenedor de botones
        buttonContainer = findViewById(R.id.button_container)

        // Botón de refrescar
        buttonRefresh = findViewById(R.id.button_refresh)
        buttonRefresh.setOnClickListener {
            socketViewModel.sendMessage("obtener_alquileres")
        }

        // EditText para buscar propiedades
        searchEditText = findViewById(R.id.search_edit_text)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Filtrar las propiedades según el texto ingresado
                filterProperties(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Observar la respuesta del servidor
        socketViewModel.serverResponse.observe(this, Observer { response ->
            Log.d("ServerResponse", response)
            handleServerResponse(response)
        })

        // Configuración del NavigationView
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_publicar -> {
                    startActivity(Intent(this, publicarActivity::class.java))
                    true
                }
                R.id.menu_alquilar -> {
                    startActivity(Intent(this, alquilarActivity::class.java))
                    true
                }
                R.id.menu_monitorear -> {
                    startActivity(Intent(this, ledsActivity::class.java))
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
                    startActivity(Intent(this, MainActivity3::class.java))
                    true
                }
                R.id.navigation_otros -> {
                    startActivity(Intent(this, MainActivity3::class.java))
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

        // Iniciar actualizaciones periódicas
        handler.post(updateRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Detener las actualizaciones cuando la actividad se destruya
        handler.removeCallbacks(updateRunnable)
    }

    override fun onPause() {
        super.onPause()
        // Detener las actualizaciones periódicas cuando la actividad pase a segundo plano
        handler.removeCallbacks(updateRunnable)
    }

    override fun onStop() {
        super.onStop()
        // Detener las actualizaciones periódicas cuando la actividad se detenga
        handler.removeCallbacks(updateRunnable)
    }

    private fun handleServerResponse(response: String) {
        // Limpiar los botones existentes
        buttonContainer.removeAllViews()

        // Dividir la respuesta en líneas
        val propertyList = response.split("\n")

        // Guardar las propiedades en una lista global para poder filtrarlas
        val propertyButtons = mutableListOf<Button>()

        for (property in propertyList) {
            val propertyData = property.split(",")

            if (propertyData.size >= 5) { // Validación de datos
                val location = propertyData[0].removePrefix("[").removeSuffix("]")

                // Crear un botón para cada propiedad
                val newButton = Button(this).apply {
                    text = location
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    // Aquí asignamos un color específico al fondo del botón
                    //setBackgroundColor(resources.getColor(R.color.yourColor)) // Usando un color definido en `colors.xml`
                    // O si prefieres usar un color hexadecimal:
                    setBackgroundColor(Color.parseColor("#1e81b0"))
                }

                // Listener para mostrar detalles de la propiedad
                newButton.setOnClickListener {
                    val intent = Intent(this@MainActivity3, PropertyDetailsActivity::class.java)
                    intent.putExtra("propertyDetails", property)
                    startActivity(intent)
                }

                // Agregar el botón a la lista
                propertyButtons.add(newButton)
            }
        }

        // Almacenar los botones para filtrarlos
        filterProperties(searchEditText.text.toString(), propertyButtons)
    }

    private fun filterProperties(query: String, propertyButtons: List<Button> = emptyList()) {
        buttonContainer.removeAllViews()

        val filteredButtons = if (query.isBlank()) {
            propertyButtons // Si no hay búsqueda, mostrar todas las propiedades
        } else {
            propertyButtons.filter { it.text.toString().contains(query, ignoreCase = true) }
        }

        filteredButtons.forEach { buttonContainer.addView(it) }
    }
}





