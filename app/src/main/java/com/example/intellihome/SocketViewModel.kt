package com.example.intellihome

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.PrintWriter
import java.net.Socket
import java.util.Scanner

class SocketViewModel : ViewModel() {
    private var socket: Socket? = null
    private var out: PrintWriter? = null
    private var `in`: Scanner? = null

    private val _serverResponse = MutableLiveData<String>()
    val serverResponse: LiveData<String> get() = _serverResponse

    fun connectToServer(ip: String, port: Int) {
        Thread {
            try {
                socket = Socket(ip, port)
                out = PrintWriter(socket?.getOutputStream(), true)
                `in` = Scanner(socket?.getInputStream())

                // Hilo para recibir respuestas del servidor
                while (true) {
                    if (`in`?.hasNextLine() == true) {
                        val response = `in`?.nextLine() // Leer respuesta del servidor
                        _serverResponse.postValue(response) //falla pero no se toca
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun sendMessage(message: String) {
        Thread {
            try {
                out?.println(message)
                out?.flush() // Asegurarse de que el mensaje se envíe inmediatamente
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
    //para cerrar todo
    override fun onCleared() {
        super.onCleared()
        try {
            out?.close()
            `in`?.close()
            socket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
