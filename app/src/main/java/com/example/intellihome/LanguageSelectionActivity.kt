package com.example.intellihome

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatButton
import com.example.intellihome.utils.ThemeUtils

class LanguageSelectionActivity : AppCompatActivity() {

    private lateinit var languageSpinner: Spinner
    private lateinit var confirmButton: Button
    private lateinit var colorDisplay: TextView
    private lateinit var selectColorButton: Button
    private var selectedColor: Int = Color.WHITE // Color predeterminado

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selection)

        languageSpinner = findViewById(R.id.languageSpinner)
        confirmButton = findViewById(R.id.confirmButton)
        colorDisplay = findViewById(R.id.colorDisplay)
        selectColorButton = findViewById(R.id.selectColorButton)
        ThemeUtils.applyTheme(this)
        applyCustomColors()
        // Configurar el Spinner con los idiomas
        val languages = arrayOf("Español", "Inglés", "Húngaro", "Portugués")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter

        // Configurar el botón para seleccionar color
        selectColorButton.setOnClickListener {
            showColorPickerDialog()
        }

        // Configurar el botón de confirmación
        confirmButton.setOnClickListener {
            val selectedLanguage = languageSpinner.selectedItem.toString()
            when (selectedLanguage) {
                "Español" -> setAppLanguage("es")
                "Inglés" -> setAppLanguage("en")
                "Húngaro" -> setAppLanguage("hu")
                "Portugués" -> setAppLanguage("pt")
            }
            saveColor(selectedColor) // Guarda el color seleccionado
            // Aquí puedes añadir cualquier otra lógica que desees al confirmar cambios
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

        selectColorButton.setBackgroundColor(darkColor)
        confirmButton.setBackgroundColor(darkColor)
    }
    private fun showColorPickerDialog() {
        // Inflar el diseño del diálogo
        val dialogView = layoutInflater.inflate(R.layout.dialog_color_picker, null)
        val colorSelected = dialogView.findViewById<TextView>(R.id.colorSelected)
        val colorSeekBar = dialogView.findViewById<SeekBar>(R.id.colorSeekBar)

        // Crear el diálogo
        val dialog = AlertDialog.Builder(this)
            .setTitle("Seleccionar Color")
            .setView(dialogView)
            .create()

        // Configurar el SeekBar
        colorSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val hue = progress.toFloat()
                val color = Color.HSVToColor(floatArrayOf(hue, 1f, 1f))
                colorSelected.setBackgroundColor(color) // Cambia el fondo del TextView
                colorSelected.setTextColor(if (hue < 180) Color.WHITE else Color.BLACK) // Cambia el color del texto
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Configurar el botón de confirmación
        dialogView.findViewById<Button>(R.id.confirmColorButton).setOnClickListener {
            selectedColor = (colorSelected.background as ColorDrawable).color // Guarda el color seleccionado
            colorDisplay.setBackgroundColor(selectedColor) // Muestra el color en el TextView
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveColor(color: Int) {
        // Guardar el color seleccionado en SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putInt("Selected_Color", color)
        editor.apply()
    }

    private fun setAppLanguage(languageCode: String) {
        // Guardar el idioma seleccionado en SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("My_Lang", languageCode)
        editor.apply()

        // Reiniciar la aplicación para aplicar el nuevo idioma
        restartApp()
    }

    private fun restartApp() {
        // Cerrar la aplicación
        finishAffinity()
        // Reiniciar la actividad principal
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }


}
