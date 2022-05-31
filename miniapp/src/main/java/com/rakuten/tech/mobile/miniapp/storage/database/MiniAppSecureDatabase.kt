package com.rakuten.tech.mobile.miniapp.storage.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import androidx.annotation.NonNull
import androidx.sqlite.db.SupportSQLiteDatabase
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

internal const val DATABASE_DOES_NOT_EXIST_ERROR = "Database does not exist."
internal const val MAX_DB_SPACE_LIMIT_REACHED_ERROR =
    "Can't insert new items. Database reached to max space limit."

/**
 * Concrete MiniApp Database Implementation.
 */
internal class MiniAppSecureDatabase(
    @NonNull private var context: Context,
    @NonNull dbName: String, // MiniAppId will be the dbName
    @NonNull dbVersion: Int,
    @NonNull private var maxDatabaseSize: Long
) : MiniAppSecureDatabaseImpl(context, dbName, dbVersion) {

    private lateinit var database: SupportSQLiteDatabase

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

    override fun onDatabaseReady(database: SupportSQLiteDatabase) {
        this.database = database
    }

    override fun isDatabaseOpen(): Boolean = database.isOpen

    override fun isDatabaseAvailable(dbName: String): Boolean {
        return context.databaseList().contains(dbName)
    }

    /**
     * For the future usage, just in case
     */
    override fun getDatabaseVersion(): Int = dbVersion

    override fun getDatabaseMaxsize(): Long = maxDatabaseSize

    /**
     * Kept For the future reference, Just in case.
     */
    override fun resetDatabaseMaxSize(changedDBMaxSize: Long) {
        maxDatabaseSize = changedDBMaxSize
    }

    override fun getDatabaseUsedSize(): Long {
        val dbFile = context.getDatabasePath(dbName)
        return dbFile.length()
    }

    /**
     * Kept For the future reference, Just in case.
     */
    override fun getDatabaseAvailableSize(): Long {
        val actualMaxSize = (
                getDatabaseMaxsize() - (
                        getDatabasePageSize() * PAGE_SIZE_MULTIPLIER) - DB_HEADER_SIZE
                )
        return actualMaxSize - getDatabaseUsedSize()
    }

    override fun isDatabaseFull(): Boolean {
        val actualMaxSize = (
                getDatabaseMaxsize() - (
                        getDatabasePageSize() * PAGE_SIZE_MULTIPLIER) - DB_HEADER_SIZE
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

    override fun deleteWholeDatabase(dbName: String) {
        context.deleteDatabase(dbName)
    }

    @Throws(SQLException::class, SQLiteException::class)
    override fun insert(items: Map<String, String>): Boolean {
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
        } catch (e: SQLiteException) {
            throw e
        }
        return (result > -1);
    }

    @SuppressLint("Range")
    @Throws(SQLException::class)
    override fun getItem(key: String): String {
        var result = "null"
        try {
            if (!isDatabaseAvailable(dbName)) {
                throw SQLException(DATABASE_DOES_NOT_EXIST_ERROR)
            }
            database.beginTransaction()
            val query = "$GET_ITEM_QUERY_PREFIX\"$key\""
            val cursor = database.query(query)
            cursor.moveToFirst();

            while (!cursor.isAfterLast) {
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

    /**
     * Kept For the future reference, Just in case.
     */
    @SuppressLint("Range")
    @Throws(SQLException::class)
    override fun getAllItems(): Map<String, String> {
        var result = HashMap<String, String>();
        try {
            if (!isDatabaseAvailable(dbName)) {
                throw SQLException(DATABASE_DOES_NOT_EXIST_ERROR)
            }
            database.beginTransaction()
            val cursor = database.query(GET_ALL_ITEMS_QUERY)
            cursor.moveToFirst();

            while (!cursor.isAfterLast) {
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

    @Throws(SQLException::class)
    override fun deleteItems(keys: Set<String>): Boolean {
        var totalDeleted: Int
        try {
            if (!isDatabaseAvailable(dbName)) {
                throw SQLException(DATABASE_DOES_NOT_EXIST_ERROR)
            }
            database.beginTransaction()
            totalDeleted =
                database.delete(TABLE_NAME, "$FIRST_COLUMN_NAME = ? ", keys.toTypedArray())
            if (totalDeleted > 0) {
                database.setTransactionSuccessful()
            }
            database.endTransaction()
        } catch (e: RuntimeException) {
            throw e
        }
        return totalDeleted > 0
    }

    @Throws(IOException::class, SQLException::class)
    override fun deleteAllRecords() {
        try {
            if (!isDatabaseAvailable(dbName)) {
                throw SQLException(DATABASE_DOES_NOT_EXIST_ERROR)
            }
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
}
