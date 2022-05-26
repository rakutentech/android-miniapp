package com.rakuten.tech.mobile.miniapp.storage.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.sql.SQLException

private const val TABLE_NAME = "MiniAppCache"
private const val FIRST_COLUMN_NAME = "first"
private const val SECOND_COLUMN_NAME = "second"
private const val DB_HEADER_SIZE = 100

internal const val MAX_DB_SPACE_LIMIT_REACHED_ERROR = "Can't Insert New Items. Database reached to max space limit."

internal class MiniAppDatabase(
    var context: Context,
    var dbName: String,
    private var maxDatabaseSize: Long
) : SQLiteOpenHelper(context, dbName, null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        try {
            val query =
                "create table if not exists $TABLE_NAME ($FIRST_COLUMN_NAME text, $SECOND_COLUMN_NAME text)"
            db?.execSQL(query)
            db?.maximumSize = maxDatabaseSize
        } catch (e: SQLException) {
            // May never occur so ignoring.
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        try {
            val query = "DROP TABLE IF EXISTS $TABLE_NAME"
            db?.execSQL(query);
            onCreate(db);
        } catch (e: SQLException) {
            // May never occur so ignoring.
        }
    }

    private fun getDatabasePageSize(): Long {
        return this.readableDatabase.pageSize
    }

    internal fun getDatabaseMaxsize(): Long = maxDatabaseSize

    /**
     * For the future usage, just in case
     */
    internal fun resetDatabaseMaxSize(changedDBMaxSize: Long) {
        maxDatabaseSize = changedDBMaxSize
    }

    internal fun getDatabaseUsedSize(): Long {
        val file = File(context.getDatabasePath(dbName).toURI())
        return file.length()
    }

    /**
     * For the future usage, just in case
     */
    internal fun getDatabaseAvailableSize(): Long {
        val actualMaxSize = (getDatabaseMaxsize() - (getDatabasePageSize() * 4) - DB_HEADER_SIZE)
        return actualMaxSize - getDatabaseUsedSize()
    }

    internal fun isDatabaseFull(): Boolean {
        val actualMaxSize = (getDatabaseMaxsize() - (getDatabasePageSize() * 4) - DB_HEADER_SIZE)
        return getDatabaseUsedSize() >= actualMaxSize
    }

    @Throws(RuntimeException::class)
    internal fun insert(items: Map<String, String>) : Boolean {
        var result: Long = -1
        try {
            if (isDatabaseFull()) {
                throw RuntimeException(MAX_DB_SPACE_LIMIT_REACHED_ERROR)
            }
            val db = this.writableDatabase
            val contentValues = ContentValues()
            items.entries.forEach {
                contentValues.put(FIRST_COLUMN_NAME, it.key)
                contentValues.put(SECOND_COLUMN_NAME, it.value)
                result = db.insert(TABLE_NAME, null, contentValues)
            }
            db.close()
        } catch (e: RuntimeException) {
            throw e
        }
        return (result > -1);
    }

    @SuppressLint("Range")
    @Throws(RuntimeException::class)
    internal fun getItem(key: String) : String {
        var result = "null"
        try {
            val db = this.readableDatabase
            db.beginTransaction()

            val query = "select * from $TABLE_NAME where $FIRST_COLUMN_NAME=\"$key\""
            val cursor = db.rawQuery(query, null)
            cursor.moveToFirst();

            while(!cursor.isAfterLast) {
                result = cursor.getString(cursor.getColumnIndex(SECOND_COLUMN_NAME))
                cursor.moveToNext()
            }
            if (result != "null") {
                db.setTransactionSuccessful()
            }
            db.endTransaction()
            cursor.close()
            db.close()
        } catch (e: RuntimeException) {
            throw e
        }
        return result
    }

    @SuppressLint("Range")
    @Throws(RuntimeException::class)
    internal fun getAllItems() : Map<String, String> {
        var result = HashMap<String, String>();
        try {
            val db = this.readableDatabase
            db.beginTransaction()

            val query = "select * from $TABLE_NAME"
            val cursor = db.rawQuery(query, null)
            cursor.moveToFirst();

            while(!cursor.isAfterLast) {
                val first = cursor.getString(cursor.getColumnIndex(FIRST_COLUMN_NAME))
                val second = cursor.getString(cursor.getColumnIndex(SECOND_COLUMN_NAME))
                result[first] = second
                cursor.moveToNext()
            }
            if (result.isNotEmpty()) {
                db.setTransactionSuccessful()
            }
            db.endTransaction()
            cursor.close()
            db.close()
        } catch (e: RuntimeException) {
            throw e
        }
        return result;
    }

    @Throws(RuntimeException::class)
    internal fun deleteItems(keys: Set<String>) : Boolean {
        var totalDeleted: Int = 0
        try {
            val db = this.writableDatabase
            db.beginTransaction()
            totalDeleted = db.delete(TABLE_NAME, "$FIRST_COLUMN_NAME = ? ", keys.toTypedArray())
            if (totalDeleted > 0) {
                db.setTransactionSuccessful()
            }
            db.endTransaction()
            db.close()
        } catch (e: RuntimeException) {
            throw e
        }
        return totalDeleted > 0
    }

    @Throws(SQLException::class)
    internal fun deleteAllRecords() {
        try {
            val db = this.writableDatabase
            db.beginTransaction()
            val query = "DROP TABLE IF EXISTS $TABLE_NAME"
            db.execSQL(query);
            db.setTransactionSuccessful()
            db.endTransaction()
        } catch (e: SQLException) {
            throw e
        }
    }

    internal fun deleteWholeDB(dbName: String) {
        context.deleteDatabase(dbName)
    }
}
