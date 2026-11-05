package com.example.arifmetikamallar.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class CalculatorDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "calculator.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_HISTORY = "history"
        const val COLUMN_ID = "id"
        const val COLUMN_EXPRESSION = "expression"
        const val COLUMN_RESULT = "result"
        const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_HISTORY (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_EXPRESSION TEXT NOT NULL,
                $COLUMN_RESULT TEXT NOT NULL,
                $COLUMN_TIMESTAMP INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORY")
        onCreate(db)
    }

    fun insert(expression: String, result: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EXPRESSION, expression)
            put(COLUMN_RESULT, result)
            put(COLUMN_TIMESTAMP, System.currentTimeMillis())
        }
        return db.insert(TABLE_HISTORY, null, values)
    }

    fun getAllHistory(): List<HistoryItem> {
        val list = mutableListOf<HistoryItem>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_HISTORY,
            arrayOf(COLUMN_ID, COLUMN_EXPRESSION, COLUMN_RESULT, COLUMN_TIMESTAMP),
            null,
            null,
            null,
            null,
            "$COLUMN_ID DESC"
        )

        cursor.use {
            if (it.moveToFirst()) {
                val idIndex = it.getColumnIndexOrThrow(COLUMN_ID)
                val exprIndex = it.getColumnIndexOrThrow(COLUMN_EXPRESSION)
                val resIndex = it.getColumnIndexOrThrow(COLUMN_RESULT)
                val timeIndex = it.getColumnIndexOrThrow(COLUMN_TIMESTAMP)

                do {
                    val item = HistoryItem(
                        id = it.getLong(idIndex),
                        expression = it.getString(exprIndex),
                        result = it.getString(resIndex),
                        timestamp = it.getLong(timeIndex)
                    )
                    list.add(item)
                } while (it.moveToNext())
            }
        }
        return list
    }

    fun deleteById(id: Long): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_HISTORY, "$COLUMN_ID = ?", arrayOf(id.toString())) > 0
    }

    fun deleteAll(): Int {
        val db = writableDatabase
        return db.delete(TABLE_HISTORY, null, null)
    }
}
