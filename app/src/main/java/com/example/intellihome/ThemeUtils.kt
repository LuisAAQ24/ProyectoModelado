package com.example.intellihome.utils

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import com.example.intellihome.R

object ThemeUtils {

    // Guardar el color en SharedPreferences
    fun saveColor(context: Context, color: Int) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putInt("Selected_Color", color)
        editor.apply()
    }

    // Aplicar el tema según el color seleccionado
    fun applyTheme(context: Context) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val selectedColor = sharedPreferences.getInt("Selected_Color", Color.WHITE) // Color predeterminado

        // Calcular colores
        val darkColor = darkenColor(selectedColor)
        val lightColor = lightenColor(selectedColor)

        // Aplicar colores
        context.setTheme(R.style.AppTheme)
        // Cambiar colores de fondo y botones

    }

    fun darkenColor(color: Int): Int {
        val factor = 0.7f // Cambia este valor según la oscuridad deseada
        val a = Color.alpha(color)
        val r = (Color.red(color) * factor).toInt()
        val g = (Color.green(color) * factor).toInt()
        val b = (Color.blue(color) * factor).toInt()
        return Color.argb(a, r, g, b)
    }

    fun lightenColor(color: Int): Int {
        val factor = 1.3f // mas alto mas claro
        val a = Color.alpha(color)
        val r = (Color.red(color) * factor).toInt().coerceAtMost(255)
        val g = (Color.green(color) * factor).toInt().coerceAtMost(255)
        val b = (Color.blue(color) * factor).toInt().coerceAtMost(255)
        return Color.argb(a, r, g, b)
    }
}
