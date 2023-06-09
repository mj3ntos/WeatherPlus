package com.example.weatherplus

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private val apiKey: String = "5b3e54a7fc9c15a535b069c78b56cae6"
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.submit).setOnClickListener {
            val city = findViewById<EditText>(R.id.editAddress).text

            if(city.isEmpty()) {
                Toast.makeText(this, "Wprowadź miasto!", Toast.LENGTH_LONG).show()
            }
            else {
                val view: View? = this.currentFocus
                if (view != null) {
                    val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
                }

                findViewById<LinearLayout>(R.id.enterAddress).visibility = View.GONE
                findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE

                CoroutineScope(Dispatchers.IO).launch {
                    val data = apiCall(city, apiKey)
                    withContext(Dispatchers.Main){
                        apiDataHandler(data)
                        findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                    }
                }
            }
        }

        findViewById<Button>(R.id.tryAgain).setOnClickListener {
            findViewById<Button>(R.id.tryAgain).visibility = View.GONE
            findViewById<LinearLayout>(R.id.enterAddress).visibility = View.VISIBLE
        }
    }

     private fun apiCall (city: Editable, apiKey: String): String? {
        val response: String? = try{
            URL("https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=metric&lang=pl").readText(Charsets.UTF_8)
        } catch (e: Exception){
            null
        }
        return response
    }

    @SuppressLint("SetTextI18n")
    private fun apiDataHandler(result: String?){
        try{
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
            findViewById<TextView>(R.id.sunrise).text = SimpleDateFormat( "HH:mm", Locale.getDefault()).format(Date( (sunrise+timeZone)*1000))
            findViewById<TextView>(R.id.sunset).text = SimpleDateFormat( "HH:mm", Locale.getDefault()).format(Date((sunset+timeZone)*1000))
            findViewById<TextView>(R.id.wet).text = humidity
            findViewById<TextView>(R.id.wind).text = windSpeed
            findViewById<TextView>(R.id.pressure).text = pressure
            findViewById<TextView>(R.id.feelsLike).text = feelsLike

            findViewById<ConstraintLayout>(com.google.android.material.R.id.container).visibility = View.VISIBLE
        }
        catch (e: Exception){
            Toast.makeText(this, "Wyszukiwanie nie powiodło się", Toast.LENGTH_LONG).show()
            findViewById<Button>(R.id.tryAgain).visibility = View.VISIBLE
        }
    }
}
