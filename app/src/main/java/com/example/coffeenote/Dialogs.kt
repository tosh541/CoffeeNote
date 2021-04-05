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
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(requireActivity(), this, year, month, day)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        onSelected("$year/${month + 1}/$day")
    }
}
class AlertDialog(private val message: String,
                   private val okSelected: () -> Unit)
    : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
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