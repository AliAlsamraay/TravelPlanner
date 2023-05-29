package com.psut.travelplanner

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import android.content.Intent
import android.database.sqlite.SQLiteDatabase


class AddEditActivity : AppCompatActivity() {

    private var travelId: Int = -1
    private lateinit var editTextTitle: EditText
    private lateinit var textDate: TextView
    private lateinit var textTime: TextView

    private lateinit var saveButton: Button

    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var timePickerDialog: TimePickerDialog

    private var selectedDate: String = ""
    private var selectedTime: String = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit)

        // Retrieve the travelId from the intent extras
        travelId = intent.getIntExtra("TRAVEL_ID", -1)

        // Initialize UI components
        editTextTitle = findViewById(R.id.editTextTitle)
        textDate = findViewById(R.id.editTextDate)
        textTime = findViewById(R.id.editTextTime)

        // Set click listeners
        textDate.setOnClickListener { showDatePicker() }
        textTime.setOnClickListener { showTimePicker() }
        saveButton = findViewById(R.id.buttonSave)
        saveButton.setOnClickListener {
            saveTravelRecord()
        }

        // Load travel details if travelId is valid
        if (travelId != -1) {
            loadTravelDetails()
        }


        // Set initial values if editing existing travel
        val isEditing = intent.getBooleanExtra("EDIT_MODE", false)
        if (isEditing) {
            loadTravelDetails()
        }
    }

    private fun loadTravelDetails() {
        val dbHelper = TravelDbHelper(this)
        val travel = dbHelper.getTravelById(travelId)

        editTextTitle.setText(travel?.title)
        textDate.setText(travel?.date)
        textTime.setText(travel?.time)
    }


    private fun saveTravelRecord() {
        val title = editTextTitle.text.toString().trim()
        val date = textDate.text.toString().trim()
        val time = textTime.text.toString().trim()

        if (title.isNotEmpty() && date.isNotEmpty() && time.isNotEmpty()) {
            val dbHelper = TravelDbHelper(this)
            val success: Boolean

            if (travelId != -1) {
                // Update existing travel record
                val travel = TravelItinerary(travelId, title, date, time)
                success = dbHelper.updateTravel(travel)
            } else {
                // Create new travel record
                val travel = TravelItinerary(travelId, title, date, time)
                travelId = dbHelper.insertTravel(travel).toInt()
                success = !travel.equals(-1)
            }

            if (success) {
                // Start the TravelAlarmService to schedule the alarm
                val intent = Intent(this, TravelAlarmService::class.java)
                intent.action = TravelAlarmService.ACTION_SET_ALARM
                intent.putExtra(TravelAlarmService.EXTRA_TRAVEL_ID, travelId)
                startService(intent)

                Toast.makeText(this, "Travel record saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to save travel record", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please enter all the fields", Toast.LENGTH_SHORT).show()
        }
    }



    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            selectedDate = "$day/${month + 1}/$year"
            textDate.setText(selectedDate)
        }

        datePickerDialog = DatePickerDialog(
            this, dateSetListener,
            year,
            month,
            day,
        )

        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
            selectedTime = String.format("%02d:%02d", hourOfDay, minute)
            textTime.setText(selectedTime)
        }, hour, minute, true)

        timePickerDialog.show()
    }


}
