package com.example.weatherplus.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast

class AirPlaneModeReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == Intent.ACTION_AIRPLANE_MODE_CHANGED) {
            val isTurnedOn = Settings.Global.getInt(
                context?.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON
            ) !=0

            if(isTurnedOn) {
                Toast.makeText(context, "Tryb samolotowy jest włączony", Toast.LENGTH_SHORT).show()
            }
            else Toast.makeText(context, "Tryb samolotowy jest wyłączony", Toast.LENGTH_SHORT).show()
        }
    }
}