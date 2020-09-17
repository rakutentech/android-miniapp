package com.rakuten.tech.mobile.miniapp.storage

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class CachedMiniAppVerifierSpec {

    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val prefs = context.getSharedPreferences("test-cache", Context.MODE_PRIVATE)
    private val dispatcher = TestCoroutineDispatcher()

    private val verifier = CachedMiniAppVerifier(prefs, dispatcher)
    private val id = "test-miniapp-id"

    @Test
    fun `should verify hash for files`() = runBlockingTest {
        val folder = tempFolder.newFolder("mini-app-folder")
        File(folder, "file1.html").writeText("test content")
        File(folder, "file2.html").writeText("test content")

        verifier.storeHashAsync(id, folder)

        verifier.verify(id, folder) shouldBe true
    }

    @Test
    fun `should fail to verify hash when files have been modified`() = runBlockingTest {
        val folder = tempFolder.newFolder("mini-app-folder")
        val file = File(folder, "file1.html")
        file.writeText("test content")

        verifier.storeHashAsync(id, folder)
        file.writeText("modified content")

        verifier.verify(id, folder) shouldBe false
    }

    @Test
    fun `should fail to verify hash when files in subdirectories have been modified`() = runBlockingTest {
        val folder = tempFolder.newFolder("mini-app-folder", "sub-folder")
        val file = File(folder, "file1.html")
        file.writeText("test content")

        verifier.storeHashAsync(id, folder)
        file.writeText("modified content")

        verifier.verify(id, folder) shouldBe false
    }

    @Test
    fun `should fail to verify hash when mini app files are deleted`() = runBlockingTest {
        val folder = tempFolder.newFolder("mini-app-folder")
        val file = File(folder, "file1.html")
        file.writeText("test content")

        verifier.storeHashAsync(id, folder)
        folder.deleteRecursively()

        verifier.verify(id, folder) shouldBe false
    }
}
