package com.example.intellihome

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import android.content.SharedPreferences
import java.util.Locale

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var userMarker: Marker
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var geocoder: Geocoder
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sharedPreferences = getSharedPreferences("location_prefs", MODE_PRIVATE)
        geocoder = Geocoder(this, Locale.getDefault()) // Inicializa Geocoder

        // Obtener el mapa
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Obtener la ubicación del usuario
        getUserLocation()
    }

    private fun getUserLocation() {
        // Verificar si los permisos están concedidos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Solicitar permisos
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        // Si los permisos están concedidos, obtener la ubicación
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val userLatLng = LatLng(it.latitude, it.longitude)
                userMarker = mMap.addMarker(MarkerOptions().position(userLatLng).draggable(true))!!
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))

                // Configurar un listener para el marcador
                mMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
                    override fun onMarkerDragStart(marker: Marker) {}

                    override fun onMarkerDrag(marker: Marker) {}

                    override fun onMarkerDragEnd(marker: Marker) {
                        // Guardar la ubicación cuando el usuario termine de arrastrar el marcador
                        val newLatLng = marker.position
                        getCityFromLocation(newLatLng)
                    }
                })
            } ?: run {
                Toast.makeText(this, "No se pudo obtener la ubicación del usuario.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCityFromLocation(latLng: LatLng) {
        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        if (addresses != null) {
            if (addresses.isNotEmpty()) {
                val city = addresses[0]?.locality // Obtener el nombre de la ciudad
                Toast.makeText(this, "Ciudad seleccionada: $city", Toast.LENGTH_SHORT).show()

                // Guardar la ciudad y la ubicación
                saveLocation(latLng, city)
            } else {
                Toast.makeText(this, "No se pudo obtener la ciudad.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveLocation(latLng: LatLng, city: String?) {
        val editor = sharedPreferences.edit()
        editor.putFloat("latitude", latLng.latitude.toFloat())
        editor.putFloat("longitude", latLng.longitude.toFloat())
        editor.apply()

        // Mostrar la ciudad en el mensaje de guardado
        val message = if (city != null) {
            "Ubicación guardada: $city"
        } else {
            "Ubicación guardada: ${latLng.latitude}, ${latLng.longitude}"
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

        // Cerrar la actividad después de guardar la ubicación
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, intentar obtener la ubicación de nuevo
                getUserLocation()
            } else {
                // Permiso denegado, mostrar un mensaje al usuario
                Toast.makeText(this, "Permiso de ubicación denegado. No se puede obtener la ubicación.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
