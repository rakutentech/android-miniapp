package com.rakuten.tech.mobile.miniapp.storage

import android.content.Context
import android.database.sqlite.SQLiteException
import com.rakuten.tech.mobile.miniapp.TEST_MAX_STORAGE_SIZE_IN_KB
import com.rakuten.tech.mobile.miniapp.TEST_MA_ID
import com.rakuten.tech.mobile.miniapp.TEST_STORAGE_VERSION
import com.rakuten.tech.mobile.miniapp.errors.MiniAppSecureStorageError
import com.rakuten.tech.mobile.miniapp.storage.database.DATABASE_BUSY_ERROR
import com.rakuten.tech.mobile.miniapp.storage.database.DATABASE_SPACE_LIMIT_REACHED_ERROR
import com.rakuten.tech.mobile.miniapp.storage.database.DATABASE_UNAVAILABLE_ERROR
import com.rakuten.tech.mobile.miniapp.storage.database.MiniAppSecureDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.MockitoAnnotations
import java.io.IOException
import java.sql.SQLException

@ExperimentalCoroutinesApi
class MiniAppSecureStorageSpec {

    private val context: Context = mock()
    private val onSuccess: () -> Unit = mock()
    private val masdb: MiniAppSecureDatabase = mock()
    private val onFailed: (MiniAppSecureStorageError) -> Unit = mock()

    private lateinit var mass: MiniAppSecureStorage

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this);
        mass = MiniAppSecureStorage(
            context,
            TEST_STORAGE_VERSION,
            TEST_MAX_STORAGE_SIZE_IN_KB
        )
        mass.databaseName = TEST_MA_ID
        mass.miniAppSecureDatabase = masdb
        mass.scope = TestCoroutineScope()
    }

    @Test
    @Ignore
    fun `load should return onFailed in case if database couldn't created or opened`() {

        When calling masdb.createAndOpenDatabase() itReturns true

        mass.load(TEST_MA_ID, onSuccess, mock())

        Verify on onSuccess that onSuccess() was called
    }

    @Test
    fun `get database used size should call success`() {

        val onSuccess: (Long) -> Unit = mock()

        When calling masdb.getDatabaseUsedSize() itReturns TEST_MAX_STORAGE_SIZE_IN_KB.toLong()

        mass.getDatabaseUsedSize(onSuccess)

        Verify on onSuccess that onSuccess(TEST_MAX_STORAGE_SIZE_IN_KB.toLong()) was called
    }

    /**
     * Starting of insert item test cases
     */

    @Test
    fun `insert onFailed should be called if sql exception occurs`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.insert(any()) itThrows SQLException("Failed")

        mass.insertItems(mock(), mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
    }

    @Test
    fun `insert onFailed should be called if sqlite exception occurs`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.insert(any()) itThrows SQLiteException("Failed")

        mass.insertItems(mock(), mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
    }

    @Test
    fun `insert onFailed should be called if database is busy error occurred`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.insert(any()) itThrows SQLException(
            DATABASE_BUSY_ERROR
        )

        mass.insertItems(mock(), mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageBusyError) was called
    }

    @Test
    fun `insert onFailed should be called if database is full error occurred`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.insert(any()) itThrows SQLException(
            DATABASE_SPACE_LIMIT_REACHED_ERROR
        )

        mass.insertItems(mock(), mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageFullError) was called
    }

    @Test
    fun `insert onFailed should be called if insert is false`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.insert(any()) itReturns false

        mass.insertItems(mock(), mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
    }

    @Test
    fun `insert onSuccess should be called if items inserted successfully`() = runBlockingTest {

        val items = mapOf("key" to "value")

        When calling mass.miniAppSecureDatabase.isDatabaseAvailable(mass.databaseName) itReturns true

        When calling mass.miniAppSecureDatabase.insert(items) itReturns true

        mass.insertItems(items, onSuccess, mock())

        Verify on onSuccess that onSuccess() was called
    }

    /**
     * Starting of get item test cases
     */

    @Test
    fun `get item onFailed should be called if runtime exception occurs`() = runBlockingTest {

        val key = "key"

        When calling mass.miniAppSecureDatabase.getItem(key) itThrows RuntimeException("Failed")

        mass.getItem(key, mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
    }

    @Test
    fun `get item onFailed should be called if sql exception occurs`() = runBlockingTest {

        val key = "key"

        When calling mass.miniAppSecureDatabase.getItem(key) itThrows SQLException("Failed")

        mass.getItem(key, mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
    }

    @Test
    fun `get item onFailed should be called if database is busy error occurred`() =
        runBlockingTest {

            val key = "key"

            When calling mass.miniAppSecureDatabase.getItem(key) itThrows SQLException(
                DATABASE_BUSY_ERROR
            )

            mass.getItem(key, mock(), onFailed)

            Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageBusyError) was called
        }

    @Test
    fun `get item onFailed should be called if database is unavailable error occurred`() =
        runBlockingTest {

            val key = "key"

            When calling mass.miniAppSecureDatabase.getItem(key) itThrows SQLException(
                DATABASE_UNAVAILABLE_ERROR
            )

            mass.getItem(key, mock(), onFailed)

            Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageUnavailableError) was called
        }

    @Test
    fun `get item onSuccess should be called if item fetched successfully`() = runBlockingTest {
        val onSuccess: (String) -> Unit = mock()
        val key = "key"
        val value = "value"

        When calling mass.miniAppSecureDatabase.getItem(key) itReturns value

        mass.getItem(key, onSuccess, mock())

        Verify on onSuccess that onSuccess(value) was called
    }

    /**
     * Starting of get all items test cases
     */

    @Test
    fun `get all items onFailed should be called if runtime exception occurs`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.getAllItems() itThrows RuntimeException("Failed")

        mass.getAllItems(mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
    }

    @Test
    fun `get all items onFailed should be called if sql exception occurs`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.getAllItems() itThrows SQLException("Failed")

        mass.getAllItems(mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
    }

    @Test
    fun `get all items onFailed should be called if database is busy error occurred`() =
        runBlockingTest {

            When calling mass.miniAppSecureDatabase.getAllItems() itThrows SQLException(
                DATABASE_BUSY_ERROR
            )

            mass.getAllItems(mock(), onFailed)

            Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageBusyError) was called
        }

    @Test
    fun `get all items onFailed should be called if database is unavailable error occurred`() =
        runBlockingTest {

            When calling mass.miniAppSecureDatabase.getAllItems() itThrows SQLException(
                DATABASE_UNAVAILABLE_ERROR
            )

            mass.getAllItems(mock(), onFailed)

            Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageUnavailableError) was called
        }

    @Test
    fun `get all items onSuccess should be called if item fetched successfully`() =
        runBlockingTest {
            val onSuccess: (Map<String, String>) -> Unit = mock()
            val items: Map<String, String> = mapOf(
                "key1" to "value1",
                "key2" to "value2"
            )

            When calling mass.miniAppSecureDatabase.getAllItems() itReturns items

            mass.getAllItems(onSuccess, mock())

            Verify on onSuccess that onSuccess(items) was called
        }

    @Test
    fun `get all items onSuccess with empty map should be called if no items present`() =
        runBlockingTest {
            val onSuccess: (Map<String, String>) -> Unit = mock()
            val items: Map<String, String> = emptyMap()

            When calling mass.miniAppSecureDatabase.getAllItems() itReturns items

            mass.getAllItems(onSuccess, mock())

            Verify on onSuccess that onSuccess(items) was called
        }

    /**
     * Starting of delete items test cases
     */
    @Test
    fun `delete items onFailed should be called if runtime exception occurs`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.deleteItems(any()) itThrows RuntimeException("Failed")

        mass.deleteItems(mock(), mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
    }

    @Test
    fun `delete items onFailed should be called if sql exception occurs`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.deleteItems(any()) itThrows SQLException("Failed")

        mass.deleteItems(mock(), mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
    }

    @Test
    fun `delete items onFailed should be called if database is busy error occurred`() =
        runBlockingTest {

            When calling mass.miniAppSecureDatabase.deleteItems(any()) itThrows SQLException(
                DATABASE_BUSY_ERROR
            )

            mass.deleteItems(mock(), mock(), onFailed)

            Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageBusyError) was called
        }

    @Test
    fun `delete items onFailed should be called if database is unavailable error occurred`() =
        runBlockingTest {

            When calling mass.miniAppSecureDatabase.deleteItems(any()) itThrows SQLException(
                DATABASE_UNAVAILABLE_ERROR
            )

            mass.deleteItems(mock(), mock(), onFailed)

            Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageUnavailableError) was called
        }

    @Test
    fun `delete items onFailed should be called if database return false for deleting items`() =
        runBlockingTest {

            When calling mass.miniAppSecureDatabase.deleteItems(any()) itReturns false

            mass.deleteItems(mock(), mock(), onFailed)

            Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
        }

    @Test
    fun `delete items onSuccess should be called if database return true for deleting items`() =
        runBlockingTest {

            When calling mass.miniAppSecureDatabase.deleteItems(any()) itReturns true

            mass.deleteItems(mock(), onSuccess, mock())

            Verify on onSuccess that onSuccess() was called
        }

    /**
     * Starting delete whole database tesst cases
     */
    @Test
    fun `delete onFailed should be called if database is unavailable error occurred`() =
        runBlockingTest {

            When calling mass.miniAppSecureDatabase.deleteAllRecords() itThrows SQLException(
                DATABASE_UNAVAILABLE_ERROR
            )

            mass.delete(mock(), onFailed)

            Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageUnavailableError) was called
        }

    @Test
    fun `delete onFailed should be called if database sql exception occurred`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.deleteAllRecords() itThrows SQLException("Failed")

        mass.delete(mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
    }

    @Test
    fun `delete onFailed should be called if database io exception occurred`() = runBlockingTest {

        When calling mass.miniAppSecureDatabase.deleteAllRecords() itThrows IOException("Failed")

        mass.delete(mock(), onFailed)

        Verify on onFailed that onFailed(MiniAppSecureStorageError.secureStorageIOError) was called
    }

    @Test
    @Ignore
    fun `delete onSuccess should be called`() = runBlockingTest {
        val onSuccess: () -> Unit = mock()

        mass.databaseName = TEST_MA_ID
        mass.miniAppSecureDatabase = masdb

        When calling masdb.deleteAllRecords()

        mass.delete(onSuccess, mock())

        Verify on onSuccess that onSuccess() was called
    }
}
