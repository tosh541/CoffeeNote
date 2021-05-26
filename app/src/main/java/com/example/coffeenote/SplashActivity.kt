package com.example.coffeenote

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.coroutines.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class SplashActivity : AppCompatActivity() {
    private val realm = Realm.getDefaultInstance()
    private val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        val results = realm.where<CoffeeNote>().findAll()
        val response = runBlocking {
            getAll()
        }
        realm.beginTransaction()
        results.deleteAllFromRealm()
        realm.commitTransaction()
        realm.executeTransaction { db: Realm ->
            repeat(response.size) {
            val record = response[it]
            val coffeeNote = db.createObject<CoffeeNote>(record.id.toLong())
            val date = record.date.toDate()
            if (date != null) coffeeNote.date = date
            coffeeNote.title = record.title
            coffeeNote.detail = record.detail
            coffeeNote.rich = record.rich.toFloat()
            coffeeNote.bitter = record.bitter.toFloat()
            coffeeNote.sour = record.sour.toFloat()
            coffeeNote.total = record.total.toFloat()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
        client.close()
    }

    private suspend fun getAll() = coroutineScope {
        return@coroutineScope client.get<List<Note>>("http://10.0.2.2:8080/coffeeNotes/")
    }

    @SuppressLint("SimpleDateFormat")
    private fun String.toDate(pattern: String = "yyyy/MM/dd"): Date? {
        return try {
            SimpleDateFormat(pattern).parse(this)
        } catch (e: IllegalArgumentException) {
            return null
        } catch (e: ParseException) {
            return null
        }
    }
}