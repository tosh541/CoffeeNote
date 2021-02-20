package com.example.coffeenote

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class CoffeeNoteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        val config = RealmConfiguration.Builder()
                .allowWritesOnUiThread(true).build()
        Realm.setDefaultConfiguration(config)
    }
}