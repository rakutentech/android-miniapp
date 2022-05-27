package com.rakuten.tech.mobile.miniapp.storage.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.annotation.NonNull
import androidx.sqlite.db.SupportSQLiteDatabase
import java.io.File
import java.io.IOException
import java.sql.SQLException

private const val DB_HEADER_SIZE = 100
private const val PAGE_SIZE_MULTIPLIER = 3
private const val TABLE_NAME = "MiniAppCache"
private const val FIRST_COLUMN_NAME = "first"
private const val SECOND_COLUMN_NAME = "second"

private const val GET_ALL_ITEMS_QUERY = "select * from $TABLE_NAME"
private const val DROP_TABLE_QUERY = "DROP TABLE IF EXISTS $TABLE_NAME"
private const val GET_ITEM_QUERY_PREFIX = "select * from $TABLE_NAME where $FIRST_COLUMN_NAME="
private const val CREATE_TABLE_QUERY = "create table if not exists $TABLE_NAME (" +
        "$FIRST_COLUMN_NAME text primary key, $SECOND_COLUMN_NAME text)"

internal const val MAX_DB_SPACE_LIMIT_REACHED_ERROR = "Can't Insert New Items. Database reached to max space limit."

/**
 * Concrete Database Implementation
 */
internal class MiniAppSecuredDatabase(
    @NonNull private var context: Context,
    dbName: String, // MiniAppId will be the dbName
    dbVersion: Int,
    private var maxDatabaseSize: Long
) : MiniAppSecuredDatabaseImpl(context, dbName, dbVersion) {

    private val database: SupportSQLiteDatabase = this.getDatabase()

    private fun getDatabasePageSize(): Long {
        return database.pageSize
    }

    override fun onCreateDatabase(db: SupportSQLiteDatabase) {
        try {
            db.execSQL(CREATE_TABLE_QUERY)
            db.maximumSize = maxDatabaseSize
        } catch (e: SQLException) {
            // Ignoring.
        }
    }

    override fun onUpgradeDatabase(db: SupportSQLiteDatabase) {
        try {
            db.execSQL(DROP_TABLE_QUERY);
            onCreate(db);
        } catch (e: SQLException) {
            // Ignoring.
        }
    }

    override fun onDatabaseCorrupted(db: SupportSQLiteDatabase) {}

    /**
     * For the future usage, just in case
     */
    override fun getDatabaseVersion(): Int = dbVersion

    override fun getDatabaseMaxsize(): Long = maxDatabaseSize

    /**
     * For the future usage, just in case
     */
    override fun resetDatabaseMaxSize(changedDBMaxSize: Long) {
        maxDatabaseSize = changedDBMaxSize
    }

    override fun getDatabaseUsedSize(): Long {
        val file = File(context.getDatabasePath(dbName).toURI())
        return file.length()
    }

    /**
     * For the future usage, just in case
     */
    override fun getDatabaseAvailableSize(): Long {
        val actualMaxSize = (
                getDatabaseMaxsize() - (
                    getDatabasePageSize() * PAGE_SIZE_MULTIPLIER
                ) - DB_HEADER_SIZE
            )
        return actualMaxSize - getDatabaseUsedSize()
    }

    override fun isDatabaseFull(): Boolean {
        val actualMaxSize = (
                getDatabaseMaxsize() - (
                        getDatabasePageSize() * PAGE_SIZE_MULTIPLIER
                    ) - DB_HEADER_SIZE
                )
        return getDatabaseUsedSize() >= actualMaxSize
    }

    @Throws(IOException::class)
    override fun closeDatabase() {
        try {
            if (database.isOpen) {
                database.close()
            }
        } catch (e: IOException) {
            throw e
        }
    }

    @Throws(SQLException::class)
    override fun insert(items: Map<String, String>) : Boolean {
        var result: Long = -1
        try {
            if (isDatabaseFull()) {
                throw SQLException(MAX_DB_SPACE_LIMIT_REACHED_ERROR)
            }
            database.beginTransaction()
            val contentValues = ContentValues()
            items.entries.forEach {
                contentValues.put(FIRST_COLUMN_NAME, it.key)
                contentValues.put(SECOND_COLUMN_NAME, it.value)
                result = database.insert(TABLE_NAME, SQLiteDatabase.CONFLICT_REPLACE, contentValues)
            }
            if (result > -1) {
                database.setTransactionSuccessful()
            }
            database.endTransaction()
        } catch (ex: SQLException) {
            throw ex
        }
        return (result > -1);
    }

    @SuppressLint("Range")
    @Throws(RuntimeException::class)
    override fun getItem(key: String) : String {
        var result = "null"
        try {
            database.beginTransaction()
            val query = "$GET_ITEM_QUERY_PREFIX\"$key\""
            val cursor = database.query(query)
            cursor.moveToFirst();

            while(!cursor.isAfterLast) {
                result = cursor.getString(cursor.getColumnIndex(SECOND_COLUMN_NAME))
                cursor.moveToNext()
            }
            if (result != "null") {
                database.setTransactionSuccessful()
            }
            database.endTransaction()
            cursor.close()
        } catch (e: RuntimeException) {
            throw e
        }
        return result
    }

    @SuppressLint("Range")
    @Throws(RuntimeException::class)
    override fun getAllItems() : Map<String, String> {
        var result = HashMap<String, String>();
        try {
            database.beginTransaction()
            val cursor = database.query(GET_ALL_ITEMS_QUERY)
            cursor.moveToFirst();

            while(!cursor.isAfterLast) {
                val first = cursor.getString(cursor.getColumnIndex(FIRST_COLUMN_NAME))
                val second = cursor.getString(cursor.getColumnIndex(SECOND_COLUMN_NAME))
                result[first] = second
                cursor.moveToNext()
            }
            if (result.isNotEmpty()) {
                database.setTransactionSuccessful()
            }
            database.endTransaction()
            cursor.close()
        } catch (e: RuntimeException) {
            throw e
        }
        return result;
    }

    @Throws(RuntimeException::class)
    override fun deleteItems(keys: Set<String>) : Boolean {
        var totalDeleted: Int = 0
        try {
            database.beginTransaction()
            totalDeleted = database.delete(TABLE_NAME, "$FIRST_COLUMN_NAME = ? ", keys.toTypedArray())
            if (totalDeleted > 0) {
                database.setTransactionSuccessful()
            }
            database.endTransaction()
        } catch (e: RuntimeException) {
            throw e
        }
        return totalDeleted > 0
    }

    @Throws(SQLException::class)
    override fun deleteAllRecords() {
        try {
            database.beginTransaction()
            database.execSQL(DROP_TABLE_QUERY);
            database.setTransactionSuccessful()
            database.endTransaction()
            closeDatabase()
        } catch (e: IOException) {
            throw e
        } catch (e: SQLException) {
            throw e
        }
    }

    override fun deleteWholeDB(dbName: String) {
        context.deleteDatabase(dbName)
    }
}
