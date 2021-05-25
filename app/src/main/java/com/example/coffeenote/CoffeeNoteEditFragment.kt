package com.example.coffeenote

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.coffeenote.databinding.FragmentCoffeeNoteEditBinding
import com.google.android.material.snackbar.Snackbar
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class CoffeeNoteEditFragment : Fragment() {
    private var _binding: FragmentCoffeeNoteEditBinding? = null
    private val binding get() = _binding!!
    private val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }

    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        realm = Realm.getDefaultInstance()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentCoffeeNoteEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val args: CoffeeNoteEditFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (args.coffeeNoteId != -1L) {
            val coffeeNote = realm.where<CoffeeNote>()
                    .equalTo("id", args.coffeeNoteId).findFirst()
            binding.dateEdit.setText(DateFormat.format("yyyy/MM/dd", coffeeNote?.date))
            binding.titleEdit.setText(coffeeNote?.title)
            if (coffeeNote?.rich != null) binding.richRating.rating = coffeeNote.rich
            if (coffeeNote?.bitter != null) binding.bitterRating.rating = coffeeNote.bitter
            if (coffeeNote?.sour != null) binding.sourRating.rating = coffeeNote.sour
            if (coffeeNote?.total != null) binding.totalRating.rating = coffeeNote.total
            binding.detailEdit.setText(coffeeNote?.detail)
            binding.delete.visibility = View.VISIBLE
        } else {
            binding.delete.visibility = View.INVISIBLE
            val calendar = Calendar.getInstance()
            binding.dateEdit.setText(DateFormat.format("yyyy/MM/dd", calendar))
        }
        (activity as? MainActivity)?.setFabVisible(View.INVISIBLE)
        binding.save.setOnClickListener {
            val dialog = AlertDialog("保存", {saveCoffeeNote(it)})
            dialog.show(parentFragmentManager, "save")
        }
        binding.delete.setOnClickListener {
            val dialog = AlertDialog("削除", {deleteCoffeeNote(it)})
            dialog.show(parentFragmentManager, "delete")
        }
        binding.dateButton.setOnClickListener {
            DateDialog{ date -> binding.dateEdit.setText(date)}
                    .show(parentFragmentManager, "date_dialog")
        }
    }

    private fun saveCoffeeNote(view: View) {
        when (args.coffeeNoteId) {
            -1L -> {
                realm.executeTransaction { db: Realm ->
                    val maxId = db.where<CoffeeNote>().max("id")
                    val nextId = (maxId?.toLong() ?: 0L) + 1L
                    val coffeeNote = db.createObject<CoffeeNote>(nextId)
                    val date = "${binding.dateEdit.text}".toDate()
                    if (date != null) coffeeNote.date = date
                    coffeeNote.title = binding.titleEdit.text.toString()
                    coffeeNote.detail = binding.detailEdit.text.toString()
                    coffeeNote.rich = binding.richRating.rating
                    coffeeNote.bitter = binding.bitterRating.rating
                    coffeeNote.sour = binding.sourRating.rating
                    coffeeNote.total = binding.totalRating.rating
                }
                val maxId = realm.where<CoffeeNote>().max("id")
                val id = maxId?.toInt() ?: 0
                val webDB = Note(id,
                    binding.dateEdit.text.toString(),
                    binding.titleEdit.text.toString(),
                    binding.detailEdit.text.toString(),
                    binding.richRating.rating.toBigDecimal(),
                    binding.bitterRating.rating.toBigDecimal(),
                    binding.sourRating.rating.toBigDecimal(),
                    binding.totalRating.rating.toBigDecimal()
                )
                runBlocking { saveDate(webDB) }
                Snackbar.make(view, "保存しました", Snackbar.LENGTH_SHORT)
                        .show()
                findNavController().popBackStack()
            }
            else -> {
                realm.executeTransaction { db: Realm ->
                    val coffeeNote = db.where<CoffeeNote>()
                            .equalTo("id", args.coffeeNoteId).findFirst()
                    val date = "${binding.dateEdit.text}".toDate()
                    if (date != null) coffeeNote?.date = date
                    coffeeNote?.title = binding.titleEdit.text.toString()
                    coffeeNote?.detail = binding.titleEdit.text.toString()
                    coffeeNote?.rich = binding.richRating.rating
                    coffeeNote?.bitter = binding.bitterRating.rating
                    coffeeNote?.sour = binding.sourRating.rating
                    coffeeNote?.total = binding.totalRating.rating
                }
                Snackbar.make(view, "修正しました", Snackbar.LENGTH_SHORT)
                        .show()
                findNavController().popBackStack()
            }
        }
    }

    private fun deleteCoffeeNote(view: View) {
        realm.executeTransaction { db: Realm ->
            db.where<CoffeeNote>().equalTo("id", args.coffeeNoteId)
                    ?.findFirst()
                    ?.deleteFromRealm()
        }
        Snackbar.make(view, "削除しました", Snackbar.LENGTH_SHORT)
                .setActionTextColor(Color.YELLOW)
                .show()
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
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

    private suspend fun saveDate(data: Note) = coroutineScope {
        val response = client.post<Unit>("http://10.0.2.2:8080/coffeeNotes/") {
            contentType(ContentType.Application.Json)
            body = data
        }
        return@coroutineScope response
    }
}