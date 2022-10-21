package com.rakuten.tech.mobile.miniapp.storage.database

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.NonNull
import androidx.annotation.VisibleForTesting
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.rakuten.tech.mobile.miniapp.storage.util.MiniAppDatabaseEncryptionUtil
import net.sqlcipher.database.SupportFactory
import java.io.IOException
import java.sql.SQLException

@VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
internal enum class MiniAppDatabaseStatus {
    DEFAULT,
    INITIATED,
    OPENED,
    READY,
    CLOSED,
    UNAVAILABLE,
    BUSY,
    FAILED,
    FULL,
    CORRUPTED
}

/**
 * Database Implementation Wrapper.
 */
@Suppress("TooManyFunctions")
@VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
internal abstract class MiniAppSecureDatabaseImpl(
    @NonNull private var context: Context,
    var dbName: String, // MiniAppId will be the dbName
    var dbVersion: Int
) : SupportSQLiteOpenHelper.Callback(dbVersion) {

    private lateinit var sqliteHelper: SupportSQLiteOpenHelper

    /**
     * Method to create and open the secured database
     * for the mini apps to store the data.
     */
    @Throws(RuntimeException::class)
    @Suppress("RethrowCaughtException", "TooGenericExceptionCaught")
    internal fun createAndOpenDatabase(): Boolean {
        var status = false
        try {
            // Creating database here.
            val configuration =
                SupportSQLiteOpenHelper.Configuration.builder(context)
                    .name(dbName)
                    .callback(this)
                    .build()
            sqliteHelper = getSqliteOpenHelperFactory().create(configuration)
            // Opening database here.
            if (sqliteHelper != null) {
                onDatabaseReady(sqliteHelper.writableDatabase)
                status = true
            }
        } catch (e: RuntimeException) {
            throw e
        }
        return status
    }

    /**
     * Using SQLCipher support factory to lock and protect the database
     * with a passcode.
     * The passcode will be the MiniAppId which is the database name too.
     */
    private fun getSqliteOpenHelperFactory(): SupportSQLiteOpenHelper.Factory {
        return SupportFactory(
            MiniAppDatabaseEncryptionUtil.encryptPasscode(
                context,
                dbName // DB_NAME will be the passcode too.
            ).toByteArray()
        )
    }

    override fun onConfigure(db: SupportSQLiteDatabase) {
        onDatabaseConfiguration(db)
    }

    override fun onCreate(db: SupportSQLiteDatabase) {
        onCreateDatabase(db)
    }

    override fun onOpen(db: SupportSQLiteDatabase) {
        onOpenDatabase(db)
    }

    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgradeDatabase(db)
    }

    override fun onCorruption(db: SupportSQLiteDatabase) {
        onDatabaseCorrupted(db)
    }

    @Throws(SQLException::class)
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    abstract fun onDatabaseConfiguration(db: SupportSQLiteDatabase)

    @Throws(SQLException::class)
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    abstract fun onCreateDatabase(db: SupportSQLiteDatabase)

    @Throws(SQLException::class)
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    abstract fun onOpenDatabase(db: SupportSQLiteDatabase)

    @Throws(SQLException::class)
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    abstract fun onUpgradeDatabase(db: SupportSQLiteDatabase)

    @Throws(SQLException::class)
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    abstract fun onDatabaseCorrupted(db: SupportSQLiteDatabase)

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    abstract fun onDatabaseReady(database: SupportSQLiteDatabase)

    internal abstract fun isDatabaseOpen(): Boolean

    internal abstract fun isDatabaseAvailable(dbName: String): Boolean

    internal abstract fun getDatabaseVersion(): Int

    internal abstract fun getDatabaseMaxsize(): Long

    internal abstract fun getDatabaseStatus(): MiniAppDatabaseStatus

    internal abstract fun resetDatabaseMaxSize(changedDBMaxSize: Long)

    internal abstract fun getDatabaseUsedSize(): Long

    internal abstract fun getDatabaseAvailableSize(): Long

    internal abstract fun isDatabaseFull(): Boolean

    @Throws(IOException::class)
    internal abstract fun closeDatabase()

    internal abstract fun deleteWholeDatabase(dbName: String)

    @Throws(SQLException::class)
    internal abstract fun insert(items: Map<String, String>): Boolean

    @SuppressLint("Range")
    @Throws(RuntimeException::class)
    internal abstract fun getItem(key: String): String

    @SuppressLint("Range")
    @Throws(RuntimeException::class)
    internal abstract fun getAllItems(): Map<String, String>

    @Throws(RuntimeException::class)
    internal abstract fun deleteItems(keys: Set<String>): Boolean

    @Throws(SQLException::class, IOException::class)
    internal abstract fun deleteAllRecords()
}
