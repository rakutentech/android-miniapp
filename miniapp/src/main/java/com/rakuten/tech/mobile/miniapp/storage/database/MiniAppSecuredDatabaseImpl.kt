package com.rakuten.tech.mobile.miniapp.storage.database

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.NonNull
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.rakuten.tech.mobile.miniapp.storage.util.DatabaseEncryptionUtil
import net.sqlcipher.database.SupportFactory
import java.io.IOException
import java.sql.SQLException

/**
 * Database Implementation Wrapper
 */
abstract class MiniAppSecuredDatabaseImpl(
    @NonNull private var context: Context,
    var dbName: String, // MiniAppId will be the dbName
    var dbVersion: Int
): SupportSQLiteOpenHelper.Callback(dbVersion) {

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
     * If needed in the future then this passcode can be taken from the user
     * with a enter passcode UI screen
     */
    private fun getSqliteOpenHelperFactory(): SupportSQLiteOpenHelper.Factory {
        return SupportFactory(
            DatabaseEncryptionUtil.encryptDatabasePasscode(
                context,
                dbName // MiniAppId will be the passcode & dbName
            )?.toByteArray()
        )
    }

    /**
     * Opening Database here
     */
    internal fun getDatabase() = database

    override fun onCreate(db: SupportSQLiteDatabase) {
        onCreateDatabase(db)
    }

    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgradeDatabase(db)
    }

    override fun onCorruption(db: SupportSQLiteDatabase) {
        onDatabaseCorrupted(db)
    }

    internal abstract fun onCreateDatabase(db: SupportSQLiteDatabase)

    internal abstract fun onUpgradeDatabase(db: SupportSQLiteDatabase)

    internal abstract fun onDatabaseCorrupted(db: SupportSQLiteDatabase)

    internal abstract fun getDatabaseVersion(): Int

    internal abstract fun getDatabaseMaxsize(): Long

    internal abstract fun resetDatabaseMaxSize(changedDBMaxSize: Long)

    internal abstract fun getDatabaseUsedSize(): Long

    internal abstract fun getDatabaseAvailableSize(): Long

    internal abstract fun isDatabaseFull(): Boolean

    @Throws(IOException::class)
    internal abstract fun closeDatabase()

    @Throws(SQLException::class)
    internal abstract fun insert(items: Map<String, String>) : Boolean

    @SuppressLint("Range")
    @Throws(RuntimeException::class)
    internal abstract fun getItem(key: String) : String

    @SuppressLint("Range")
    @Throws(RuntimeException::class)
    internal abstract fun getAllItems() : Map<String, String>

    @Throws(RuntimeException::class)
    internal abstract fun deleteItems(keys: Set<String>) : Boolean

    @Throws(SQLException::class, IOException::class)
    internal abstract fun deleteAllRecords()

    internal abstract fun deleteWholeDB(dbName: String)
}
