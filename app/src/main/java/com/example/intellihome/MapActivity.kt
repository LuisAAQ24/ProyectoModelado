package com.example.intellihome

import android.Manifest
import android.content.Intent
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
import java.util.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var userMarker: Marker
    private lateinit var geocoder: Geocoder
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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
            if (location != null) {
                val userLatLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))

                // Añadir un marcador en la ubicación del usuario
                userMarker = mMap.addMarker(MarkerOptions().position(userLatLng).title("Tu ubicación"))!!
                userMarker.showInfoWindow()

                // Establecer listener para la selección de ubicaciones
                mMap.setOnMapClickListener { selectedLocation ->
                    userMarker.position = selectedLocation
                    userMarker.title = "Ubicación seleccionada"
                    userMarker.showInfoWindow()

                    // Obtener dirección de la ubicación seleccionada
                    val addressList = geocoder.getFromLocation(selectedLocation.latitude, selectedLocation.longitude, 1)
                    if (addressList != null) {
                        if (addressList.isNotEmpty()) {
                            val address = addressList[0]?.getAddressLine(0)

                            // Devolver la ubicación seleccionada
                            val intent = Intent()
                            intent.putExtra("ubicacion", address)
                            setResult(RESULT_OK, intent)
                            finish() // Cerrar la actividad y volver a la anterior
                        } else {
                            Toast.makeText(this, "No se pudo obtener la dirección", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación del usuario", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
