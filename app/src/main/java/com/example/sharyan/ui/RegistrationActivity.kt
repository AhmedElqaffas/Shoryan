package com.example.sharyan.ui

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sharyan.R
import kotlinx.android.synthetic.main.activity_registration.*
import java.util.*

class RegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // Setting a click listener for the birthDatePicker button
        birthDatePicker.setOnClickListener{
            pickBirthDate()
        }


        // Adding a click listener for confirmRegistrationButton
        confirmRegistrationButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

    }

    /**
     * This function opens a DatePicker dialog so that the user can select his/her birth date for
     * registration
     */
    fun pickBirthDate(){
        val c = Calendar.getInstance()
        val currentYear = c.get(Calendar.YEAR)
        val currentMonth = c.get(Calendar.MONTH)
        val currentDay = c.get(Calendar.DAY_OF_MONTH)
        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            // Display Selected date in the Button's text
            birthDatePicker.text = "$dayOfMonth - $monthOfYear - $year"
        }, currentYear, currentMonth, currentDay)
        dpd.show()
    }
}