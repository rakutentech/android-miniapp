package com.rakuten.tech.mobile.miniapp.storage.database

import android.content.ContentValues
import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rakuten.tech.mobile.miniapp.TEST_MAX_STORAGE_SIZE_IN_BYTES
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TEST_STORAGE_VERSION
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteFullException
import org.amshove.kluent.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.IOException
import java.sql.SQLException

private const val BATCH_SIZE = 101
private const val IO_EXCEPTION_ERROR_MSG = "Failed with IOException."
private const val SQL_EXCEPTION_ERROR_MSG = "Failed with SQLException."
private const val RUNTIME_EXCEPTION_ERROR_MSG = "Failed with RunTimeException."
private const val ILLEGAL_STATE_EXCEPTION_ERROR_MSG = "Failed with Illegal State Exception"

@ExperimentalCoroutinesApi
@Suppress("LargeClass")
@RunWith(RobolectricTestRunner::class)
class MiniAppSecureDatabaseSpec {

    private var context: Context = mock()

    private lateinit var dbFile: File

    private lateinit var sqlException: SQLException

    private lateinit var massDB: MiniAppSecureDatabase

    private lateinit var database: SupportSQLiteDatabase

    private lateinit var runTimeException: RuntimeException

    private lateinit var massDBImpl: MiniAppSecureDatabaseImpl

    private lateinit var miniAppDBStatus: MiniAppDatabaseStatus

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        massDB = Mockito.spy(MiniAppSecureDatabase(
            context,
            TEST_MA_ID,
            TEST_STORAGE_VERSION,
            TEST_MAX_STORAGE_SIZE_IN_BYTES.toLong()
        ))
        massDB.miniAppDatabaseStatus = mock()
        massDB.database = mock(SupportSQLiteDatabase::class)

        database = mock(SupportSQLiteDatabase::class)
        massDBImpl = mock(MiniAppSecureDatabaseImpl::class)
        miniAppDBStatus = mock(MiniAppDatabaseStatus::class)
        dbFile = Mockito.spy(File("file://$TEST_MA_ID"))

        sqlException = assertThrows(SQLException::class.java) {
            throw SQLException(SQL_EXCEPTION_ERROR_MSG)
        }
        runTimeException = assertThrows(RuntimeException::class.java) {
            throw RuntimeException(RUNTIME_EXCEPTION_ERROR_MSG)
        }

        When calling context.getDatabasePath(TEST_MA_ID) itReturns dbFile
    }

    @After
    fun clear() {
        Mockito.reset(context)
        Mockito.reset(database)
        Mockito.reset(massDBImpl)
        Mockito.reset(miniAppDBStatus)
    }

    /**
     * onDatabaseConfiguration test cases
     */
    @Test
    fun `verify execSQL should be executed with AUTO_VACUUM query during onDatabaseConfiguration`() = runBlockingTest {

        massDB.onDatabaseConfiguration(database)

        verify(database, times(1)).execSQL(AUTO_VACUUM)
    }

    @Test
    fun `verify execSQL should not be executed with wrong query during onDatabaseConfiguration`() = runBlockingTest {

        massDB.onDatabaseConfiguration(database)

        verify(database, times(0)).execSQL(CREATE_TABLE_QUERY)
    }

    @Test
    fun `verify the error message when SQLException occurred during onDatabaseConfiguration`() = runBlockingTest {

        When calling database.execSQL(AUTO_VACUUM) itAnswers { sqlException }

        massDB.onDatabaseConfiguration(database)

        assertEquals(sqlException.message, SQL_EXCEPTION_ERROR_MSG)
    }

    @Test
    fun `verify the status was called when onDatabaseConfiguration is success`() = runBlockingTest {

        massDB.onDatabaseConfiguration(database)

        assertTrue(massDB.miniAppDatabaseStatus == MiniAppDatabaseStatus.DEFAULT)
    }

    /**
     * onCreateDatabase test cases
     */
    @Test
    fun `verify execSQL should be executed for CREATE_TABLE_QUERY during onCreateDatabase`() = runBlockingTest {

        massDB.onCreateDatabase(database)

        verify(database, times(1)).execSQL(CREATE_TABLE_QUERY)
    }

    @Test
    fun `verify execSQL should not be executed for wrong query during onCreateDatabase `() = runBlockingTest {

        massDB.onCreateDatabase(database)

        verify(database, times(0)).execSQL(AUTO_VACUUM)
    }

    @Test
    fun `verify the error message when SQLException occurred during onCreateDatabase`() = runBlockingTest {

        When calling database.execSQL(CREATE_TABLE_QUERY) itAnswers { sqlException }

        massDB.onCreateDatabase(database)

        assertEquals(sqlException.message, SQL_EXCEPTION_ERROR_MSG)
    }

    @Test
    fun `verify the status was called when onCreateDatabase is success`() = runBlockingTest {

        massDB.onCreateDatabase(database)

        assertTrue(massDB.miniAppDatabaseStatus == MiniAppDatabaseStatus.INITIATED)
    }

    /**
     * onDatabaseReady test cases
     */
    @Test
    fun `verify the status was called for onDatabaseReady`() = runBlockingTest {

        massDB.onDatabaseReady(database)

        assertTrue(massDB.miniAppDatabaseStatus == MiniAppDatabaseStatus.READY)
    }

    /**
     * onOpenDatabase test cases
     */
    @Test
    fun `verify the status was called for onOpenDatabase`() = runBlockingTest {

        massDB.onOpenDatabase(database)

        assertTrue(massDB.miniAppDatabaseStatus == MiniAppDatabaseStatus.OPENED)
    }

    /**
     * onUpgradeDatabase test cases
     */
    @Test
    fun `verify execSQL should be executed for DROP_TABLE_QUERY during onUpgradeDatabase`() = runBlockingTest {

        massDB.onUpgradeDatabase(database)

        verify(database, times(1)).execSQL(DROP_TABLE_QUERY)
    }

    @Test
    fun `verify execSQL should be executed for CREATE_TABLE_QUERY during onUpgradeDatabase`() = runBlockingTest {

        massDB.onUpgradeDatabase(database)

        verify(database, times(1)).execSQL(CREATE_TABLE_QUERY)
    }

    @Test
    fun `verify execSQL should not be executed for wrong query during onUpgradeDatabase `() = runBlockingTest {

        massDB.onUpgradeDatabase(database)

        verify(database, times(0)).execSQL(AUTO_VACUUM)
    }

    @Test
    fun `verify the onCreate was called when onUpgradeDatabase is success`() = runBlockingTest {

        massDB.onUpgradeDatabase(database)

        verify(database, times(2)).execSQL(any())
    }

    @Test
    fun `verify the error message when SQLException occurred during onUpgradeDatabase`() = runBlockingTest {

        When calling database.execSQL(DROP_TABLE_QUERY) itAnswers { sqlException }

        massDB.onUpgradeDatabase(database)

        assertEquals(sqlException.message, SQL_EXCEPTION_ERROR_MSG)
    }

    @Test
    fun `verify the status was called when onUpgradeDatabase is success`() = runBlockingTest {

        massDB.onUpgradeDatabase(database)

        assertTrue(massDB.miniAppDatabaseStatus == MiniAppDatabaseStatus.INITIATED)
    }

    /**
     * onDatabaseCorrupted test cases
     */
    @Test
    fun `verify the status is called when RunTimeException occurred during onDatabaseCorrupted`() = runBlockingTest {

        When calling massDB.deleteWholeDatabase(TEST_MA_ID) itThrows RuntimeException(RUNTIME_EXCEPTION_ERROR_MSG)

        massDB.onDatabaseCorrupted(database)

        assertTrue(massDB.miniAppDatabaseStatus == MiniAppDatabaseStatus.UNAVAILABLE)
    }

    @Test
    fun `verify the error message when RunTimeException occurred during onDatabaseCorrupted`() = runBlockingTest {

        When calling massDB.deleteWholeDatabase(TEST_MA_ID) itAnswers { runTimeException }

        massDB.onDatabaseCorrupted(database)

        assertEquals(runTimeException.message, RUNTIME_EXCEPTION_ERROR_MSG)
    }

    @Test
    fun `verify the status was called when onDatabaseCorrupted is success`() = runBlockingTest {

        massDB.onDatabaseCorrupted(database)

        assertTrue(massDB.miniAppDatabaseStatus == MiniAppDatabaseStatus.CORRUPTED)
    }

    @Test
    fun `verify the database deletion when onDatabaseCorrupted is success`() = runBlockingTest {

        massDB.onDatabaseCorrupted(database)

        verify(context, times(1)).deleteDatabase(any())
    }

    /**
     * isDatabaseOpen test cases
     */
    @Test
    fun `verify the database isOpen was called`() = runBlockingTest {

        massDB.isDatabaseOpen()

        verify(massDB.database, times(1)).isOpen
    }

    @Test
    fun `verify the database isOpen was false`() = runBlockingTest {

        When calling massDB.database.isOpen itReturns false

        assertFalse(massDB.isDatabaseOpen())
    }

    @Test
    fun `verify the database isOpen was true`() = runBlockingTest {

        When calling massDB.database.isOpen itReturns true

        assertTrue(massDB.isDatabaseOpen())
    }

    /**
     * isDatabaseAvailable test cases
     */
    @Test
    fun `verify the isDatabaseAvailable returns true if database file is found`() = runBlockingTest {

        val dbList = arrayOf(TEST_MA_ID, "DB-2", "DB-3")

        When calling context.databaseList() itAnswers { dbList }

        assertTrue(massDB.isDatabaseAvailable(TEST_MA_ID))
    }

    @Test
    fun `verify the isDatabaseAvailable returns false if database file is found`() = runBlockingTest {

        val dbList = arrayOf(TEST_MA_ID, "DB-2", "DB-3")

        When calling context.databaseList() itAnswers { dbList }

        assertFalse(massDB.isDatabaseAvailable("DB-4"))
    }

    @Test
    fun `verify the status was called if isDatabaseAvailable returns false`() = runBlockingTest {

        val dbList = arrayOf(TEST_MA_ID, "DB-2", "DB-3")

        When calling context.databaseList() itAnswers { dbList }

        massDB.isDatabaseAvailable("DB-4")

        assertTrue(massDB.miniAppDatabaseStatus == MiniAppDatabaseStatus.UNAVAILABLE)
    }

    /**
     * DB Version Test case
     */
    @Test
    fun `verify the correct database version has received`() {

        assertEquals(TEST_STORAGE_VERSION, massDB.getDatabaseVersion())
    }

    /**
     * DB Max Size Test case
     */
    @Test
    fun `verify the correct database max size has received`() {

        assertEquals(TEST_MAX_STORAGE_SIZE_IN_BYTES.toLong(), massDB.getDatabaseMaxsize())
    }

    /**
     * getDatabaseUsedSize Test Cases
     */
    @Test
    fun `verify getDatabasePath was called for getDatabaseUsedSize`() = runBlockingTest {

        massDB.getDatabaseUsedSize()

        verify(context, times(1)).getDatabasePath(TEST_MA_ID)
    }

    @Test
    fun `verify getDatabasePath returns the same file for getDatabaseUsedSize`() = runBlockingTest {

        massDB.getDatabaseUsedSize()

        assertEquals(dbFile, context.getDatabasePath(TEST_MA_ID))
    }

    @Test
    fun `verify the correct file length was returned for getDatabaseUsedSize`() = runBlockingTest {

        val fileSize = TEST_MAX_STORAGE_SIZE_IN_BYTES.toLong()

        When calling context.getDatabasePath(TEST_MA_ID).length() itReturns TEST_MAX_STORAGE_SIZE_IN_BYTES.toLong()

        assertEquals(fileSize, massDB.getDatabaseUsedSize())
    }

    /**
     * getDatabaseAvailableSize test case
     */
    @Test
    fun `verify getDatabaseAvailableSize returns correct available size`() {

        val usedSize = 100000L

        When calling context.getDatabasePath(TEST_MA_ID).length() itReturns usedSize

        val availableSize = massDB.getDatabaseAvailableSize()

        assertEquals(availableSize, (TEST_MAX_STORAGE_SIZE_IN_BYTES.toLong() - usedSize))
    }

    /**
     * isDatabaseFull test cases
     */
    @Test
    fun `verify isDatabaseFull return true if the size reaches to max limit`() {

        When calling context.getDatabasePath(TEST_MA_ID).length() itReturns TEST_MAX_STORAGE_SIZE_IN_BYTES.toLong()

        assertTrue(massDB.isDatabaseFull())
    }

    @Test
    fun `verify isDatabaseFull return false if the size not reached to max limit`() {

        val usedSize = 100000L

        When calling context.getDatabasePath(TEST_MA_ID).length() itReturns usedSize

        assertFalse(massDB.isDatabaseFull())
    }

    /**
     * deleteWholeDatabase test cases
     */
    @Test
    fun `verify deleteDatabase was called on context for deleteWholeDatabase`() {

        massDB.deleteWholeDatabase(TEST_MA_ID)

        Verify on context that context.deleteDatabase(TEST_MA_ID) was called
    }

    /**
     * closeDatabase test cases
     */
    @Test
    fun `verify database closed successfully`() {

        When calling massDB.database.isOpen itReturns true

        massDB.closeDatabase()

        Verify on massDB.database that massDB.database.close() was called
    }

    @Test
    fun `verify database close should not call if it is not opened`() {

        When calling massDB.database.isOpen itReturns false

        massDB.closeDatabase()

        verify(massDB.database, times(0)).close()
    }

    @Test
    fun `verify the status after database closed successfully`() {

        When calling massDB.database.isOpen itReturns true

        massDB.closeDatabase()

        assertEquals(massDB.miniAppDatabaseStatus, MiniAppDatabaseStatus.CLOSED)
    }

    @Test
    fun `verify endTransaction should be called if database still haa any pending transactions during database close`() {

        When calling massDB.database.inTransaction() itReturns true

        When calling massDB.database.isOpen itReturns true

        massDB.closeDatabase()

        verify(massDB.database, times(1)).endTransaction()
    }

    @Test
    fun `verify endTransaction should not be called if database has no pending transaction during database close`() {

        When calling massDB.database.inTransaction() itReturns false

        When calling massDB.database.isOpen itReturns true

        massDB.closeDatabase()

        verify(massDB.database, times(0)).endTransaction()
    }

    @Test
    fun `verify the status if IOException occurred during database close`() {

        When calling massDB.database.close() itThrows IOException(IO_EXCEPTION_ERROR_MSG)

        When calling massDB.database.isOpen itReturns true

        massDB.closeDatabase()

        assertEquals(massDB.miniAppDatabaseStatus, MiniAppDatabaseStatus.FAILED)
    }

    @Test
    fun `verify the finishAnyPendingDBTransaction was called if IOException occurred during database close`() {

        When calling massDB.database.close() itThrows IOException(IO_EXCEPTION_ERROR_MSG)

        When calling massDB.database.isOpen itReturns true

        massDB.closeDatabase()

        verify(massDB, times(2)).finishAnyPendingDBTransaction()
    }

    @Test
    fun `verify the database close should not be called if IllegalStateException occurred during database close`() {

        When calling massDB.finishAnyPendingDBTransaction() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        When calling massDB.database.isOpen itReturns true

        massDB.closeDatabase()

        verify(database, times(0)).close()
    }

    @Test
    fun `verify the status if IllegalStateException occurred during database close`() {

        When calling massDB.finishAnyPendingDBTransaction() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        When calling massDB.database.isOpen itReturns true

        massDB.closeDatabase()

        assertEquals(massDB.miniAppDatabaseStatus, MiniAppDatabaseStatus.FAILED)
    }

    /**
     * insert above batch size test cases
     */
    @Test
    fun `verify the insert should throw DB full exception while inserting when batch size is above limits`() = runBlockingTest {

        val items =  Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        When calling items.size itReturns BATCH_SIZE

        When calling context.getDatabasePath(TEST_MA_ID).length() itReturns TEST_MAX_STORAGE_SIZE_IN_BYTES.toLong()

        val sqlFullException = assertThrows(SQLiteFullException::class.java) {
            massDB.insert(items)
        }

        assertTrue(sqlFullException.message == DATABASE_SPACE_LIMIT_REACHED_ERROR)
    }

    @Test
    fun `verify all the items are successfully inserted above batch size`() = runBlockingTest {

        val sizeBelowMaxLimit = 100000L

        var pairs: ArrayList<Pair<String, String>> = ArrayList()
        for(i in 1..BATCH_SIZE) {
            pairs.add(Pair("a-$i", "b-$i"))
        }

        val items = Mockito.spy(pairs.associate { Pair(it.first, it.second) })

        val contentValues = ContentValues()

        items.entries.forEach {
            contentValues.put(FIRST_COLUMN_NAME, it.key)
            contentValues.put(SECOND_COLUMN_NAME, it.value)
        }

        When calling items.size itReturns BATCH_SIZE

        When calling context.getDatabasePath(TEST_MA_ID).length() itReturns sizeBelowMaxLimit

        massDB.insert(items)

        verify(massDB, times(BATCH_SIZE)).insert(contentValues)
    }

    @Test
    fun `verify the insert successfully passes when batch size is above limits`() = runBlockingTest {

        val sizeBelowMaxLimit = 100000L

        val items =  Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        When calling items.size itReturns BATCH_SIZE

        When calling context.getDatabasePath(TEST_MA_ID).length() itReturns sizeBelowMaxLimit

        assertTrue(massDB.insert(items))
    }

    @Test
    fun `verify the finalize was called insert successfully passes for above batch size`() = runBlockingTest {

        val sizeBelowMaxLimit = 100000L

        val items =  Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        When calling items.size itReturns BATCH_SIZE

        When calling context.getDatabasePath(TEST_MA_ID).length() itReturns sizeBelowMaxLimit

        massDB.insert(items)

        verify(massDB, times(1)).finalize()
    }

    @Test
    fun `verify the status after insert successfully passes for above batch size`() = runBlockingTest {

        val sizeBelowMaxLimit = 100000L

        val items =  Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        When calling items.size itReturns BATCH_SIZE

        When calling context.getDatabasePath(TEST_MA_ID).length() itReturns sizeBelowMaxLimit

        massDB.insert(items)

        assertEquals(massDB.miniAppDatabaseStatus, MiniAppDatabaseStatus.READY)
    }

    @Test
    fun `verify endTransaction should not be called if database don't have any pending transactions after insert for above batch size`() = runBlockingTest {

        val sizeBelowMaxLimit = 100000L

        val items =  Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        When calling items.size itReturns BATCH_SIZE

        When calling context.getDatabasePath(TEST_MA_ID).length() itReturns sizeBelowMaxLimit

        massDB.insert(items)

        verify(massDB.database, times(0)).endTransaction()
    }

    @Test
    fun `verify endTransaction should be called if database still has any pending transactions after insert for above batch size`() = runBlockingTest {

        val sizeBelowMaxLimit = 100000L

        val items =  Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        When calling items.size itReturns BATCH_SIZE

        When calling context.getDatabasePath(TEST_MA_ID).length() itReturns sizeBelowMaxLimit

        When calling massDB.database.inTransaction() itReturns true

        massDB.insert(items)

        verify(massDB.database, times(2)).endTransaction()
    }
    @Test
    fun `verify the insert throws IllegalStateException for above batch size`() = runBlockingTest {

        val sizeBelowMaxLimit = 100000L

        val items =  Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        When calling items.size itReturns BATCH_SIZE

        When calling context.getDatabasePath(TEST_MA_ID).length() itReturns sizeBelowMaxLimit

        When calling massDB.finishAnyPendingDBTransaction() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        val ise = assertThrows(IllegalStateException::class.java) {
            massDB.insert(items)
        }

        assertEquals(ILLEGAL_STATE_EXCEPTION_ERROR_MSG, ise.message)
    }

    @Test
    fun `verify the finalize throws IllegalStateException after insert is done for above batch size`() = runBlockingTest {

        val sizeBelowMaxLimit = 100000L

        val items =  Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        When calling items.size itReturns BATCH_SIZE

        When calling context.getDatabasePath(TEST_MA_ID).length() itReturns sizeBelowMaxLimit

        When calling massDB.finalize() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        val ise = assertThrows(IllegalStateException::class.java) {
            massDB.insert(items)
        }

        assertEquals(ILLEGAL_STATE_EXCEPTION_ERROR_MSG, ise.message)
    }

    @Test
    fun `verify the finalize throws RuntimeException after insert is done for above batch size`() = runBlockingTest {

        val sizeBelowMaxLimit = 100000L

        val items =  Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        When calling items.size itReturns BATCH_SIZE

        When calling context.getDatabasePath(TEST_MA_ID).length() itReturns sizeBelowMaxLimit

        When calling massDB.finalize() itThrows RuntimeException(
            RUNTIME_EXCEPTION_ERROR_MSG
        )

        val rte = assertThrows(RuntimeException::class.java) {
            massDB.insert(items)
        }

        assertEquals(RUNTIME_EXCEPTION_ERROR_MSG, rte.message)
    }

    @Test
    fun `verify the failed status when insert throws IllegalStateException for above batch size`() = runBlockingTest {

        val sizeBelowMaxLimit = 100000L

        val items =  Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        When calling items.size itReturns BATCH_SIZE

        When calling context.getDatabasePath(TEST_MA_ID).length() itReturns sizeBelowMaxLimit

        When calling massDB.finishAnyPendingDBTransaction() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        assertThrows(IllegalStateException::class.java) {
            massDB.insert(items)
        }

        assertEquals(massDB.miniAppDatabaseStatus, MiniAppDatabaseStatus.FAILED)
    }

    @Test
    fun `verify the failed status when insert throws RuntimeException for above batch size`() = runBlockingTest {

        val sizeBelowMaxLimit = 100000L

        val items =  Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        When calling items.size itReturns BATCH_SIZE

        When calling context.getDatabasePath(TEST_MA_ID).length() itReturns sizeBelowMaxLimit

        When calling massDB.finishAnyPendingDBTransaction() itThrows RuntimeException(
            RUNTIME_EXCEPTION_ERROR_MSG
        )

        assertThrows(RuntimeException::class.java) {
            massDB.insert(items)
        }

        assertEquals(massDB.miniAppDatabaseStatus, MiniAppDatabaseStatus.FAILED)
    }

    /**
     * insert below batch size
     */
    @Test
    fun `verify the isDatabaseFull should not be called for inserting below batch size`() = runBlockingTest {

        val items =  Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        massDB.insert(items)

        verify(massDB, times(0)).isDatabaseFull()
    }

    @Test
    fun `verify all the items are inserted successfully below batch size2`() = runBlockingTest {

        val items =  Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        val contentValues = ContentValues()

        items.entries.forEach {
            contentValues.put(FIRST_COLUMN_NAME, it.key)
            contentValues.put(SECOND_COLUMN_NAME, it.value)
        }

        massDB.insert(items)

        verify(massDB, times(items.size)).insert(contentValues)
    }

    @Test
    fun `verify the insert successfully passes for below batch size`() = runBlockingTest {

        val items =  Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        assertTrue(massDB.insert(items))
    }

    @Test
    fun `verify the finalize was called insert successfully passes for below batch size`() = runBlockingTest {

        val items =  Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        massDB.insert(items)

        verify(massDB, times(1)).finalize()
    }

    @Test
    fun `verify the status after insert successfully passes for below batch size`() = runBlockingTest {

        val items =  Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        massDB.insert(items)

        assertEquals(massDB.miniAppDatabaseStatus, MiniAppDatabaseStatus.READY)
    }

    @Test
    fun `verify endTransaction should not be called if database don't have any pending transactions after insert`() = runBlockingTest {

        val items =  Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        massDB.insert(items)

        verify(massDB.database, times(0)).endTransaction()
    }

    @Test
    fun `verify endTransaction should be called if database still haa any pending transactions after insert`() = runBlockingTest {

        val items =  Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        When calling massDB.database.inTransaction() itReturns true

        massDB.insert(items)

        verify(massDB.database, times(2)).endTransaction()
    }

    @Test
    fun `verify the insert throws IllegalStateException`() = runBlockingTest {

        val items =  Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        When calling massDB.finishAnyPendingDBTransaction() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        val ise = assertThrows(IllegalStateException::class.java) {
            massDB.insert(items)
        }

        assertEquals(ILLEGAL_STATE_EXCEPTION_ERROR_MSG, ise.message)
    }

    @Test
    fun `verify the finalize throws IllegalStateException after insert is done`() = runBlockingTest {

        val items =  Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        When calling massDB.finalize() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        val ise = assertThrows(IllegalStateException::class.java) {
            massDB.insert(items)
        }

        assertEquals(ILLEGAL_STATE_EXCEPTION_ERROR_MSG, ise.message)
    }

    @Test
    fun `verify the finalize throws RuntimeException after insert is done`() = runBlockingTest {

        val items =  Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        When calling massDB.finishAnyPendingDBTransaction() itThrows RuntimeException(
            RUNTIME_EXCEPTION_ERROR_MSG
        )

        val rte = assertThrows(RuntimeException::class.java) {
            massDB.insert(items)
        }

        assertEquals(RUNTIME_EXCEPTION_ERROR_MSG, rte.message)
    }

    @Test
    fun `verify the failed status when insert throws IllegalStateException`() = runBlockingTest {

        val items =  Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        When calling massDB.finishAnyPendingDBTransaction() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        assertThrows(IllegalStateException::class.java) {
            massDB.insert(items)
        }

        assertEquals(massDB.miniAppDatabaseStatus, MiniAppDatabaseStatus.FAILED)
    }

    @Test
    fun `verify the failed status when insert throws RunTimeException`() = runBlockingTest {

        val items =  Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        When calling massDB.finishAnyPendingDBTransaction() itThrows RuntimeException(
            RUNTIME_EXCEPTION_ERROR_MSG
        )

        assertThrows(RuntimeException::class.java) {
            massDB.insert(items)
        }

        assertEquals(massDB.miniAppDatabaseStatus, MiniAppDatabaseStatus.FAILED)
    }

    /**
     * deleteItems test cases
     */
    @Test
    fun `verify deleteItems returns true if deleted successfully`() = runBlockingTest {

        val key = Mockito.spy(setOf("a"))

        When calling massDB.delete(key.first()) itReturns 1

        assertTrue(massDB.deleteItems(key))

    }

    @Test
    fun `verify delete was called for all items if deleted successfully`() = runBlockingTest {

        val key = Mockito.spy(setOf("a", "b"))

        When calling massDB.delete(key.first()) itReturns 1

        When calling massDB.delete(key.first()) itReturns 1

        massDB.deleteItems(key)

        verify(massDB, times(4)).delete(any())
    }

    /**
     * deleteItems above batches test cases
     */
}
