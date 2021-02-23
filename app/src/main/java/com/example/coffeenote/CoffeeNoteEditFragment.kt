package com.example.coffeenote

import android.graphics.Color
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.coffeenote.databinding.FragmentCoffeeNoteEditBinding
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class CoffeeNoteEditFragment : Fragment() {
    private var _binding: FragmentCoffeeNoteEditBinding? = null
    private val binding get() = _binding!!

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
            binding.detailEdit.setText(coffeeNote?.detail)
            binding.delete.visibility = View.VISIBLE
        } else {
            binding.delete.visibility = View.INVISIBLE
        }
        (activity as? MainActivity)?.setFabVisible(View.INVISIBLE)
        binding.save.setOnClickListener { saveCoffeeNote(it) }
        binding.delete.setOnClickListener { deleteCoffeeNote(it) }
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
                    coffeeNote.detail = binding.titleEdit.text.toString()
                }
                Snackbar.make(view, "追加しました", Snackbar.LENGTH_SHORT)
                        .setAction("戻る") { findNavController().popBackStack() }
                        .setActionTextColor(Color.YELLOW)
                        .show()
            }
            else -> {
                realm.executeTransaction { db: Realm ->
                    val coffeeNote = db.where<CoffeeNote>()
                            .equalTo("id", args.coffeeNoteId).findFirst()
                    val date = "${binding.dateEdit.text}".toDate()
                    if (date != null) coffeeNote?.date = date
                    coffeeNote?.title = binding.titleEdit.text.toString()
                    coffeeNote?.detail = binding.titleEdit.text.toString()
                }
                Snackbar.make(view, "修正しました", Snackbar.LENGTH_SHORT)
                        .setAction("戻る") { findNavController().popBackStack() }
                        .setActionTextColor(Color.YELLOW)
                        .show()
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