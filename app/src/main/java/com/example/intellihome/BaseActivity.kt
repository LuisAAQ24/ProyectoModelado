package com.example.intellihome

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.intellihome.utils.ThemeUtils // Asegúrate de importar ThemeUtils
import java.util.Locale

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        loadLocale()  // Cargar el idioma seleccionado
        ThemeUtils.applyTheme(this) // Aplicar el tema
        super.onCreate(savedInstanceState)
    }
    //Carga preferencias
    fun loadLocale() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val languageCode = sharedPreferences.getString("My_Lang", "es") // Por defecto 'es'
        setLocale(languageCode!!)
    }
    //aplica las preferencias
    protected fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        // Guardar el idioma en SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("My_Lang", languageCode)
        editor.apply()
    }
}
