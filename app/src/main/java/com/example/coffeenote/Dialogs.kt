package com.example.coffeenote


import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import java.util.*

class DateDialog(private val onSelected: (String) -> Unit) :
        DialogFragment(), DatePickerDialog.OnDateSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(requireActivity(), R.style.DialogStyle, this, year, month, day)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        onSelected("$year/${month + 1}/$day")
    }
}
class AlertDialog(private val message: String,
                   private val okSelected: () -> Unit)
    : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity(), R.style.DialogStyle)
        builder.setTitle("CoffeeNote")
        builder.setMessage("${message}しますか?")
        builder.setPositiveButton("OK") { dialog, which ->
            okSelected()
        }
        builder.setNegativeButton("Cancel") { dialog, which -> }
        builder.setIcon(R.mipmap.ic_launcher)

        return builder.create()
    }
}