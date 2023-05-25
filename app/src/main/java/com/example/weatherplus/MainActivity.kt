package com.example.weatherplus

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    val apiKey: String = "5b3e54a7fc9c15a535b069c78b56cae6"
    val TAG: String = "MainActivity"
    val city: String = "Wrocław"
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CoroutineScope(Dispatchers.IO).launch {
            val data = apiCall(city, apiKey)
                withContext(Dispatchers.Main){
                    apiDataHandler(data)
                }
        }
    }

    suspend fun apiCall (city:String, apiKey: String): String? {
        val response: String? = try{
            URL("https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=metric&lang=pl").readText(Charsets.UTF_8)
        } catch (e: Exception){
            null
        }
        return response
    }

    suspend fun apiDataHandler(result: String?){
        try{
            val jsonObj = JSONObject(result)

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
            val weatherStatus = weather.getString("description").capitalize()

            findViewById<TextView>(R.id.address).text = city
            findViewById<TextView>(R.id.temperature).text = temp
            findViewById<TextView>(R.id.weatherStatus).text = weatherStatus
            findViewById<TextView>(R.id.minMaxTemp).text = "${tempMax}/${tempMin}"
            findViewById<TextView>(R.id.sunrise).text = SimpleDateFormat( "HH:mm", Locale.GERMAN).format(Date(sunrise*1000))
            findViewById<TextView>(R.id.sunset).text =SimpleDateFormat( "HH:mm", Locale.GERMAN).format(Date(sunset*1000))
            findViewById<TextView>(R.id.wet).text = humidity
            findViewById<TextView>(R.id.wind).text = windSpeed
            findViewById<TextView>(R.id.pressure).text = pressure
            findViewById<TextView>(R.id.feelsLike).text = feelsLike
        }
        catch (e: Exception){
            findViewById<TextView>(R.id.temperature).text = "NaN"

            delay(3000L)
            Log.d(TAG, "${e}")

        }
    }
}
