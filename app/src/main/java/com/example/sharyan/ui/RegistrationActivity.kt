package com.example.sharyan.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        // Registering the bloodTypePicker TextView for Context Menu
        registerForContextMenu(bloodTypePicker)
        bloodTypePicker.setOnClickListener {
            it.showContextMenu()
        }

        // Adding a click listener for confirmRegistrationButton
        confirmRegistrationButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Destroying activity when the back button is clicked
        registrationBack.setOnClickListener{
            finish()
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
        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                // Display Selected date in the Button's text
                birthDatePicker.text = "$dayOfMonth/$monthOfYear/$year"
            },
            currentYear,
            currentMonth,
            currentDay
        )
        dpd.show()
    }

    /**
     * ContextMenu code for selecting the appropriate blood type
     */
    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val bloodTypesList = resources.getStringArray(R.array.blood_types)
        menu.setHeaderTitle("اختار فصيلة الدم")
        for(i in bloodTypesList.indices)
            menu.add(0, v.id, i, bloodTypesList[i].toString() )  //groupId, itemId, order, title

    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        bloodTypePicker.text = item.title

        return true
    }
}