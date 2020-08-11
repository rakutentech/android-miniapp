package com.rakuten.tech.mobile.miniapp.permission.widget

import android.content.Context
import android.content.DialogInterface
import android.view.ViewGroup
import com.nhaarman.mockitokotlin2.mock
import org.amshove.kluent.shouldBe
import org.junit.Before
import org.junit.Test

class PermissionDialogSpec {
    private lateinit var builder: PermissionDialog.Builder

    @Before
    fun setUp() {
        builder = PermissionDialog.Builder()
    }

    @Test
    fun `build function should return the same instance of Builder class`() {
        val mockContext: Context = mock()
        builder.build(mockContext) shouldBe builder
    }

    @Test
    fun `setView function should return the same instance of Builder class`() {
        val mockViewGroup: ViewGroup = mock()
        builder.setView(mockViewGroup) shouldBe builder
    }

    @Test
    fun `setListener function should return the same instance of Builder class`() {
        val mockListener: DialogInterface.OnClickListener = mock()
        builder.setListener(mockListener) shouldBe builder
    }
}
