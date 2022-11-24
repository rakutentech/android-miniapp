package com.rakuten.tech.mobile.miniapp.storage.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.annotation.NonNull
import androidx.annotation.VisibleForTesting
import androidx.sqlite.db.SupportSQLiteDatabase
import net.sqlcipher.database.SQLiteFullException
import java.io.IOException
import java.sql.SQLException
import java.util.stream.Collectors

@VisibleForTesting
internal const val DB_BATCH_SIZE = 100
private const val DB_HEADER_SIZE = 100
private const val PAGE_SIZE_MULTIPLIER = 3

@VisibleForTesting
internal const val TABLE_NAME = "MiniAppCache"

@VisibleForTesting
internal const val FIRST_COLUMN_NAME = "first"

@VisibleForTesting
internal const val SECOND_COLUMN_NAME = "second"

@VisibleForTesting
internal const val AUTO_VACUUM = "PRAGMA auto_vacuum = FULL"
private const val GET_ALL_ITEMS_QUERY = "select * from $TABLE_NAME"

@VisibleForTesting
internal const val DROP_TABLE_QUERY = "DROP TABLE IF EXISTS $TABLE_NAME"

@VisibleForTesting
internal const val DELETE_ALL_RECORDS_QUERY = "DELETE FROM $TABLE_NAME"

@VisibleForTesting
internal const val GET_ITEM_QUERY_PREFIX = "select * from $TABLE_NAME where $FIRST_COLUMN_NAME="

@VisibleForTesting
internal const val CREATE_TABLE_QUERY = "create table if not exists $TABLE_NAME (" +
        "$FIRST_COLUMN_NAME text primary key, $SECOND_COLUMN_NAME text)"

internal const val DATABASE_IO_ERROR = "Database I/O operation failed."
internal const val DATABASE_UNAVAILABLE_ERROR = "Database does not exist."
internal const val DATABASE_BUSY_ERROR = "Database is busy doing another operation."

@VisibleForTesting
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
    @NonNull private var maxDBSizeLimitInBytes: Long
) : MiniAppSecureDatabaseImpl(context, dbName, dbVersion) {

    @VisibleForTesting
    internal lateinit var database: SupportSQLiteDatabase

    @VisibleForTesting
    internal var miniAppDatabaseStatus = MiniAppDatabaseStatus.DEFAULT

    @VisibleForTesting
    internal fun getDatabasePageSize(): Long = database.pageSize

    @VisibleForTesting
    @Throws(IllegalStateException::class)
    @Suppress("RethrowCaughtException")
    internal fun finishAnyPendingDBTransaction() {
        try {
            if (database.inTransaction()) {
                database.endTransaction()
            }
        } catch (e: IllegalStateException) {
            throw e
        }
    }

    @VisibleForTesting
    @Throws(IllegalStateException::class)
    @Suppress("SwallowedException")
    internal fun finalize() {
        try {
            if (database.inTransaction()) {
                database.endTransaction()
            }
            if (miniAppDatabaseStatus != MiniAppDatabaseStatus.FAILED) {
                miniAppDatabaseStatus = MiniAppDatabaseStatus.READY
            }
        } catch (e: IllegalStateException) {
            // It'll always be called from finally block so ignoring.
        }
    }

    @SuppressWarnings("ExpressionBodySyntax")
    private fun getRealMaxSize(): Long {
        return getDatabaseMaxsize() - (getDatabasePageSize() * PAGE_SIZE_MULTIPLIER) - DB_HEADER_SIZE
    }

    @SuppressWarnings("ExpressionBodySyntax")
    internal fun isDatabaseBusy(): Boolean {
        return database.inTransaction()
    }

    @VisibleForTesting
    @Throws(SQLException::class)
    @Suppress("RethrowCaughtException")
    internal fun insert(contentValues: ContentValues): Long {
        var result: Long
        try {
            result = database.insert(
                TABLE_NAME,
                SQLiteDatabase.CONFLICT_REPLACE,
                contentValues
            )
        } catch (e: SQLException) {
            throw e
        }
        return result
    }

    @VisibleForTesting
    @Throws(SQLException::class)
    @Suppress("RethrowCaughtException", "TooGenericExceptionCaught")
    internal fun delete(item: String): Int {
        var totalDeleted: Int
        try {
            totalDeleted = database.delete(TABLE_NAME, "$FIRST_COLUMN_NAME='$item'", null)
        } catch (e: RuntimeException) {
            throw e
        }
        return totalDeleted
    }

    @Throws(SQLException::class)
    @Suppress("SwallowedException")
    override fun onDatabaseConfiguration(db: SupportSQLiteDatabase) {
        miniAppDatabaseStatus = try {
            db.maximumSize = maxDBSizeLimitInBytes
            db.execSQL(AUTO_VACUUM)
            MiniAppDatabaseStatus.DEFAULT
        } catch (e: SQLException) {
            MiniAppDatabaseStatus.UNAVAILABLE
        }
    }

    @Throws(SQLException::class)
    @Suppress("SwallowedException")
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun onCreateDatabase(db: SupportSQLiteDatabase) {
        miniAppDatabaseStatus = try {
            db.execSQL(CREATE_TABLE_QUERY)
            MiniAppDatabaseStatus.INITIATED
        } catch (e: SQLException) {
            MiniAppDatabaseStatus.UNAVAILABLE
        }
    }

    override fun onDatabaseReady(database: SupportSQLiteDatabase) {
        this.database = database
        miniAppDatabaseStatus = MiniAppDatabaseStatus.READY
    }

    override fun onOpenDatabase(db: SupportSQLiteDatabase) {
        miniAppDatabaseStatus = MiniAppDatabaseStatus.OPENED
    }

    @Throws(SQLException::class)
    @Suppress("SwallowedException")
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun onUpgradeDatabase(db: SupportSQLiteDatabase) {
        miniAppDatabaseStatus = try {
            db.execSQL(DROP_TABLE_QUERY)
            onCreate(db)
            MiniAppDatabaseStatus.INITIATED
        } catch (e: SQLException) {
            MiniAppDatabaseStatus.UNAVAILABLE
        }
    }

    @Throws(RuntimeException::class)
    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    override fun onDatabaseCorrupted(db: SupportSQLiteDatabase) {
        miniAppDatabaseStatus = try {
            deleteWholeDatabase(dbName)
            MiniAppDatabaseStatus.CORRUPTED
        } catch (e: RuntimeException) {
            MiniAppDatabaseStatus.UNAVAILABLE
        }
    }

    override fun isDatabaseReady(): Boolean = this::database.isInitialized

    override fun isDatabaseOpen(): Boolean = database.isOpen

    @SuppressWarnings("ExpressionBodySyntax")
    override fun isDatabaseAvailable(dbName: String): Boolean {
        val isAvailable = context.databaseList().contains(dbName)
        if (!isAvailable) {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.UNAVAILABLE
        }
        return isAvailable
    }

    override fun getDatabaseVersion(): Int = dbVersion

    override fun getDatabaseMaxsize(): Long = maxDBSizeLimitInBytes

    override fun getDatabaseStatus(): MiniAppDatabaseStatus = miniAppDatabaseStatus

    override fun resetDatabaseMaxSize(changedDBMaxSizeInBytes: Long) {
        maxDBSizeLimitInBytes = changedDBMaxSizeInBytes
    }

    override fun getDatabaseUsedSize(): Long {
        val dbFile = context.getDatabasePath(dbName)
        var fileSize = dbFile.length()
        if (dbFile.length() > maxDBSizeLimitInBytes) {
            fileSize = maxDBSizeLimitInBytes
        }
        return fileSize
    }

    @Suppress("ExpressionBodySyntax")
    override fun getDatabaseAvailableSize(): Long {
        return (getDatabaseMaxsize() - getDatabaseUsedSize())
    }

    @Suppress("ExpressionBodySyntax")
    override fun isDatabaseFull(): Boolean {
        return (getDatabaseUsedSize() >= getDatabaseMaxsize())
    }

    @Throws(IOException::class)
    @Suppress("SwallowedException")
    override fun closeDatabase() {
        try {
            if (this::database.isInitialized && database.isOpen) {
                if (miniAppDatabaseStatus == MiniAppDatabaseStatus.BUSY) {
                    miniAppDatabaseStatus = MiniAppDatabaseStatus.READY
                }
                finishAnyPendingDBTransaction()
                database.close()
            }
        } catch (e: IllegalStateException) {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FAILED
        } catch (e: IOException) {
            finishAnyPendingDBTransaction()
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FAILED
        } finally {
            if (miniAppDatabaseStatus != MiniAppDatabaseStatus.FAILED) {
                miniAppDatabaseStatus = MiniAppDatabaseStatus.CLOSED
            }
        }
    }

    @Throws(RuntimeException::class)
    @Suppress("RethrowCaughtException", "TooGenericExceptionCaught")
    override fun deleteWholeDatabase(dbName: String) {
        try {
            context.deleteDatabase(dbName)
        } catch (e: RuntimeException) {
            throw e
        }
    }

    @Throws(
        SQLException::class,
        RuntimeException::class,
        SQLiteFullException::class,
        IllegalStateException::class,
    )
    @Suppress(
        "LongParameterList",
        "LongMethod",
        "ComplexMethod",
        "NestedBlockDepth",
        "MagicNumber",
        "TooGenericExceptionCaught"
    )
    override fun insert(items: Map<String, String>): Boolean {
        var result: Long
        var isInserted = false
        try {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.BUSY
            val contentValues = ContentValues()
            if (items.size > DB_BATCH_SIZE) {
                val listOfItems = items.entries.stream().collect(Collectors.toList())
                val chunked = listOfItems.chunked(DB_BATCH_SIZE)
                chunked.forEach { outer ->
                    database.beginTransaction()
                    outer.forEach { inner ->
                        contentValues.put(FIRST_COLUMN_NAME, inner.key)
                        contentValues.put(SECOND_COLUMN_NAME, inner.value)
                        result = insert(contentValues)
                        if (result > -1) {
                            isInserted = true
                        }
                    }
                    database.setTransactionSuccessful()
                    finishAnyPendingDBTransaction()
                }
            } else {
                database.beginTransaction()
                items.entries.forEach {
                    contentValues.put(FIRST_COLUMN_NAME, it.key)
                    contentValues.put(SECOND_COLUMN_NAME, it.value)
                    result = insert(contentValues)
                    if (result > -1) {
                        isInserted = true
                    }
                }
                database.setTransactionSuccessful()
                finishAnyPendingDBTransaction()
            }
        } catch (e: IllegalStateException) {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FAILED
            throw e
        } catch (e: SQLiteFullException) {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FULL
            throw e
        } catch (e: SQLException) {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FAILED
            throw e
        } catch (e: RuntimeException) {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FAILED
            throw e
        } finally {
            finalize()
        }
        return isInserted
    }

    @Throws(
        SQLException::class,
        RuntimeException::class,
        SQLiteFullException::class,
        IllegalStateException::class,
    )
    @SuppressLint("Range")
    @Suppress("LongMethod", "LongParameterList", "TooGenericExceptionCaught")
    override fun getItem(key: String): String {
        var result = "null"
        try {
            database.beginTransaction()
            miniAppDatabaseStatus = MiniAppDatabaseStatus.BUSY
            val query = "$GET_ITEM_QUERY_PREFIX\"$key\""
            val cursor = database.query(query)
            cursor.moveToFirst()

            while (!cursor.isAfterLast) {
                result = cursor.getString(cursor.getColumnIndex(SECOND_COLUMN_NAME))
                cursor.moveToNext()
            }
            cursor.close()
            database.setTransactionSuccessful()
            finishAnyPendingDBTransaction()
        } catch (e: IllegalStateException) {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FAILED
            throw e
        } catch (e: RuntimeException) {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FAILED
            throw e
        } finally {
            finalize()
        }
        return result
    }

    @Throws(
        SQLException::class,
        RuntimeException::class,
        SQLiteFullException::class,
        IllegalStateException::class,
    )
    @SuppressLint("Range")
    @Suppress("LongMethod", "LongParameterList", "TooGenericExceptionCaught")
    override fun getAllItems(): Map<String, String> {
        var result = HashMap<String, String>()
        try {
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
            cursor.close()
            database.setTransactionSuccessful()
            finishAnyPendingDBTransaction()
        } catch (e: IllegalStateException) {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FAILED
            throw e
        } catch (e: RuntimeException) {
            finishAnyPendingDBTransaction()
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FAILED
            throw e
        } finally {
            finalize()
        }
        return result
    }

    @Throws(
        SQLException::class,
        RuntimeException::class,
        SQLiteFullException::class,
        IllegalStateException::class,
    )
    @Suppress(
        "TooGenericExceptionCaught",
        "LongParameterList",
        "LongMethod",
        "ComplexMethod",
        "NestedBlockDepth",
        "MagicNumber",
        "TooGenericExceptionCaught"
    )
    override fun deleteItems(items: Set<String>): Boolean {
        var totalDeleted: Int
        var isDeleted = false
        try {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.BUSY
            if (items.size > DB_BATCH_SIZE) {
                val listOfItems = items.stream().collect(Collectors.toList())
                val chunked = listOfItems.chunked(DB_BATCH_SIZE)
                chunked.forEach { outer ->
                    database.beginTransaction()
                    outer.forEach { item ->
                        totalDeleted = delete(item)
                        if (totalDeleted > 0) {
                            isDeleted = true
                        }
                    }
                    database.setTransactionSuccessful()
                    finishAnyPendingDBTransaction()
                }
            } else {
                database.beginTransaction()
                items.forEach { item ->
                    totalDeleted = delete(item)
                    if (totalDeleted > 0) {
                        isDeleted = true
                    }
                }
                database.setTransactionSuccessful()
                finishAnyPendingDBTransaction()
            }
            database.execSQL(AUTO_VACUUM)
        } catch (e: IllegalStateException) {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FAILED
            throw e
        } catch (e: SQLException) {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FAILED
            throw e
        } catch (e: RuntimeException) {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FAILED
            throw e
        } finally {
            finalize()
            if (!isDeleted && items.size > 1)
                isDeleted = !isDeleted
        }

        return isDeleted
    }

    @Throws(
        IOException::class,
        SQLException::class,
        RuntimeException::class,
        IllegalStateException::class
    )
    @Suppress("LongMethod", "ComplexMethod", "LongParameterList", "TooGenericExceptionCaught")
    override fun deleteAllRecords() {
        try {
            database.beginTransaction()
            miniAppDatabaseStatus = MiniAppDatabaseStatus.BUSY
            database.execSQL(DELETE_ALL_RECORDS_QUERY)
            database.setTransactionSuccessful()
            finishAnyPendingDBTransaction()
        } catch (e: IOException) {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FAILED
            throw e
        } catch (e: IllegalStateException) {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FAILED
            throw e
        } catch (e: SQLException) {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FAILED
            throw e
        } catch (e: RuntimeException) {
            miniAppDatabaseStatus = MiniAppDatabaseStatus.FAILED
            throw e
        } finally {
            finalize()
        }
    }
}
