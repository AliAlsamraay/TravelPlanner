package com.psut.travelplanner

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TravelAdapter

    private lateinit var db: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView = findViewById(R.id.recyclerView)


        adapter = TravelAdapter(this)

        // Retrieve travel records from the database
        val travelList = retrieveTravelRecords()


        // Update the adapter with the retrieved travel records
        adapter.setTravelList(travelList)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)


        //to create new Travel.
        val createButton: Button = findViewById(R.id.createButton)
        createButton.setOnClickListener {
            // Start the Add/Edit Activity for editing
            val intent = Intent(this, AddEditActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Retrieve travel records from the database
        val travelList = retrieveTravelRecords()

        // Update the adapter with the retrieved travel records
        adapter.setTravelList(travelList)

        // Notify the adapter of the data change
        adapter.notifyDataSetChanged()
    }

    private fun retrieveTravelRecords(): List<TravelItinerary> {
        val travelList = mutableListOf<TravelItinerary>()

        // Initialize and open the database
        val dbHelper = TravelDbHelper(this)
        db = dbHelper.readableDatabase

        // Query the database to retrieve travel records
        val cursor = db.query(
            TravelContract.TravelEntry.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            null
        )

        // Iterate over the cursor to retrieve travel records
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(TravelContract.TravelEntry.ID))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(TravelContract.TravelEntry.COLUMN_TITLE))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(TravelContract.TravelEntry.COLUMN_DATE))
            val time = cursor.getString(cursor.getColumnIndexOrThrow(TravelContract.TravelEntry.COLUMN_TIME))
            val travel = TravelItinerary(id, title, date, time)
            travelList.add(travel)
        }

        cursor.close()

        // Close the database when the activity is destroyed
        db.close()

        return travelList
    }

}



