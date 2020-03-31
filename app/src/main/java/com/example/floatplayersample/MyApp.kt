package com.example.floatplayersample

import android.app.Application
import kotlin.properties.Delegates

class MyApp : Application(){
    companion object{
        var instance :MyApp by Delegates.notNull()
    }
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}