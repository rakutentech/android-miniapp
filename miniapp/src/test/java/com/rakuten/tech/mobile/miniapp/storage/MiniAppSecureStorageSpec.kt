package com.rakuten.tech.mobile.miniapp.storage

import android.content.Context
import com.rakuten.tech.mobile.miniapp.TEST_MAX_STORAGE_SIZE_IN_BYTES
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TEST_STORAGE_VERSION
import com.rakuten.tech.mobile.miniapp.errors.MiniAppSecureStorageError
import com.rakuten.tech.mobile.miniapp.storage.database.DATABASE_SPACE_LIMIT_REACHED_ERROR
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import net.sqlcipher.database.SQLiteFullException
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyZeroInteractions
import java.io.IOException
import java.sql.SQLException

@ExperimentalCoroutinesApi
@Suppress("LargeClass")
class MiniAppSecureStorageSpec {

    private val context: Context = mock()
    private val onSuccess: () -> Unit = mock()
    private val onFailed: (MiniAppSecureStorageError) -> Unit = mock()

    private lateinit var mass: MiniAppSecureStorage

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        mass = MiniAppSecureStorage(
            context,
            TEST_STORAGE_VERSION,
            TEST_MAX_STORAGE_SIZE_IN_BYTES.toLong()
        )
        mass.databaseName = TEST_MA_ID
        mass.miniAppSecureDatabase = mock()
        mass.scope = TestCoroutineScope()
    }

    @Test
    fun `get database used size should call success`() {

        val onSuccess: (Long) -> Unit = mock()

        When calling mass.miniAppSecureDatabase.getDatabaseUsedSize() itReturns TEST_MAX_STORAGE_SIZE_IN_BYTES.toLong()

        mass.getDatabaseUsedSize(onSuccess)

        Verify on onSuccess that onSuccess(TEST_MAX_STORAGE_SIZE_IN_BYTES.toLong()) was called
    }

    @Test
    fun `close database should be called`() {

        mass.closeDatabase()

        verify(mass.miniAppSecureDatabase, times(1)).closeDatabase()
    }

    /**
     * Starting of load test cases
     */
    @Test
    fun `load onSuccess should call if database is created successfully`() {

        When calling mass.miniAppSecureDatabase.createAndOpenDatabase() itReturns true

        mass.load(TEST_MA_ID, onSuccess, mock())

        Verify on onSuccess that onSuccess() was called
    }

    @Test
    fun `load onSuccess should not be called if database creation is not successfully`() {

        When calling mass.miniAppSecureDatabase.createAndOpenDatabase() itReturns false

        mass.load(TEST_MA_ID, onSuccess, mock())

        verifyZeroInteractions(onSuccess)
    }

    @Test
    fun `load onFailed should be called if runtime exception occurred while database creation`() {

        When calling mass.miniAppSecureDatabase.createAndOpenDatabase() itThrows RuntimeException("Failed")

        mass.load(TEST_MA_ID, mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
    }

    /**
     * Starting of insert item test cases
     */
    @Test
    fun `createAndOpenDatabase should not be called from insert if database is available`() =
        runBlockingTest {

            When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

            mass.insertItems(mock(), mock(), onFailed)

            verify(mass.miniAppSecureDatabase, times(0)).createAndOpenDatabase()
        }

    @Test
    fun `createAndOpenDatabase should be called from insert if database is available`() =
        runBlockingTest {

            When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns false

            mass.insertItems(mock(), mock(), onFailed)

            verify(mass.miniAppSecureDatabase, times(1)).createAndOpenDatabase()
        }

    @Test
    fun `insert onFailed should be called if pre check database is busy error occurred`() =
        runBlockingTest {

            When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

            When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns true

            mass.insertItems(mock(), mock(), onFailed)

            Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageBusyError) was called
        }

    @Test
    fun `insert should not be called if pre check database is busy error occurred`() =
        runBlockingTest {

            val items = mapOf("key" to "value")

            When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

            When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns true

            mass.insertItems(mock(), mock(), onFailed)

            verify(mass.miniAppSecureDatabase, times(0)).insert(items)
        }

    @Test
    fun `insert onFailed should be called if database is full`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns false

        When calling mass.miniAppSecureDatabase.isDatabaseFull() itReturns true

        mass.insertItems(mock(), mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageFullError) was called
    }

    @Test
    fun `insert should not be called if database is full`() = runBlockingTest {

        val items = mapOf("key" to "value")

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns false

        When calling mass.miniAppSecureDatabase.isDatabaseFull() itReturns true

        mass.insertItems(mock(), mock(), onFailed)

        verify(mass.miniAppSecureDatabase, times(0)).insert(items)
    }

    @Test
    fun `insert onFailed should be called if IllegalStateException occurred`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns false

        When calling mass.miniAppSecureDatabase.isDatabaseFull() itReturns false

        When calling mass.miniAppSecureDatabase.insert(any()) itThrows IllegalStateException("Failed")

        mass.insertItems(mock(), mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
    }

    @Test
    fun `insert onFailed should be called if runtime exception occurred`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns false

        When calling mass.miniAppSecureDatabase.isDatabaseFull() itReturns false

        When calling mass.miniAppSecureDatabase.insert(any()) itThrows RuntimeException("Failed")

        mass.insertItems(mock(), mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
    }

    @Test
    fun `insert onFailed should be called if database is full exception occurred`() =
        runBlockingTest {

            When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

            When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns false

            When calling mass.miniAppSecureDatabase.isDatabaseFull() itReturns false

            When calling mass.miniAppSecureDatabase.insert(any()) itThrows SQLiteFullException(
                DATABASE_SPACE_LIMIT_REACHED_ERROR
            )

            mass.insertItems(mock(), mock(), onFailed)

            Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageFullError) was called
        }

    @Test
    fun `insert onFailed should be called if database is busy error occurred`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns false

        When calling mass.miniAppSecureDatabase.isDatabaseFull() itReturns false

        When calling mass.miniAppSecureDatabase.insert(any()) itThrows SQLException("Failed")

        mass.insertItems(mock(), mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
    }

    @Test
    fun `insert onFailed should be called if insert is false`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.isDatabaseFull() itReturns false

        When calling mass.miniAppSecureDatabase.insert(any()) itReturns false

        mass.insertItems(mock(), mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
    }

    @Test
    fun `insert onSuccess should be called if items inserted successfully`() = runBlockingTest {

        val items = mapOf("key" to "value")

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.isDatabaseFull() itReturns false

        When calling mass.miniAppSecureDatabase.insert(items) itReturns true

        mass.insertItems(items, onSuccess, mock())

        Verify on onSuccess that onSuccess() was called
    }

    /**
     * Starting of get item test cases
     */
    @Test
    fun `get item onFailed should be called if database is unavailable error occurred`() =
        runBlockingTest {

            val key = "key"

            When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns false

            mass.getItem(key, mock(), onFailed)

            Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageUnavailableError) was called
        }

    @Test
    fun `get item should not be called if database is unavailable`() = runBlockingTest {

        val key = "key"

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns false

        mass.getItem(key, mock(), onFailed)

        verify(mass.miniAppSecureDatabase, times(0)).getItem(key)
    }

    @Test
    fun `get item onFailed should be called if database is busy error occurred`() =
        runBlockingTest {

            val key = "key"

            When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

            When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns true

            mass.getItem(key, mock(), onFailed)

            Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageBusyError) was called
        }

    @Test
    fun `get item should not be called if database is busy`() = runBlockingTest {

        val key = "key"

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns true

        mass.getItem(key, mock(), onFailed)

        verify(mass.miniAppSecureDatabase, times(0)).getItem(key)
    }

    @Test
    fun `get item onFailed should be called if IllegalStateException occurs`() = runBlockingTest {

        val key = "key"

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns false

        When calling mass.miniAppSecureDatabase.getItem(key) itThrows IllegalStateException("Failed")

        mass.getItem(key, mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
    }

    @Test
    fun `get item onFailed should be called if runtime exception occurs`() = runBlockingTest {

        val key = "key"

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns false

        When calling mass.miniAppSecureDatabase.getItem(key) itThrows RuntimeException("Failed")

        mass.getItem(key, mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
    }

    @Test
    fun `get item onFailed should be called if sql exception occurs`() = runBlockingTest {

        val key = "key"

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns false

        When calling mass.miniAppSecureDatabase.getItem(key) itThrows SQLException("Failed")

        mass.getItem(key, mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
    }

    @Test
    fun `get item onSuccess should be called if item fetched successfully`() = runBlockingTest {
        val onSuccess: (String) -> Unit = mock()
        val key = "key"
        val value = "value"

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns false

        When calling mass.miniAppSecureDatabase.getItem(key) itReturns value

        mass.getItem(key, onSuccess, mock())

        Verify on onSuccess that onSuccess(value) was called
    }

    /**
     * Starting of get all items test cases
     */

    @Test
    fun `get all items onFailed should be called if database is unavailable error occurred`() =
        runBlockingTest {

            When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns false

            mass.getAllItems(mock(), onFailed)

            Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageUnavailableError) was called
        }

    @Test
    fun `get all items should not be called if database is unavailable`() =
        runBlockingTest {

            When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns false

            mass.getAllItems(mock(), onFailed)

            verify(mass.miniAppSecureDatabase, times(0)).getAllItems()
        }

    @Test
    fun `get all items onFailed should be called if database is busy error occurred`() =
        runBlockingTest {

            When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

            When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns true

            mass.getAllItems(mock(), onFailed)

            Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageBusyError) was called
        }

    @Test
    fun `get all items should not be called if database is busy error`() =
        runBlockingTest {

            When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

            When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns true

            mass.getAllItems(mock(), onFailed)

            verify(mass.miniAppSecureDatabase, times(0)).getAllItems()
        }

    @Test
    fun `get all items onFailed should be called if IllegalStateException occurs`() =
        runBlockingTest {

            When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

            When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns false

            When calling mass.miniAppSecureDatabase.getAllItems() itThrows IllegalStateException("Failed")

            mass.getAllItems(mock(), onFailed)

            Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
        }

    @Test
    fun `get all items onFailed should be called if runtime exception occurs`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns false

        When calling mass.miniAppSecureDatabase.getAllItems() itThrows RuntimeException("Failed")

        mass.getAllItems(mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
    }

    @Test
    fun `get all items onFailed should be called if sql exception occurs`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns false

        When calling mass.miniAppSecureDatabase.getAllItems() itThrows SQLException("Failed")

        mass.getAllItems(mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
    }

    @Test
    fun `get all items onSuccess should be called if item fetched successfully`() =
        runBlockingTest {

            val onSuccess: (Map<String, String>) -> Unit = mock()
            val items: Map<String, String> = mapOf(
                "key1" to "value1",
                "key2" to "value2"
            )
            When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

            When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns false

            When calling mass.miniAppSecureDatabase.getAllItems() itReturns items

            mass.getAllItems(onSuccess, mock())

            Verify on onSuccess that onSuccess(items) was called
        }

    @Test
    fun `get all items onSuccess with empty map should be called if no items present`() =
        runBlockingTest {

            val onSuccess: (Map<String, String>) -> Unit = mock()
            val items: Map<String, String> = emptyMap()

            When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

            When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns false

            When calling mass.miniAppSecureDatabase.getAllItems() itReturns items

            mass.getAllItems(onSuccess, mock())

            Verify on onSuccess that onSuccess(items) was called
        }

    /**
     * Starting of delete items test cases
     */
    @Test
    fun `delete items onFailed should be called if database is unavailable error occurred`() =
        runBlockingTest {

            When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns false

            mass.deleteItems(mock(), mock(), onFailed)

            Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageUnavailableError) was called
        }

    @Test
    fun `delete items should not be called if database is unavailable`() =
        runBlockingTest {

            When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns false

            mass.deleteItems(mock(), mock(), onFailed)

            verify(mass.miniAppSecureDatabase, times(0)).deleteItems(any())
        }

    @Test
    fun `delete items onFailed should be called if database is busy error occurred`() =
        runBlockingTest {

            When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

            When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns true

            mass.deleteItems(mock(), mock(), onFailed)

            Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageBusyError) was called
        }

    @Test
    fun `delete items should not be called if database is busy`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns true

        mass.deleteItems(mock(), mock(), onFailed)

        verify(mass.miniAppSecureDatabase, times(0)).deleteItems(any())
    }

    @Test
    fun `delete items onFailed should be called if runtime exception occurs`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns false

        When calling mass.miniAppSecureDatabase.deleteItems(any()) itThrows RuntimeException("Failed")

        mass.deleteItems(mock(), mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
    }

    @Test
    fun `delete items onFailed should be called if IllegalStateException occurs`() =
        runBlockingTest {

            When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

            When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns false

            When calling mass.miniAppSecureDatabase.deleteItems(any()) itThrows IllegalStateException(
                "Failed"
            )

            mass.deleteItems(mock(), mock(), onFailed)

            Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
        }

    @Test
    fun `delete items onFailed should be called if sql exception occurs`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns false

        When calling mass.miniAppSecureDatabase.deleteItems(any()) itThrows SQLException("Failed")

        mass.deleteItems(mock(), mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
    }

    @Test
    fun `delete items onFailed should be called if database return false for deleting items`() =
        runBlockingTest {

            When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

            When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns false

            When calling mass.miniAppSecureDatabase.deleteItems(any()) itReturns false

            mass.deleteItems(mock(), mock(), onFailed)

            Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
        }

    @Test
    fun `delete items onSuccess should be called if database return true for deleting items`() =
        runBlockingTest {

            When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

            When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns false

            When calling mass.miniAppSecureDatabase.deleteItems(any()) itReturns true

            mass.deleteItems(mock(), onSuccess, mock())

            Verify on onSuccess that onSuccess() was called
        }

    /**
     * Starting delete whole database test cases
     */
    @Test
    fun `delete onFailed should be called if database is unavailable error occurred`() =
        runBlockingTest {

            When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns false

            mass.delete(mock(), onFailed)

            Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageUnavailableError) was called
        }

    @Test
    fun `deleteAllRecords should not be called if database is unavailable`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns false

        mass.delete(mock(), onFailed)

        verify(mass.miniAppSecureDatabase, times(0)).deleteAllRecords()
    }

    @Test
    fun `delete onFailed should be called if database is busy error occurred`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns true

        mass.delete(mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageBusyError) was called
    }

    @Test
    fun `deleteAllRecords should not be called if database is busy`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns true

        mass.delete(mock(), onFailed)

        verify(mass.miniAppSecureDatabase, times(0)).deleteAllRecords()
    }

    @Test
    fun `delete onFailed should be called if database sql exception occurred`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns false

        When calling mass.miniAppSecureDatabase.deleteAllRecords() itThrows SQLException("Failed")

        mass.delete(mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
    }

    @Test
    fun `delete onFailed should be called if database io exception occurred`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns false

        When calling mass.miniAppSecureDatabase.deleteAllRecords() itThrows IOException("Failed")

        mass.delete(mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
    }

    @Test
    fun `delete onFailed should be called if database runtime exception occurred`() =
        runBlockingTest {

            When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

            When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns false

            When calling mass.miniAppSecureDatabase.deleteAllRecords() itThrows RuntimeException("Failed")

            mass.delete(mock(), onFailed)

            Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
        }

    @Test
    fun `delete onFailed should be called if IllegalStateException occurs`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns false

        When calling mass.miniAppSecureDatabase.deleteAllRecords() itThrows IllegalStateException("Failed")

        mass.delete(mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
    }

    @Test
    fun `delete onSuccess should be called`() = runBlockingTest {
        val onSuccess: () -> Unit = mock()

        mass.databaseName = TEST_MA_ID

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.isDatabaseBusy() itReturns false

        mass.miniAppSecureDatabase.deleteAllRecords()

        mass.delete(onSuccess, mock())

        Verify on onSuccess that onSuccess() was called
    }
}
