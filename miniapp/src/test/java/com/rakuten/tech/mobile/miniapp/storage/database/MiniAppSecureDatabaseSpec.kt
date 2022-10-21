package com.rakuten.tech.mobile.miniapp.storage.database

import android.content.ContentValues
import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rakuten.tech.mobile.miniapp.TEST_MAX_STORAGE_SIZE_IN_BYTES
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TEST_STORAGE_VERSION
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import net.sqlcipher.database.SQLiteFullException
import org.amshove.kluent.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
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
    @Suppress("TooGenericExceptionThrown")
    fun setup() {
        MockitoAnnotations.openMocks(this)
        massDB = Mockito.spy(
            MiniAppSecureDatabase(
                context,
                TEST_MA_ID,
                TEST_STORAGE_VERSION,
                TEST_MAX_STORAGE_SIZE_IN_BYTES.toLong()
            )
        )
        massDB.miniAppDatabaseStatus = mock()
        massDB.database = mock(SupportSQLiteDatabase::class)

        database = mock(SupportSQLiteDatabase::class)
        massDBImpl = mock(MiniAppSecureDatabaseImpl::class)
        miniAppDBStatus = mock(MiniAppDatabaseStatus::class)
        dbFile = Mockito.spy(File("file://$TEST_MA_ID"))

        sqlException = assertThrows(SQLException::class.java) {
            throw SQLException(SQL_EXCEPTION_ERROR_MSG)
        }
        @Suppress("TooGenericExceptionThrown")
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
    fun `verify execSQL should be executed with AUTO_VACUUM query during onDatabaseConfiguration`() =
        runBlockingTest {

            massDB.onDatabaseConfiguration(database)

            verify(database, times(1)).execSQL(AUTO_VACUUM)
        }

    @Test
    fun `verify execSQL should not be executed with wrong query during onDatabaseConfiguration`() =
        runBlockingTest {

            massDB.onDatabaseConfiguration(database)

            verify(database, times(0)).execSQL(CREATE_TABLE_QUERY)
        }

    @Test
    fun `verify the error message when SQLException occurred during onDatabaseConfiguration`() =
        runBlockingTest {

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
    fun `verify execSQL should be executed for CREATE_TABLE_QUERY during onCreateDatabase`() =
        runBlockingTest {

            massDB.onCreateDatabase(database)

            verify(database, times(1)).execSQL(CREATE_TABLE_QUERY)
        }

    @Test
    fun `verify execSQL should not be executed for wrong query during onCreateDatabase `() =
        runBlockingTest {

            massDB.onCreateDatabase(database)

            verify(database, times(0)).execSQL(AUTO_VACUUM)
        }

    @Test
    fun `verify the error message when SQLException occurred during onCreateDatabase`() =
        runBlockingTest {

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
    fun `verify execSQL should be executed for DROP_TABLE_QUERY during onUpgradeDatabase`() =
        runBlockingTest {

            massDB.onUpgradeDatabase(database)

            verify(database, times(1)).execSQL(DROP_TABLE_QUERY)
        }

    @Test
    fun `verify execSQL should be executed for CREATE_TABLE_QUERY during onUpgradeDatabase`() =
        runBlockingTest {

            massDB.onUpgradeDatabase(database)

            verify(database, times(1)).execSQL(CREATE_TABLE_QUERY)
        }

    @Test
    fun `verify execSQL should not be executed for wrong query during onUpgradeDatabase `() =
        runBlockingTest {

            massDB.onUpgradeDatabase(database)

            verify(database, times(0)).execSQL(AUTO_VACUUM)
        }

    @Test
    fun `verify the onCreate was called when onUpgradeDatabase is success`() = runBlockingTest {

        massDB.onUpgradeDatabase(database)

        verify(database, times(2)).execSQL(any())
    }

    @Test
    fun `verify the error message when SQLException occurred during onUpgradeDatabase`() =
        runBlockingTest {

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
    fun `verify the status is called when RunTimeException occurred during onDatabaseCorrupted`() =
        runBlockingTest {

            When calling massDB.deleteWholeDatabase(TEST_MA_ID) itThrows RuntimeException(
                RUNTIME_EXCEPTION_ERROR_MSG
            )

            massDB.onDatabaseCorrupted(database)

            assertTrue(massDB.miniAppDatabaseStatus == MiniAppDatabaseStatus.UNAVAILABLE)
        }

    @Test
    fun `verify the error message when RunTimeException occurred during onDatabaseCorrupted`() =
        runBlockingTest {

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
    fun `verify the isDatabaseAvailable returns true if database file is found`() =
        runBlockingTest {

            val dbList = arrayOf(TEST_MA_ID, "DB-2", "DB-3")

            When calling context.databaseList() itAnswers { dbList }

            assertTrue(massDB.isDatabaseAvailable(TEST_MA_ID))
        }

    @Test
    fun `verify the isDatabaseAvailable returns false if database file is found`() =
        runBlockingTest {

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

        When calling context.getDatabasePath(TEST_MA_ID)
            .length() itReturns TEST_MAX_STORAGE_SIZE_IN_BYTES.toLong()

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

        When calling context.getDatabasePath(TEST_MA_ID)
            .length() itReturns TEST_MAX_STORAGE_SIZE_IN_BYTES.toLong()

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
     * finalize test cases
     */
    @Test
    fun `verify the inTransaction throws IllegalStateException for finalize`() = runBlockingTest {

        When calling massDB.database.inTransaction() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        massDB.finalize()

        verify(massDB.database, times(1)).inTransaction()
        verify(massDB.database, times(0)).endTransaction()
        assertTrue(massDB.miniAppDatabaseStatus != MiniAppDatabaseStatus.READY)
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
    fun `verify endTransaction should be called during database close`() {

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
    fun `verify the after tasks when DB full exception is thrown`() =
        runBlockingTest {

            var pairs: ArrayList<Pair<String, String>> = ArrayList()
            for (i in 1..BATCH_SIZE) {
                pairs.add(Pair("a-$i", "b-$i"))
            }

            val items = Mockito.spy(pairs.associate { Pair(it.first, it.second) })

            When calling context.getDatabasePath(TEST_MA_ID)
                .length() itReturns TEST_MAX_STORAGE_SIZE_IN_BYTES.toLong()

            val sqlFullException = assertThrows(SQLiteFullException::class.java) {
                massDB.insert(items)
            }

            assertTrue(sqlFullException.message == DATABASE_SPACE_LIMIT_REACHED_ERROR)
            verify(massDB.database, times(0)).beginTransaction()
            verify(massDB, times(0)).insert(ContentValues())
            verify(massDB.database, times(0)).setTransactionSuccessful()
            verify(massDB, times(0)).finishAnyPendingDBTransaction()
            verify(massDB, times(1)).finalize()
            verify(massDB.database, times(1)).inTransaction()
            verify(massDB.database, times(0)).endTransaction()
        }

    @Test
    fun `verify status is not full when database is not full`() =
        runBlockingTest {

            val sizeBelowMaxLimit = 100000L // making the isDatabaseFull = false

            val items = Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

            When calling items.size itReturns BATCH_SIZE

            When calling context.getDatabasePath(TEST_MA_ID).length() itReturns sizeBelowMaxLimit

            massDB.insert(items)

            assertTrue(massDB.miniAppDatabaseStatus != MiniAppDatabaseStatus.FULL)
        }

    @Test
    fun `verify the insert successfully passes for a batch when database is not full`() =
        runBlockingTest {

            val sizeBelowMaxLimit = 100000L

            val items = Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

            When calling items.size itReturns BATCH_SIZE

            When calling context.getDatabasePath(TEST_MA_ID).length() itReturns sizeBelowMaxLimit

            assertTrue(massDB.insert(items))
        }

    @Test
    fun `verify all the items are successfully inserted afor a batch when database is not full`() =
        runBlockingTest {

        val sizeBelowMaxLimit = 100000L

        var pairs: ArrayList<Pair<String, String>> = ArrayList()
        for (i in 1..BATCH_SIZE) {
            pairs.add(Pair("a-$i", "b-$i"))
        }

        val items = Mockito.spy(pairs.associate { Pair(it.first, it.second) })

        val contentValues = ContentValues()

        items.entries.forEach {
            contentValues.put(FIRST_COLUMN_NAME, it.key)
            contentValues.put(SECOND_COLUMN_NAME, it.value)
        }

        When calling context.getDatabasePath(TEST_MA_ID).length() itReturns sizeBelowMaxLimit

        val result = massDB.insert(items)

        assertTrue(result)
        verify(massDB, times(BATCH_SIZE)).insert(contentValues)
    }

    @Test
    fun `verify the finalize was called insert successfully passes for above batch size`() =
        runBlockingTest {

            val sizeBelowMaxLimit = 100000L

            val items = Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

            When calling items.size itReturns BATCH_SIZE

            When calling context.getDatabasePath(TEST_MA_ID).length() itReturns sizeBelowMaxLimit

            val result = massDB.insert(items)

            assertTrue(result)
            verify(massDB, times(1)).finalize()
            verify(massDB.database, times(2)).inTransaction()
            verify(massDB.database, times(0)).endTransaction()
            assertEquals(massDB.miniAppDatabaseStatus, MiniAppDatabaseStatus.READY)
        }

    @Test
    fun `verify endTransaction should not be called is no pending transaction for a batch`() =
        runBlockingTest {

            val sizeBelowMaxLimit = 100000L

            val items = Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

            When calling items.size itReturns BATCH_SIZE

            When calling context.getDatabasePath(TEST_MA_ID).length() itReturns sizeBelowMaxLimit

            massDB.insert(items)

            verify(massDB.database, times(0)).endTransaction()
        }

    @Test
    fun `verify endTransaction should be called after insert for above batch size`() =
        runBlockingTest {

            val sizeBelowMaxLimit = 100000L

            val items = Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

            When calling items.size itReturns BATCH_SIZE

            When calling context.getDatabasePath(TEST_MA_ID).length() itReturns sizeBelowMaxLimit

            When calling massDB.database.inTransaction() itReturns true

            massDB.insert(items)

            verify(massDB.database, times(2)).endTransaction()
        }

    @Test
    fun `verify the finishAnyPendingDBTransaction throws IllegalStateException for a batch`() = runBlockingTest {

        val sizeBelowMaxLimit = 100000L

        val items = Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        When calling items.size itReturns BATCH_SIZE

        When calling context.getDatabasePath(TEST_MA_ID).length() itReturns sizeBelowMaxLimit

        When calling massDB.finishAnyPendingDBTransaction() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        var result = false
        val ise = assertThrows(IllegalStateException::class.java) {
            result = massDB.insert(items)
        }

        assertEquals(massDB.miniAppDatabaseStatus, MiniAppDatabaseStatus.FAILED)
        assertEquals(ise.message, ILLEGAL_STATE_EXCEPTION_ERROR_MSG)
        verify(massDB, times(1)).finalize()
        verify(massDB.database, times(2)).inTransaction()
        verify(massDB.database, times(0)).endTransaction()
        assertTrue(massDB.miniAppDatabaseStatus != MiniAppDatabaseStatus.READY)
        assertFalse(result)
    }

    @Test
    fun `verify the setTransactionSuccessful throws IllegalStateException for a batch`() = runBlockingTest {

        val sizeBelowMaxLimit = 100000L

        val items = Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        When calling items.size itReturns BATCH_SIZE

        When calling context.getDatabasePath(TEST_MA_ID).length() itReturns sizeBelowMaxLimit

        When calling massDB.database.setTransactionSuccessful() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        var result = false
        val ise = assertThrows(IllegalStateException::class.java) {
            result = massDB.insert(items)
        }

        assertEquals(massDB.miniAppDatabaseStatus, MiniAppDatabaseStatus.FAILED)
        assertEquals(ise.message, ILLEGAL_STATE_EXCEPTION_ERROR_MSG)
        verify(massDB, times(1)).finalize()
        verify(massDB.database, times(1)).inTransaction()
        verify(massDB.database, times(0)).endTransaction()
        assertTrue(massDB.miniAppDatabaseStatus != MiniAppDatabaseStatus.READY)
        assertFalse(result)
    }

    @Test
    fun `verify the finalize throws IllegalStateException after insert is done for above batch size`() =
        runBlockingTest {

            val sizeBelowMaxLimit = 100000L

            val items = Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

            When calling items.size itReturns BATCH_SIZE

            When calling context.getDatabasePath(TEST_MA_ID).length() itReturns sizeBelowMaxLimit

            When calling massDB.finalize() itThrows IllegalStateException(
                ILLEGAL_STATE_EXCEPTION_ERROR_MSG
            )

            val ise = assertThrows(IllegalStateException::class.java) {
                massDB.insert(items)
            }

            assertEquals(ILLEGAL_STATE_EXCEPTION_ERROR_MSG, ise.message)
            verify(massDB.database, times(2)).inTransaction()
            verify(massDB.database, times(0)).endTransaction()
            assertTrue(massDB.miniAppDatabaseStatus != MiniAppDatabaseStatus.READY)
        }

    @Test
    fun `verify the failed status when insert throws RuntimeException for above batch size`() =
        runBlockingTest {

            val sizeBelowMaxLimit = 100000L

            val items = Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

            When calling items.size itReturns BATCH_SIZE

            When calling context.getDatabasePath(TEST_MA_ID).length() itReturns sizeBelowMaxLimit

            When calling massDB.finishAnyPendingDBTransaction() itThrows RuntimeException(
                RUNTIME_EXCEPTION_ERROR_MSG
            )

            val rte = assertThrows(RuntimeException::class.java) {
                massDB.insert(items)
            }

            assertEquals(massDB.miniAppDatabaseStatus, MiniAppDatabaseStatus.FAILED)
            assertEquals(rte.message, RUNTIME_EXCEPTION_ERROR_MSG)
            verify(massDB, times(1)).finalize()
            verify(massDB.database, times(2)).inTransaction()
            verify(massDB.database, times(0)).endTransaction()
            assertTrue(massDB.miniAppDatabaseStatus != MiniAppDatabaseStatus.READY)
        }

    /**
     * insert below batch size
     */
    @Test
    fun `verify the isDatabaseFull should not be called for inserting below batch size`() =
        runBlockingTest {

            val items = mapOf("a" to "b", "c" to "d", "e" to "f")

            massDB.insert(items)

            verify(massDB, times(0)).isDatabaseFull()
        }

    @Test
    fun `verify the insert returns true if passes for below batch size`() = runBlockingTest {

        val items = mapOf("a" to "b", "c" to "d", "e" to "f")

        assertTrue(massDB.insert(items))
    }

    @Test
    fun `verify all the items are inserted successfully`() = runBlockingTest {

        val items = mapOf("a" to "b", "c" to "d", "e" to "f")

        val contentValues = ContentValues()

        items.entries.forEach {
            contentValues.put(FIRST_COLUMN_NAME, it.key)
            contentValues.put(SECOND_COLUMN_NAME, it.value)
        }

        massDB.insert(items)

        verify(massDB, times(items.size)).insert(contentValues)
    }

    @Test
    fun `verify the finally block after inserted successfully below batch size`() =
        runBlockingTest {

            val items = mapOf("a" to "b", "c" to "d", "e" to "f")

            massDB.insert(items)

            verify(massDB, times(1)).finalize()
            assertEquals(massDB.miniAppDatabaseStatus, MiniAppDatabaseStatus.READY)
        }

    @Test
    fun `verify endTransaction shouldn't be called if not have any pending transactions after insert`() =
        runBlockingTest {

            val items = mapOf("a" to "b", "c" to "d", "e" to "f")

            massDB.insert(items)

            verify(massDB.database, times(0)).endTransaction()
        }

    @Test
    fun `verify endTransaction should be called if database still haa any pending transactions after insert`() =
        runBlockingTest {

            val items = mapOf("a" to "b", "c" to "d", "e" to "f")

            When calling massDB.database.inTransaction() itReturns true

            massDB.insert(items)

            verify(massDB.database, times(2)).endTransaction()
        }

    @Test
    fun `verify the finishAnyPendingDBTransaction throws IllegalStateException`() = runBlockingTest {

        val items = Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        When calling massDB.finishAnyPendingDBTransaction() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        val ise = assertThrows(IllegalStateException::class.java) {
            massDB.insert(items)
        }

        assertEquals(massDB.miniAppDatabaseStatus, MiniAppDatabaseStatus.FAILED)
        assertEquals(ise.message, ILLEGAL_STATE_EXCEPTION_ERROR_MSG)
        verify(massDB, times(1)).finalize()
        verify(massDB.database, times(2)).inTransaction()
        verify(massDB.database, times(0)).endTransaction()
        assertTrue(massDB.miniAppDatabaseStatus != MiniAppDatabaseStatus.READY)
    }

    @Test
    fun `verify the setTransactionSuccessful throws IllegalStateException`() = runBlockingTest {

        val items = Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        When calling massDB.database.setTransactionSuccessful() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        val ise = assertThrows(IllegalStateException::class.java) {
            massDB.insert(items)
        }

        assertEquals(massDB.miniAppDatabaseStatus, MiniAppDatabaseStatus.FAILED)
        assertEquals(ise.message, ILLEGAL_STATE_EXCEPTION_ERROR_MSG)
        verify(massDB, times(1)).finalize()
        verify(massDB.database, times(1)).inTransaction()
        verify(massDB.database, times(0)).endTransaction()
        assertTrue(massDB.miniAppDatabaseStatus != MiniAppDatabaseStatus.READY)
    }

    @Test
    fun `verify the finalize throws IllegalStateException after insert is done`() =
        runBlockingTest {

            val items = Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

            When calling massDB.finalize() itThrows IllegalStateException(
                ILLEGAL_STATE_EXCEPTION_ERROR_MSG
            )

            val ise = assertThrows(IllegalStateException::class.java) {
                massDB.insert(items)
            }

            assertEquals(ILLEGAL_STATE_EXCEPTION_ERROR_MSG, ise.message)
            verify(massDB.database, times(2)).inTransaction()
            verify(massDB.database, times(0)).endTransaction()
            assertTrue(massDB.miniAppDatabaseStatus != MiniAppDatabaseStatus.READY)
        }

    @Test
    fun `verify tasks after RuntimeException thrown`() = runBlockingTest {

        val items = Mockito.spy(mapOf("a" to "b", "c" to "d", "e" to "f"))

        When calling massDB.database.setTransactionSuccessful() itThrows RuntimeException(
            RUNTIME_EXCEPTION_ERROR_MSG
        )

        val rte = assertThrows(RuntimeException::class.java) {
            massDB.insert(items)
        }

        assertEquals(massDB.miniAppDatabaseStatus, MiniAppDatabaseStatus.FAILED)
        assertEquals(rte.message, RUNTIME_EXCEPTION_ERROR_MSG)
        verify(massDB, times(1)).finalize()
        verify(massDB.database, times(1)).inTransaction()
        verify(massDB.database, times(0)).endTransaction()
        assertTrue(massDB.miniAppDatabaseStatus != MiniAppDatabaseStatus.READY)
    }

    /**
     * deleteItems test cases
     */
    @Test
    fun `verify deleteItems returns true if deleted successfully`() = runBlockingTest {

        val key = setOf("a")

        When calling massDB.delete(key.first()) itReturns 1

        assertTrue(massDB.deleteItems(key))
    }

    @Test
    fun `verify delete was called upto total size of keys`() = runBlockingTest {

        val keys = HashSet<String>()

        for (i in 1..5) {
            keys.add("delete-$i")
        }

        massDB.deleteItems(keys)

        verify(massDB, times(keys.size)).delete(any())
    }

    @Test
    fun `verify the correct items are passed and deleted to and from the database`() =
        runBlockingTest {

            val keys = HashSet<String>()

            for (i in 1..5) {
                keys.add("delete-$i")
            }

            massDB.deleteItems(keys)

            val iterator = keys.iterator()
            while (iterator.hasNext()) {
                verify(massDB.database, times(1)).delete(
                    TABLE_NAME, "$FIRST_COLUMN_NAME='${iterator.next()}'", null
                )
            }
        }

    @Test
    fun `verify deleteItems returns false if delete is unsuccessful`() = runBlockingTest {

        val key = setOf("a")

        When calling massDB.delete(key.first()) itReturns 0

        assertFalse(massDB.deleteItems(key))
    }

    @Test
    fun `verify setTransactionSuccessful is called after deleteItems is successful`() =
        runBlockingTest {

            val key = setOf("a")

            When calling massDB.delete(key.first()) itReturns 1

            massDB.deleteItems(key)

            verify(massDB.database, times(1)).setTransactionSuccessful()
        }

    @Test
    fun `verify finishAnyPendingDBTransaction is called after deleteItems is successful`() =
        runBlockingTest {

            val key = setOf("a")

            When calling massDB.delete(key.first()) itReturns 1

            massDB.deleteItems(key)

            verify(massDB, times(1)).finishAnyPendingDBTransaction()
        }

    @Test
    fun `verify the transactions flow for deleteItems`() =
        runBlockingTest {

            val key = setOf("a")

            When calling massDB.delete(key.first()) itReturns 1

            massDB.deleteItems(key)

            verify(massDB.database, times(1)).beginTransaction()
            verify(massDB.database, times(1)).setTransactionSuccessful()
            verify(massDB.database, times(2)).inTransaction()
            verify(massDB.database, times(0)).endTransaction()
        }

    @Test
    fun `verify endTransaction shouldn't be called if not pending any after deleteItems`() =
        runBlockingTest {

            val key = setOf("a")

            When calling massDB.delete(key.first()) itReturns 1

            massDB.deleteItems(key)

            verify(massDB.database, times(2)).inTransaction()
            verify(massDB.database, times(0)).endTransaction()
        }

    @Test
    fun `verify endTransaction should be called if transaction is still pending for deleteItems`() =
        runBlockingTest {

            val key = setOf("a")

            When calling massDB.delete(key.first()) itReturns 1

            When calling massDB.database.inTransaction() itReturns true

            massDB.deleteItems(key)

            verify(massDB.database, times(2)).endTransaction()
        }

    @Test
    fun `verify finalize task in finally after deleteItems is successful`() = runBlockingTest {

        val key = setOf("a")

        When calling massDB.delete(key.first()) itReturns 1

        massDB.deleteItems(key)

        verify(massDB, times(1)).finalize()
    }

    @Test
    fun `verify status is ready after deleteItems is successful`() = runBlockingTest {

        val key = setOf("a")

        When calling massDB.delete(key.first()) itReturns 1

        massDB.deleteItems(key)

        assertTrue(massDB.miniAppDatabaseStatus == MiniAppDatabaseStatus.READY)
    }

    @Test
    fun `verify finishAnyPendingDBTransaction throws IllegalStateException during deleteItems`() {

        When calling massDB.finishAnyPendingDBTransaction() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        assertThrows(IllegalStateException::class.java) {
            massDB.deleteItems(setOf("a"))
        }

        assertEquals(massDB.miniAppDatabaseStatus, MiniAppDatabaseStatus.FAILED)
    }

    @Test
    fun `verify after tasks when IllegalStateException during deleteItems was thrown-1`() {

        When calling massDB.database.setTransactionSuccessful() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        assertThrows(IllegalStateException::class.java) {
            massDB.deleteItems(setOf("a"))
        }

        verify(massDB, times(1)).finalize()
    }

    @Test
    fun `verify after tasks when IllegalStateException during deleteItems was thrown-2`() {

        When calling massDB.database.setTransactionSuccessful() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        val ise = assertThrows(IllegalStateException::class.java) {
            massDB.deleteItems(setOf("a"))
        }

        assertEquals(ise.message, ILLEGAL_STATE_EXCEPTION_ERROR_MSG)
    }

    @Test
    fun `verify after tasks when IllegalStateException during deleteItems was thrown-3`() {

        When calling massDB.database.setTransactionSuccessful() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        assertThrows(IllegalStateException::class.java) {
            massDB.deleteItems(setOf("a"))
        }

        assertEquals(massDB.miniAppDatabaseStatus, MiniAppDatabaseStatus.FAILED)
    }

    @Test
    fun `verify after tasks when IllegalStateException during deleteItems was thrown-4`() {

        When calling massDB.database.setTransactionSuccessful() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        assertThrows(IllegalStateException::class.java) {
            massDB.deleteItems(setOf("a"))
        }

        verify(massDB.database, times(1)).inTransaction()
    }

    @Test
    fun `verify after tasks when IllegalStateException during deleteItems was thrown-5`() {

        When calling massDB.database.setTransactionSuccessful() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        assertThrows(IllegalStateException::class.java) {
            massDB.deleteItems(setOf("a"))
        }

        verify(massDB.database, times(0)).endTransaction()
    }

    @Test
    fun `verify after tasks when IllegalStateException during deleteItems was thrown-6`() {

        When calling massDB.database.setTransactionSuccessful() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        assertThrows(IllegalStateException::class.java) {
            massDB.deleteItems(setOf("a"))
        }

        assertTrue(massDB.miniAppDatabaseStatus != MiniAppDatabaseStatus.READY)
    }

    @Test
    fun `verify the RunTimeException occurred during deleteItems-1`() = runBlockingTest {

        val key = setOf("a")

        When calling massDB.delete(key.first()) itReturns 1

        When calling massDB.database.execSQL(AUTO_VACUUM) itThrows RuntimeException(
            RUNTIME_EXCEPTION_ERROR_MSG
        )

        assertThrows(RuntimeException::class.java) {
            massDB.deleteItems(key)
        }

        verify(massDB, times(1)).finalize()
    }

    @Test
    fun `verify the RunTimeException occurred during deleteItems-2`() = runBlockingTest {

        val key = setOf("a")

        When calling massDB.delete(key.first()) itReturns 1

        When calling massDB.database.execSQL(AUTO_VACUUM) itThrows RuntimeException(
            RUNTIME_EXCEPTION_ERROR_MSG
        )

        val rte = assertThrows(RuntimeException::class.java) {
            massDB.deleteItems(key)
        }

        assertEquals(rte.message, RUNTIME_EXCEPTION_ERROR_MSG)
    }

    @Test
    fun `verify the RunTimeException occurred during deleteItems-3`() = runBlockingTest {

        val key = setOf("a")

        When calling massDB.delete(key.first()) itReturns 1

        When calling massDB.database.execSQL(AUTO_VACUUM) itThrows RuntimeException(
            RUNTIME_EXCEPTION_ERROR_MSG
        )

        assertThrows(RuntimeException::class.java) {
            massDB.deleteItems(key)
        }

        assertEquals(massDB.miniAppDatabaseStatus, MiniAppDatabaseStatus.FAILED)
    }

    @Test
    fun `verify the RunTimeException occurred during deleteItems-4`() = runBlockingTest {

        val key = setOf("a")

        When calling massDB.delete(key.first()) itReturns 1

        When calling massDB.database.execSQL(AUTO_VACUUM) itThrows RuntimeException(
            RUNTIME_EXCEPTION_ERROR_MSG
        )

        assertThrows(RuntimeException::class.java) {
            massDB.deleteItems(key)
        }

        verify(massDB.database, times(2)).inTransaction()
    }

    @Test
    fun `verify the RunTimeException occurred during deleteItems-5`() = runBlockingTest {

        val key = setOf("a")

        When calling massDB.delete(key.first()) itReturns 1

        When calling massDB.database.execSQL(AUTO_VACUUM) itThrows RuntimeException(
            RUNTIME_EXCEPTION_ERROR_MSG
        )

        assertThrows(RuntimeException::class.java) {
            massDB.deleteItems(key)
        }

        verify(massDB.database, times(0)).endTransaction()
    }

    @Test
    fun `verify the RunTimeException occurred during deleteItems-6`() = runBlockingTest {

        val key = setOf("a")

        When calling massDB.delete(key.first()) itReturns 1

        When calling massDB.database.execSQL(AUTO_VACUUM) itThrows RuntimeException(
            RUNTIME_EXCEPTION_ERROR_MSG
        )

        val rte = assertThrows(RuntimeException::class.java) {
            massDB.deleteItems(key)
        }
        assertTrue(massDB.miniAppDatabaseStatus != MiniAppDatabaseStatus.READY)
    }

    /**
     * deleteItems above batches test cases
     */
    @Test
    fun `verify deleteItems returns true if deleted successfully above batches`() =
        runBlockingTest {

            val keys = Mockito.spy(HashSet<String>())

            for (i in 1..BATCH_SIZE) {
                keys.add("delete-$i")
            }

            val itr = keys.iterator()
            while (itr.hasNext()) {
                When calling massDB.delete(itr.next()) itReturns 1
            }

            assertTrue(massDB.deleteItems(keys))
        }

    @Test
    fun `verify deleteItems returns true even if only one item deleted from a batch`() =
        runBlockingTest {

            val keys = Mockito.spy(HashSet<String>())

            for (i in 1..BATCH_SIZE) {
                keys.add("delete-$i")
            }

            var cnt = 1
            val itr = keys.iterator()
            while (itr.hasNext()) {

                if (cnt == 51) {
                    When calling massDB.delete(itr.next()) itReturns 1
                } else {
                    When calling massDB.delete(itr.next()) itReturns 0
                }
                cnt += 1
            }

            assertTrue(massDB.deleteItems(keys))
        }

    @Test
    fun `verify deleteItems deleted all items in above batch size`() = runBlockingTest {

        val keys = Mockito.spy(HashSet<String>())

        for (i in 1..BATCH_SIZE) {
            keys.add("delete-$i")
        }

        val itr = keys.iterator()
        while (itr.hasNext()) {
            When calling massDB.delete(itr.next()) itReturns 1
        }

        verify(massDB, times(BATCH_SIZE)).delete(any())
    }

    @Test
    fun `verify the correct items are passed and deleted to and from the database in a batch`() =
        runBlockingTest {

            val keys = Mockito.spy(HashSet<String>())

            for (i in 1..BATCH_SIZE) {
                keys.add("delete-$i")
            }

            massDB.deleteItems(keys)

            val iterator = keys.iterator()
            while (iterator.hasNext()) {
                verify(massDB.database, times(1)).delete(
                    TABLE_NAME, "$FIRST_COLUMN_NAME='${iterator.next()}'", null
                )
            }
        }

    @Test
    fun `verify the transactions flow for deleteItems in a batch`() =
        runBlockingTest {

            val keys = Mockito.spy(HashSet<String>())

            for (i in 1..BATCH_SIZE) {
                keys.add("delete-$i")
            }

            massDB.deleteItems(keys)

            verify(massDB.database, times(2)).beginTransaction()
            verify(massDB.database, times(2)).setTransactionSuccessful()
            verify(massDB.database, times(3)).inTransaction()
            verify(massDB.database, times(0)).endTransaction()
        }

    @Test
    fun `verify endTransaction shouldn't be called if not pending any after batch deleteItems`() =
        runBlockingTest {

            val keys = Mockito.spy(HashSet<String>())

            for (i in 1..BATCH_SIZE) {
                keys.add("delete-$i")
            }

            massDB.deleteItems(keys)

            verify(massDB.database, times(3)).inTransaction()
            verify(massDB.database, times(0)).endTransaction()
        }

    @Test
    fun `verify endTransaction should be called if transaction is still pending for deleteItems2`() =
        runBlockingTest {

            val keys = Mockito.spy(HashSet<String>())

            for (i in 1..BATCH_SIZE) {
                keys.add("delete-$i")
            }

            When calling massDB.database.inTransaction() itReturns true

            massDB.deleteItems(keys)

            verify(massDB.database, times(3)).endTransaction()
        }

    @Test
    fun `verify deleteItems returns true even if none of the keys found to delete in a batch`() =
        runBlockingTest {

            val keys = Mockito.spy(HashSet<String>())

            for (i in 1..BATCH_SIZE) {
                keys.add("delete-$i")
            }

            val itr = keys.iterator()
            while (itr.hasNext()) {
                When calling massDB.delete(itr.next()) itReturns 0
            }

            assertTrue(massDB.deleteItems(keys))
        }

    @Test
    fun `verify after tasks after every batch completed`() = runBlockingTest {

        val keys = Mockito.spy(HashSet<String>())

        for (i in 1..BATCH_SIZE) {
            keys.add("delete-$i")
        }

        When calling keys.size itReturns BATCH_SIZE

        val itr = keys.iterator()
        while (itr.hasNext()) {
            When calling massDB.delete(itr.next()) itReturns 1
        }

        When calling massDB.database.inTransaction() itReturns true

        massDB.deleteItems(keys)

        verify(massDB.database, times(2)).setTransactionSuccessful()
        verify(massDB, times(2)).finishAnyPendingDBTransaction()
        verify(massDB, times(1)).finalize()
        verify(massDB.database, times(3)).endTransaction()
        assertEquals(massDB.miniAppDatabaseStatus, MiniAppDatabaseStatus.READY)
    }

    @Test
    fun `verify the finalize throws IllegalStateException after deleteItems`() =
        runBlockingTest {

            When calling massDB.finalize() itThrows IllegalStateException(
                ILLEGAL_STATE_EXCEPTION_ERROR_MSG
            )

            val ise = assertThrows(IllegalStateException::class.java) {
                massDB.deleteItems(setOf("a"))
            }

            assertEquals(ILLEGAL_STATE_EXCEPTION_ERROR_MSG, ise.message)
            verify(massDB.database, times(2)).inTransaction()
            verify(massDB.database, times(0)).endTransaction()
            assertTrue(massDB.miniAppDatabaseStatus != MiniAppDatabaseStatus.READY)
        }

    @Test
    fun `verify finishAnyPendingDBTransaction throws IllegalStateException during deleteItems in a batch`() {

        When calling massDB.finishAnyPendingDBTransaction() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        assertThrows(IllegalStateException::class.java) {
            massDB.deleteItems(setOf("a"))
        }

        assertEquals(massDB.miniAppDatabaseStatus, MiniAppDatabaseStatus.FAILED)
    }

    @Test
    fun `verify after tasks when IllegalStateException thrown during batch of deleteItems-1`() {

        val keys = Mockito.spy(setOf("a"))

        When calling keys.size itReturns BATCH_SIZE

        When calling massDB.database.setTransactionSuccessful() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        assertThrows(IllegalStateException::class.java) {
            massDB.deleteItems(keys)
        }

        verify(massDB, times(1)).finalize()
    }

    @Test
    fun `verify after tasks when IllegalStateException thrown during batch of deleteItems-2`() {

        val keys = Mockito.spy(setOf("a"))

        When calling keys.size itReturns BATCH_SIZE

        When calling massDB.database.setTransactionSuccessful() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        val ise = assertThrows(IllegalStateException::class.java) {
            massDB.deleteItems(keys)
        }

        assertEquals(ise.message, ILLEGAL_STATE_EXCEPTION_ERROR_MSG)
    }

    @Test
    fun `verify after tasks when IllegalStateException thrown during batch of deleteItems-3`() {

        val keys = Mockito.spy(setOf("a"))

        When calling keys.size itReturns BATCH_SIZE

        When calling massDB.database.setTransactionSuccessful() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        assertThrows(IllegalStateException::class.java) {
            massDB.deleteItems(keys)
        }

        assertEquals(massDB.miniAppDatabaseStatus, MiniAppDatabaseStatus.FAILED)
    }

    @Test
    fun `verify after tasks when IllegalStateException thrown during batch of deleteItems-4`() {

        val keys = Mockito.spy(setOf("a"))

        When calling keys.size itReturns BATCH_SIZE

        When calling massDB.database.setTransactionSuccessful() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        assertThrows(IllegalStateException::class.java) {
            massDB.deleteItems(keys)
        }

        verify(massDB.database, times(1)).inTransaction()
    }

    @Test
    fun `verify after tasks when IllegalStateException thrown during batch of deleteItems-5`() {

        val keys = Mockito.spy(setOf("a"))

        When calling keys.size itReturns BATCH_SIZE

        When calling massDB.database.setTransactionSuccessful() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        assertThrows(IllegalStateException::class.java) {
            massDB.deleteItems(keys)
        }

        verify(massDB.database, times(0)).endTransaction()
    }

    @Test
    fun `verify after tasks when IllegalStateException thrown during batch of deleteItems-6`() {

        val keys = Mockito.spy(setOf("a"))

        When calling keys.size itReturns BATCH_SIZE

        When calling massDB.database.setTransactionSuccessful() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        assertThrows(IllegalStateException::class.java) {
            massDB.deleteItems(keys)
        }

        assertTrue(massDB.miniAppDatabaseStatus != MiniAppDatabaseStatus.READY)
    }

    @Test
    fun `verify the RunTimeException occurred during batch of deleteItems-1`() = runBlockingTest {

        val keys = Mockito.spy(setOf("a"))

        When calling keys.size itReturns BATCH_SIZE

        When calling massDB.database.execSQL(AUTO_VACUUM) itThrows RuntimeException(
            RUNTIME_EXCEPTION_ERROR_MSG
        )

        assertThrows(RuntimeException::class.java) {
            massDB.deleteItems(keys)
        }

        verify(massDB, times(1)).finalize()
    }

    @Test
    fun `verify the RunTimeException occurred during batch of deleteItems-2`() = runBlockingTest {

        val keys = Mockito.spy(setOf("a"))

        When calling keys.size itReturns BATCH_SIZE

        When calling massDB.database.execSQL(AUTO_VACUUM) itThrows RuntimeException(
            RUNTIME_EXCEPTION_ERROR_MSG
        )

        val rte = assertThrows(RuntimeException::class.java) {
            massDB.deleteItems(keys)
        }

        assertEquals(rte.message, RUNTIME_EXCEPTION_ERROR_MSG)
    }

    @Test
    fun `verify the RunTimeException occurred during batch of deleteItems-3`() = runBlockingTest {

        val keys = Mockito.spy(setOf("a"))

        When calling keys.size itReturns BATCH_SIZE

        When calling massDB.database.execSQL(AUTO_VACUUM) itThrows RuntimeException(
            RUNTIME_EXCEPTION_ERROR_MSG
        )

        assertThrows(RuntimeException::class.java) {
            massDB.deleteItems(keys)
        }

        assertEquals(massDB.miniAppDatabaseStatus, MiniAppDatabaseStatus.FAILED)
    }

    @Test
    fun `verify the RunTimeException occurred during batch of deleteItems-4`() = runBlockingTest {

        val keys = Mockito.spy(setOf("a"))

        When calling keys.size itReturns BATCH_SIZE

        When calling massDB.database.execSQL(AUTO_VACUUM) itThrows RuntimeException(
            RUNTIME_EXCEPTION_ERROR_MSG
        )

        assertThrows(RuntimeException::class.java) {
            massDB.deleteItems(keys)
        }

        verify(massDB.database, times(2)).inTransaction()
    }

    @Test
    fun `verify the RunTimeException occurred during batch of deleteItems-5`() = runBlockingTest {

        val keys = Mockito.spy(setOf("a"))

        When calling keys.size itReturns BATCH_SIZE

        When calling massDB.database.execSQL(AUTO_VACUUM) itThrows RuntimeException(
            RUNTIME_EXCEPTION_ERROR_MSG
        )

        assertThrows(RuntimeException::class.java) {
            massDB.deleteItems(keys)
        }

        verify(massDB.database, times(0)).endTransaction()
    }

    @Test
    fun `verify the RunTimeException occurred during batch of deleteItems-6`() = runBlockingTest {

        val keys = Mockito.spy(setOf("a"))

        When calling keys.size itReturns BATCH_SIZE

        When calling massDB.database.execSQL(AUTO_VACUUM) itThrows RuntimeException(
            RUNTIME_EXCEPTION_ERROR_MSG
        )

        assertThrows(RuntimeException::class.java) {
            massDB.deleteItems(keys)
        }

        assertTrue(massDB.miniAppDatabaseStatus != MiniAppDatabaseStatus.READY)
    }

    @Test
    fun `verify the AUTO_VACCUM has been called after the deleteItems`() = runBlockingTest {

        val key = Mockito.spy(setOf("a"))

        When calling massDB.delete(key.first()) itReturns 1

        assertTrue(massDB.deleteItems(key))

        verify(massDB.database, times(1)).execSQL(AUTO_VACUUM)
    }

    /**
     * deleteAllRecords test cases
     */
    @Test
    fun `verify sql query executed successfully`() = runBlockingTest {

        massDB.deleteAllRecords()

        verify(massDB.database, times(1)).execSQL(DROP_TABLE_QUERY)
    }

    @Test
    fun `verify transaction is successful after deleting all records`() = runBlockingTest {

        massDB.deleteAllRecords()

        verify(massDB.database, times(1)).setTransactionSuccessful()
    }

    @Test
    fun `verify the transactions flow for deleteAllRecords`() =
        runBlockingTest {

            massDB.deleteAllRecords()

            verify(massDB.database, times(1)).beginTransaction()
            verify(massDB.database, times(1)).setTransactionSuccessful()
            verify(massDB.database, times(2)).inTransaction()
        }

    @Test
    fun `verify endTransaction shouldn't be called if not pending any for deleteAllRecords`() =
        runBlockingTest {

            massDB.deleteAllRecords()

            verify(massDB.database, times(2)).inTransaction()
            verify(massDB.database, times(0)).endTransaction()
        }

    @Test
    fun `verify endTransaction should be called if transaction still pending for deleteAllRecords`() =
        runBlockingTest {

            When calling massDB.database.inTransaction() itReturns true

            massDB.deleteAllRecords()

            verify(massDB.database, times(2)).endTransaction()
        }

    @Test
    fun `verify finalize task in finally after deleteAllRecords is successful`() = runBlockingTest {

        massDB.deleteAllRecords()

        verify(massDB, times(1)).finalize()
    }

    @Test
    fun `verify status in finally after deleteAllRecords is successful`() = runBlockingTest {

        massDB.deleteAllRecords()

        assertTrue(massDB.miniAppDatabaseStatus == MiniAppDatabaseStatus.UNAVAILABLE)
    }

    @Test
    fun `verify database isn't opened if all the records in the table are deleted successfully`() =
        runBlockingTest {

            massDB.deleteAllRecords()

            assertFalse(massDB.database.isOpen)
        }

    @Test
    fun `verify db close should not be called if db is not opened after deleteAllRecords`() =
        runBlockingTest {

            massDB.deleteAllRecords()

            assertFalse(massDB.database.isOpen)
            verify(massDB.database, times(0)).close()
        }

    @Test
    fun `verify deleteAllRecords closes the database`() = runBlockingTest {

        When calling massDB.database.isOpen itReturns true

        massDB.deleteAllRecords()

        Verify on massDB.database that massDB.database.close() was called
    }

    @Test
    fun `verify finishAnyPendingDBTransaction was called twice after deleteAllRecords`() =
        runBlockingTest {

            When calling massDB.database.isOpen itReturns true

            massDB.deleteAllRecords()

            verify(massDB, times(2)).finishAnyPendingDBTransaction()
        }

    @Test
    fun `verify inTransaction was called thrice when closes database`() = runBlockingTest {

        When calling massDB.database.isOpen itReturns true

        massDB.deleteAllRecords()

        verify(massDB.database, times(3)).inTransaction()
    }

    @Test
    fun `verify finishAnyPendingDBTransaction not called if database was not opened`() =
        runBlockingTest {

            When calling massDB.database.isOpen itReturns false

            massDB.deleteAllRecords()

            verify(massDB, times(1)).finishAnyPendingDBTransaction()
        }

    @Test
    fun `verify the finalize throws IllegalStateException after deleteAllRecords`() =
        runBlockingTest {

            When calling massDB.finalize() itThrows IllegalStateException(
                ILLEGAL_STATE_EXCEPTION_ERROR_MSG
            )

            val ise = assertThrows(IllegalStateException::class.java) {
                massDB.deleteAllRecords()
            }

            assertEquals(ILLEGAL_STATE_EXCEPTION_ERROR_MSG, ise.message)
            verify(massDB.database, times(2)).inTransaction()
            verify(massDB.database, times(0)).endTransaction()
            assertTrue(massDB.miniAppDatabaseStatus != MiniAppDatabaseStatus.READY)
        }

    @Test
    fun `verify finishAnyPendingDBTransaction throws IllegalStateException`() {

        When calling massDB.finishAnyPendingDBTransaction() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        assertThrows(IllegalStateException::class.java) {
            massDB.deleteAllRecords()
        }

        assertEquals(massDB.miniAppDatabaseStatus, MiniAppDatabaseStatus.FAILED)
    }

    @Test
    fun `verify after tasks when IllegalStateException was thrown-1`() {

        When calling massDB.database.setTransactionSuccessful() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        assertThrows(IllegalStateException::class.java) {
            massDB.deleteAllRecords()
        }

        verify(massDB, times(1)).finalize()
    }

    @Test
    fun `verify after tasks when IllegalStateException was thrown-2`() {

        When calling massDB.database.setTransactionSuccessful() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        assertThrows(IllegalStateException::class.java) {
            massDB.deleteAllRecords()
        }

        assertTrue(massDB.miniAppDatabaseStatus != MiniAppDatabaseStatus.UNAVAILABLE)
    }

    @Test
    fun `verify after tasks when IllegalStateException was thrown-3`() {

        When calling massDB.database.setTransactionSuccessful() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        val ise = assertThrows(IllegalStateException::class.java) {
            massDB.deleteAllRecords()
        }

        assertEquals(ise.message, ILLEGAL_STATE_EXCEPTION_ERROR_MSG)
    }

    @Test
    fun `verify after tasks when IllegalStateException was thrown-4`() {

        When calling massDB.database.setTransactionSuccessful() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        assertThrows(IllegalStateException::class.java) {
            massDB.deleteAllRecords()
        }

        assertTrue(massDB.miniAppDatabaseStatus != MiniAppDatabaseStatus.READY)
    }

    @Test
    fun `verify after tasks when IllegalStateException was thrown-5`() {

        When calling massDB.database.setTransactionSuccessful() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        assertThrows(IllegalStateException::class.java) {
            massDB.deleteAllRecords()
        }

        verify(massDB.database, times(1)).inTransaction()
    }

    @Test
    fun `verify after tasks when IllegalStateException was thrown-6`() {

        When calling massDB.database.setTransactionSuccessful() itThrows IllegalStateException(
            ILLEGAL_STATE_EXCEPTION_ERROR_MSG
        )

        assertThrows(IllegalStateException::class.java) {
            massDB.deleteAllRecords()
        }

        verify(massDB.database, times(0)).endTransaction()
    }

    @Test
    fun `verify after tasks when RuntimeException was thrown-1`() {

        When calling massDB.database.execSQL(DROP_TABLE_QUERY) itThrows RuntimeException(
            RUNTIME_EXCEPTION_ERROR_MSG
        )

        assertThrows(RuntimeException::class.java) {
            massDB.deleteAllRecords()
        }

        assertEquals(massDB.miniAppDatabaseStatus, MiniAppDatabaseStatus.FAILED)
    }

    @Test
    fun `verify after tasks when RuntimeException was thrown-2`() {

        When calling massDB.database.execSQL(DROP_TABLE_QUERY) itThrows RuntimeException(
            RUNTIME_EXCEPTION_ERROR_MSG
        )

        assertThrows(RuntimeException::class.java) {
            massDB.deleteAllRecords()
        }

        assertTrue(massDB.miniAppDatabaseStatus != MiniAppDatabaseStatus.UNAVAILABLE)
    }

    @Test
    fun `verify after tasks when RuntimeException was thrown-3`() {

        When calling massDB.database.execSQL(DROP_TABLE_QUERY) itThrows RuntimeException(
            RUNTIME_EXCEPTION_ERROR_MSG
        )

        val ise = assertThrows(RuntimeException::class.java) {
            massDB.deleteAllRecords()
        }

        assertEquals(ise.message, RUNTIME_EXCEPTION_ERROR_MSG)
    }

    @Test
    fun `verify after tasks when RuntimeException was thrown-4`() {

        When calling massDB.database.execSQL(DROP_TABLE_QUERY) itThrows RuntimeException(
            RUNTIME_EXCEPTION_ERROR_MSG
        )

        assertThrows(RuntimeException::class.java) {
            massDB.deleteAllRecords()
        }

        assertTrue(massDB.miniAppDatabaseStatus != MiniAppDatabaseStatus.READY)
    }

    @Test
    fun `verify after tasks when RuntimeException was thrown-5`() {

        When calling massDB.database.execSQL(DROP_TABLE_QUERY) itThrows RuntimeException(
            RUNTIME_EXCEPTION_ERROR_MSG
        )

        assertThrows(RuntimeException::class.java) {
            massDB.deleteAllRecords()
        }

        verify(massDB.database, times(1)).inTransaction()
    }

    @Test
    fun `verify after tasks when RuntimeException was thrown-6`() {

        When calling massDB.database.execSQL(DROP_TABLE_QUERY) itThrows RuntimeException(
            RUNTIME_EXCEPTION_ERROR_MSG
        )

        assertThrows(RuntimeException::class.java) {
            massDB.deleteAllRecords()
        }

        verify(massDB.database, times(0)).endTransaction()
    }
}
