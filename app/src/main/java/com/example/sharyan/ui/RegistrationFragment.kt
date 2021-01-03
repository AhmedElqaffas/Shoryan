package com.example.sharyan.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.sharyan.R
import kotlinx.android.synthetic.main.fragment_registration.*
import java.util.*


class RegistrationFragment : Fragment() {

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        instantiateNavController(view)
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
            startActivity(Intent(activity, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
        }

        // Destroying activity when the back button is clicked
        registrationBack.setOnClickListener{
            navController.popBackStack()
        }
    }

    private fun instantiateNavController(view: View){
        navController = Navigation.findNavController(view)
    }

    /**
     * This function opens a DatePicker dialog so that the user can select his/her birth date for
     * registration
     */
    private fun pickBirthDate(){
        val c = Calendar.getInstance()
        val currentYear = c.get(Calendar.YEAR)
        val currentMonth = c.get(Calendar.MONTH)
        val currentDay = c.get(Calendar.DAY_OF_MONTH)
        val dpd = DatePickerDialog(
            requireActivity(),
            { view, year, monthOfYear, dayOfMonth ->
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