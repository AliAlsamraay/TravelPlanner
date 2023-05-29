package com.psut.travelplanner

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import android.provider.BaseColumns

class TravelDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "travel.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = "CREATE TABLE ${TravelContract.TravelEntry.TABLE_NAME} (" +
                "${TravelContract.TravelEntry.ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "${TravelContract.TravelEntry.COLUMN_TITLE} TEXT NOT NULL, " +
                "${TravelContract.TravelEntry.COLUMN_DATE} TEXT NOT NULL, " +
                "${TravelContract.TravelEntry.COLUMN_TIME} TEXT NOT NULL)"

        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    fun getTravelById(travelId: Int): TravelItinerary? {
        val db = readableDatabase
        var travel: TravelItinerary? = null

        val projection = arrayOf(
            TravelContract.TravelEntry.COLUMN_TITLE,
            TravelContract.TravelEntry.COLUMN_DATE,
            TravelContract.TravelEntry.COLUMN_TIME
        )

        val selection = "${TravelContract.TravelEntry.ID} = ?"
        val selectionArgs = arrayOf(travelId.toString())

        val cursor = db.query(
            TravelContract.TravelEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            val title = cursor.getString(cursor.getColumnIndexOrThrow(TravelContract.TravelEntry.COLUMN_TITLE))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(TravelContract.TravelEntry.COLUMN_DATE))
            val time = cursor.getString(cursor.getColumnIndexOrThrow(TravelContract.TravelEntry.COLUMN_TIME))

            travel = TravelItinerary(travelId, title, date, time)
        }

        cursor.close()
        db.close()

        return travel
    }

    fun updateTravel(travel: TravelItinerary): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(TravelContract.TravelEntry.COLUMN_TITLE, travel.title)
            put(TravelContract.TravelEntry.COLUMN_DATE, travel.date)
            put(TravelContract.TravelEntry.COLUMN_TIME, travel.time)
        }

        val selection = "${TravelContract.TravelEntry.ID} = ?"
        val selectionArgs = arrayOf(travel.id.toString())

        val rowsAffected = db.update(
            TravelContract.TravelEntry.TABLE_NAME,
            values,
            selection,
            selectionArgs
        )

        db.close()

        return rowsAffected != 0
    }


    fun insertTravel(travel: TravelItinerary): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(TravelContract.TravelEntry.COLUMN_TITLE, travel.title)
            put(TravelContract.TravelEntry.COLUMN_DATE, travel.date)
            put(TravelContract.TravelEntry.COLUMN_TIME, travel.time)
        }

        val newRowId = db.insert(
            TravelContract.TravelEntry.TABLE_NAME,
            null,
            values
        )

        db.close()

        return newRowId
    }

}


object TravelContract {

    //table and column names
    object TravelEntry : BaseColumns {
        const val ID = "ID"
        const val TABLE_NAME = "travel"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DATE = "date"
        const val COLUMN_TIME = "time"
    }
}

data class TravelItinerary(val id:Int, val title: String, val date: String, val time: String)