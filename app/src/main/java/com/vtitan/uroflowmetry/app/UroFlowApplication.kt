package com.vtitan.uroflowmetry.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.vtitan.uroflowmetry.util.SessionManager

class UroFlowApplication : Application() {
    var instance: UroFlowApplication? = null
    private lateinit var sessionManager: SessionManager

    override fun onCreate() {
        instance=this
        sessionManager= SessionManager(this)
        if(sessionManager.isLightModeOn()){
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )
        }else{
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        super.onCreate()
    }


    fun setAppTheme(selectedDarkLightTheme: Int) {
        if (selectedDarkLightTheme == 1) {
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )
        } else {
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

}