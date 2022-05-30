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

    private lateinit var sqliteHelper: SupportSQLiteOpenHelper

//    init {
//        createAndOpenDatabase()
//    }

    internal fun createAndOpenDatabase(): Boolean {
        // Creating database here.
        var status = false
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
        return status
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
            "test123".toByteArray()
//            DatabaseEncryptionUtil.encryptDatabasePasscode(
//                context,
//                dbName // MiniAppId will be the passcode & dbName
//            )?.toByteArray()
        )
    }

    override fun onCreate(db: SupportSQLiteDatabase) {
        onCreateDatabase(db)
    }

    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgradeDatabase(db)
    }

    override fun onCorruption(db: SupportSQLiteDatabase) {
        onDatabaseCorrupted(db)
    }

    protected abstract fun onCreateDatabase(db: SupportSQLiteDatabase)

    protected abstract fun onUpgradeDatabase(db: SupportSQLiteDatabase)

    protected abstract fun onDatabaseCorrupted(db: SupportSQLiteDatabase)

    protected abstract fun onDatabaseReady(database: SupportSQLiteDatabase)

    internal abstract fun isDatabaseAvailable(dbName: String): Boolean

    internal abstract fun getDatabaseVersion(): Int

    internal abstract fun getDatabaseMaxsize(): Long

    internal abstract fun resetDatabaseMaxSize(changedDBMaxSize: Long)

    internal abstract fun getDatabaseUsedSize(): Long

    internal abstract fun getDatabaseAvailableSize(): Long

    internal abstract fun isDatabaseFull(): Boolean

    @Throws(IOException::class)
    internal abstract fun closeDatabase()

    internal abstract fun deleteWholeDB(dbName: String)

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
}
