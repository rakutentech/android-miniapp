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

internal const val DATABASE_IO_ERROR = "Database I/O operation failed."
internal const val DATABASE_UNAVAILABLE_ERROR = "Database does not exist."
internal const val DATABASE_BUSY_ERROR = "Database is busy doing another operation."
internal const val DATABASE_SPACE_LIMIT_REACHED_ERROR =
    "Can't insert new items. Database reached to max space limit."

/**
 * Concrete MiniApp Database Implementation.
 */
@SuppressWarnings("LargeClass", "TooManyFunctions")
internal class MiniAppSecureDatabase(
    @NonNull private var context: Context,
    @NonNull dbName: String, // MiniAppId will be the dbName
    @NonNull dbVersion: Int,
    @NonNull private var maxDatabaseSize: Long
) : MiniAppSecureDatabaseImpl(context, dbName, dbVersion) {

    private lateinit var database: SupportSQLiteDatabase

    private var miniAppDatabaseStatus = MiniAppDatabaseStatus.DEFAULT

    init {
        miniAppDatabaseStatus = MiniAppDatabaseStatus.INITIATED
    }

    private fun getDatabasePageSize(): Long = database.pageSize

    @SuppressWarnings("ExpressionBodySyntax")
    private fun isDatabaseBusy(): Boolean {
        return miniAppDatabaseStatus == MiniAppDatabaseStatus.BUSY
    }

    override fun onCreateDatabase(db: SupportSQLiteDatabase) {
        try {
            db.execSQL(CREATE_TABLE_QUERY)
            db.maximumSize = maxDatabaseSize
        } catch (e: SQLException) {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FAILED
            throw e
        }
    }

    override fun onUpgradeDatabase(db: SupportSQLiteDatabase) {
        try {
            db.execSQL(DROP_TABLE_QUERY)
            onCreate(db)
        } catch (e: SQLException) {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FAILED
            throw e
        }
    }

    override fun onDatabaseCorrupted(db: SupportSQLiteDatabase) {
        context.deleteDatabase(dbName)
    }

    override fun onDatabaseReady(database: SupportSQLiteDatabase) {
        this.database = database
        MiniAppDatabaseStatus.OPENED
    }

    override fun isDatabaseOpen(): Boolean = database.isOpen

    @SuppressWarnings("ExpressionBodySyntax")
    override fun isDatabaseAvailable(dbName: String): Boolean {
        return context.databaseList().contains(dbName)
    }

    override fun getDatabaseVersion(): Int = dbVersion

    override fun getDatabaseMaxsize(): Long = maxDatabaseSize

    override fun getDatabaseStatus(): MiniAppDatabaseStatus = miniAppDatabaseStatus

    override fun resetDatabaseMaxSize(changedDBMaxSize: Long) {
        maxDatabaseSize = changedDBMaxSize
    }

    override fun getDatabaseUsedSize(): Long {
        val dbFile = context.getDatabasePath(dbName)
        return dbFile.length()
    }

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
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FAILED
            throw e
        } finally {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.CLOSED
        }
    }

    override fun deleteWholeDatabase(dbName: String) {
        context.deleteDatabase(dbName)
    }

    @Throws(SQLException::class, SQLiteException::class)
    override fun insert(items: Map<String, String>): Boolean {
        var result: Long
        var isInserted = false
        try {
            if (isDatabaseBusy()) {
                throw SQLException(DATABASE_BUSY_ERROR)
            }
            if (isDatabaseFull()) {
                throw SQLException(DATABASE_SPACE_LIMIT_REACHED_ERROR)
            }
            database.beginTransaction()
            miniAppDatabaseStatus = MiniAppDatabaseStatus.BUSY
            val contentValues = ContentValues()
            items.entries.forEach {
                contentValues.put(FIRST_COLUMN_NAME, it.key)
                contentValues.put(SECOND_COLUMN_NAME, it.value)
                result = database.insert(TABLE_NAME, SQLiteDatabase.CONFLICT_REPLACE, contentValues)
                if (result > -1) {
                    isInserted = true
                }
            }
            database.setTransactionSuccessful()
            database.endTransaction()
        } catch (e: SQLException) {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FAILED
            throw e
        } catch (e: SQLiteException) {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FAILED
            throw e
        } finally {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.READY
        }
        return isInserted
    }

    @SuppressLint("Range")
    @SuppressWarnings("TooGenericExceptionCaught")
    @Throws(SQLException::class, RuntimeException::class)
    override fun getItem(key: String): String {
        var result = "null"
        try {
            if (isDatabaseBusy()) {
                throw SQLException(DATABASE_BUSY_ERROR)
            }
            if (!isDatabaseAvailable(dbName)) {
                throw SQLException(DATABASE_UNAVAILABLE_ERROR)
            }
            database.beginTransaction()
            miniAppDatabaseStatus = MiniAppDatabaseStatus.BUSY
            val query = "$GET_ITEM_QUERY_PREFIX\"$key\""
            val cursor = database.query(query)
            cursor.moveToFirst()

            while (!cursor.isAfterLast) {
                result = cursor.getString(cursor.getColumnIndex(SECOND_COLUMN_NAME))
                cursor.moveToNext()
            }
            database.setTransactionSuccessful()
            database.endTransaction()
            cursor.close()
        } catch (e: RuntimeException) {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FAILED
            throw e
        } finally {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.READY
        }
        return result
    }

    @SuppressLint("Range")
    @SuppressWarnings("TooGenericExceptionCaught")
    @Throws(SQLException::class)
    override fun getAllItems(): Map<String, String> {
        var result = HashMap<String, String>()
        try {
            if (isDatabaseBusy()) {
                throw SQLException(DATABASE_BUSY_ERROR)
            }
            if (!isDatabaseAvailable(dbName)) {
                throw SQLException(DATABASE_UNAVAILABLE_ERROR)
            }
            database.beginTransaction()
            miniAppDatabaseStatus = MiniAppDatabaseStatus.BUSY
            val cursor = database.query(GET_ALL_ITEMS_QUERY)
            cursor.moveToFirst()

            while (!cursor.isAfterLast) {
                val first = cursor.getString(cursor.getColumnIndex(FIRST_COLUMN_NAME))
                val second = cursor.getString(cursor.getColumnIndex(SECOND_COLUMN_NAME))
                result[first] = second
                cursor.moveToNext()
            }
            database.setTransactionSuccessful()
            database.endTransaction()
            cursor.close()
        } catch (e: RuntimeException) {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FAILED
            throw e
        } finally {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.READY
        }
        return result
    }

    @Throws(SQLException::class)
    @SuppressWarnings("TooGenericExceptionCaught")
    override fun deleteItems(keys: Set<String>): Boolean {
        var totalDeleted: Int
        var isDeleted = false
        try {
            if (isDatabaseBusy()) {
                throw SQLException(DATABASE_BUSY_ERROR)
            }
            if (!isDatabaseAvailable(dbName)) {
                throw SQLException(DATABASE_UNAVAILABLE_ERROR)
            }
            database.beginTransaction()
            miniAppDatabaseStatus = MiniAppDatabaseStatus.BUSY
            keys.forEach {
                totalDeleted = database.delete(TABLE_NAME, "$FIRST_COLUMN_NAME='$it'", null)
                if (totalDeleted > 0) {
                    isDeleted = true
                }
            }
            database.setTransactionSuccessful()
            database.endTransaction()
        } catch (e: RuntimeException) {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FAILED
            throw e
        } finally {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.READY
        }
        return isDeleted
    }

    @Throws(IOException::class, SQLException::class)
    override fun deleteAllRecords() {
        try {
            if (!isDatabaseAvailable(dbName)) {
                throw SQLException(DATABASE_UNAVAILABLE_ERROR)
            }
            database.beginTransaction()
            miniAppDatabaseStatus = MiniAppDatabaseStatus.BUSY
            database.execSQL(DROP_TABLE_QUERY)
            database.setTransactionSuccessful()
            database.endTransaction()
            closeDatabase()
        } catch (e: IOException) {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FAILED
            throw e
        } catch (e: SQLException) {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FAILED
            throw e
        }
    }
}
