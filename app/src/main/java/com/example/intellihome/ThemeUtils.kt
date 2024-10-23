package com.example.intellihome.utils

import android.content.Context
import com.example.intellihome.R

object ThemeUtils {

    // Aplicar el tema fijo según lo especificado en tu archivo de estilos
    fun applyTheme(context: Context) {
        // Establece el tema fijo desde los recursos
        context.setTheme(R.style.AppTheme)
    }
}
