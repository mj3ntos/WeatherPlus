package com.example.weatherplus.utils

import android.content.Context
import android.content.SharedPreferences

class CityPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("CityPrefs", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun saveCity(city: String){
        editor.putString("city", city)
        editor.apply()
    }

    fun getCity(): String{
        return sharedPreferences.getString("city", null) ?: ""
    }
}