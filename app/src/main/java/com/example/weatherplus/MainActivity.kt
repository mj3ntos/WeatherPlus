package com.example.weatherplus

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.weatherplus.utils.AirPlaneModeReceiver
import com.example.weatherplus.utils.CityPreferences
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private val apiKey: String = "5b3e54a7fc9c15a535b069c78b56cae6"
    private val airPlaneModeReceiver = AirPlaneModeReceiver()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val PERMISSION_REQUEST_CODE = 123

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (isAirplaneModeOn(this)) {
            Toast.makeText(this, "Wyłącz tryb samolotowy, aby wyszukać miasto", Toast.LENGTH_LONG)
                .show()
        }

        val cityPreferences = CityPreferences(this)
        val storedCity: Editable =
            Editable.Factory.getInstance().newEditable(cityPreferences.getCity())
        val editAddress = findViewById<EditText>(R.id.editAddress)
        editAddress.text = storedCity

        findViewById<Button>(R.id.submit).setOnClickListener {
            val city = editAddress.text

            if (city.isEmpty()) {
                Toast.makeText(this, "Wprowadź miasto!", Toast.LENGTH_LONG).show()
            } else {
                val view: View? = this.currentFocus
                if (view != null) {
                    val inputMethodManager =
                        getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
                }

                findViewById<ConstraintLayout>(R.id.enterAddress).visibility = View.GONE
                findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE


                CoroutineScope(Dispatchers.IO).launch {
                    val data = apiCall(city, apiKey)
                    withContext(Dispatchers.Main) {
                        apiDataHandler(data)
                        findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                        cityPreferences.saveCity(city.toString())
                    }
                }
            }
        }

        findViewById<Button>(R.id.tryAgain).setOnClickListener {
            findViewById<Button>(R.id.tryAgain).visibility = View.GONE
            findViewById<ConstraintLayout>(R.id.enterAddress).visibility = View.VISIBLE
        }

        findViewById<ImageView>(R.id.currentLocation).setOnClickListener {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
            } else{
                findViewById<ConstraintLayout>(R.id.enterAddress).visibility = View.GONE
                findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE

                getLastLocation()
            }
        }

        registerReceiver(
            airPlaneModeReceiver,
            IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(airPlaneModeReceiver)
    }

    private fun apiCall(
        city: Editable,
        apiKey: String,
        myLocation: Boolean = false,
        lat: Double = 0.0,
        lon: Double = 0.0
    ): String? {
        val response: String? = try {
            if (myLocation) {
                URL("https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$apiKey&units=metric&lang=pl").readText(
                    Charsets.UTF_8
                )
            } else
                URL("https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=metric&lang=pl").readText(
                    Charsets.UTF_8
                )
        } catch (e: Exception) {
            null
        }
        return response
    }

    @SuppressLint("SetTextI18n")
    private fun apiDataHandler(result: String?) {
        try {
            val jsonObj = JSONObject(result.toString())

            val city = jsonObj.getString("name")
            val main = jsonObj.getJSONObject("main")
            val sys = jsonObj.getJSONObject("sys")
            val wind = jsonObj.getJSONObject("wind")
            val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

            val temp = "${main.getInt("temp")}°"
            val feelsLike = "${main.getInt("feels_like")}°C"
            val tempMin = "${main.getInt("temp_min")}°"
            val tempMax = "${main.getInt("temp_max")}°"
            val humidity = "${main.getString("humidity")}%"
            val pressure = "${main.getString("pressure")} hPa"
            val windSpeed = "${wind.getString("speed")} km/h"
            val sunrise = sys.getLong("sunrise")
            val sunset = sys.getLong("sunset")
            val timeZone = jsonObj.getInt("timezone")
            val weatherStatus = weather.getString("description").capitalize()

            findViewById<TextView>(R.id.address).text = city
            findViewById<TextView>(R.id.temperature).text = temp
            findViewById<TextView>(R.id.weatherStatus).text = weatherStatus
            findViewById<TextView>(R.id.minMaxTemp).text = "$tempMax/$tempMin"
            findViewById<TextView>(R.id.sunrise).text = SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            ).format(Date((sunrise + timeZone) * 1000))
            findViewById<TextView>(R.id.sunset).text = SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            ).format(Date((sunset + timeZone) * 1000))
            findViewById<TextView>(R.id.wet).text = humidity
            findViewById<TextView>(R.id.wind).text = windSpeed
            findViewById<TextView>(R.id.pressure).text = pressure
            findViewById<TextView>(R.id.feelsLike).text = feelsLike

            findViewById<ConstraintLayout>(com.google.android.material.R.id.container).visibility =
                View.VISIBLE
        } catch (e: Exception) {
            Toast.makeText(this, "Wyszukiwanie nie powiodło się", Toast.LENGTH_LONG).show()
            findViewById<Button>(R.id.tryAgain).visibility = View.VISIBLE
        }
    }

    private fun isAirplaneModeOn(context: Context): Boolean {
        return Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON,
            0
        ) != 0
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation()
                } else {
                    // ...
                }
            }
            // obsluga innych kodów
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    // Zastosuj lokalizację w aplikacji
                    Log.d("Location", "Latitude: $lat, Longitude: $lon")
                    val city = findViewById<EditText>(R.id.editAddress).text

                    CoroutineScope(Dispatchers.IO).launch {
                        val data = apiCall(city, apiKey, true, lat, lon)
                        withContext(Dispatchers.Main) {
                            apiDataHandler(data)
                            findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Location", "getLastLocation failed: $e")
            }
    }


}