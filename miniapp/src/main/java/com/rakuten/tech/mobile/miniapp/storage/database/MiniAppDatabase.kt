package com.rakuten.tech.mobile.miniapp.storage.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.annotation.NonNull
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.rakuten.tech.mobile.miniapp.storage.util.DatabaseEncryptionUtil
import net.sqlcipher.database.SupportFactory
import java.io.File
import java.io.IOException
import java.sql.SQLException

private const val DB_HEADER_SIZE = 100
private const val TABLE_NAME = "MiniAppCache"
private const val FIRST_COLUMN_NAME = "first"
private const val SECOND_COLUMN_NAME = "second"

private const val GET_ALL_ITEMS_QUERY = "select * from $TABLE_NAME"
private const val DROP_TABLE_QUERY = "DROP TABLE IF EXISTS $TABLE_NAME"
private const val GET_ITEM_QUERY_PREFIX = "select * from $TABLE_NAME where $FIRST_COLUMN_NAME="
private const val CREATE_TABLE_QUERY = "create table if not exists $TABLE_NAME ($FIRST_COLUMN_NAME text, $SECOND_COLUMN_NAME text)"

internal const val MAX_DB_SPACE_LIMIT_REACHED_ERROR = "Can't Insert New Items. Database reached to max space limit."

internal class MiniAppDatabase(
    @NonNull private var context: Context,
    private var dbName: String, // MiniAppId will be the dbName
    private var dbVersion: Int,
    private var maxDatabaseSize: Long
) : SupportSQLiteOpenHelper.Callback(dbVersion) {

    private lateinit var database: SupportSQLiteDatabase
    private lateinit var sqliteHelper: SupportSQLiteOpenHelper

    init {
        createAndOpenDatabase()
    }

    private fun createAndOpenDatabase() {
        // Creating database here.
        val configuration =
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(this)
                .build()
        sqliteHelper = getSqliteOpenHelperFactory().create(configuration)
        // Opening database here.
        database = sqliteHelper.writableDatabase
    }

    /**
     * Using SQLCipher support factory to protect the database
     * with a passcode and the passcode will be encrypted and
     * stored to preferences to decrypt again.
     * The passcode will be the MiniAppId which is the database name too.
     */
    private fun getSqliteOpenHelperFactory(): SupportSQLiteOpenHelper.Factory {
        return SupportFactory(
            DatabaseEncryptionUtil.encryptDatabasePasscode(
                context,
                dbName // MiniAppId will be the passcode & dbName
            )?.toByteArray()
        )
    }

    private fun getDatabasePageSize(): Long {
        return database.pageSize
    }

    override fun onCreate(db: SupportSQLiteDatabase) {
        try {
            db.execSQL(CREATE_TABLE_QUERY)
            db.maximumSize = maxDatabaseSize
        } catch (e: SQLException) {
            // Ignoring.
        }
    }

    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            db.execSQL(DROP_TABLE_QUERY);
            onCreate(db);
        } catch (e: SQLException) {
            // Ignoring.
        }
    }

    /**
     * For the future usage, just in case
     */
    internal fun getDatabaseVersion(): Int = dbVersion

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
        val actualMaxSize = (getDatabaseMaxsize() - (getDatabasePageSize() * 2) - DB_HEADER_SIZE)
        return actualMaxSize - getDatabaseUsedSize()
    }

    internal fun isDatabaseFull(): Boolean {
        val actualMaxSize = (getDatabaseMaxsize() - (getDatabasePageSize() * 2) - DB_HEADER_SIZE)
        return getDatabaseUsedSize() >= actualMaxSize
    }

    @Throws(IOException::class)
    internal fun closeDatabase() {
        try {
            if (database.isOpen) {
                database.close()
            }
        } catch (e: IOException) {
            throw e
        }
    }

    @Throws(SQLException::class)
    internal fun insert(items: Map<String, String>) : Boolean {
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
    internal fun getItem(key: String) : String {
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
    internal fun getAllItems() : Map<String, String> {
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
    internal fun deleteItems(keys: Set<String>) : Boolean {
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
    internal fun deleteAllRecords() {
        try {
            database.beginTransaction()
            database.execSQL(DROP_TABLE_QUERY);
            database.setTransactionSuccessful()
            database.endTransaction()
            closeDatabase()
        } catch (e: SQLException) {
            throw e
        }
    }

    internal fun deleteWholeDB(dbName: String) {
        context.deleteDatabase(dbName)
    }
}
