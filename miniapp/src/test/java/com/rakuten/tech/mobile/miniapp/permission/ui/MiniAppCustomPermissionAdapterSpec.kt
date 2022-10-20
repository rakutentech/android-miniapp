package com.rakuten.tech.mobile.miniapp.permission.ui

import android.view.View
import android.widget.Switch
import android.widget.TextView
import org.mockito.kotlin.*
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import com.rakuten.tech.mobile.miniapp.R
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldNotBeNull

@Suppress("LargeClass")
class MiniAppCustomPermissionAdapterSpec {
    private lateinit var permissionAdapter: MiniAppCustomPermissionAdapter
    private val names = arrayListOf<MiniAppCustomPermissionType>()
    private val results = arrayListOf<MiniAppCustomPermissionResult>()
    private val descriptions = arrayListOf<String>()

    @Before
    fun setup() {
        permissionAdapter = spy(MiniAppCustomPermissionAdapter())
    }

    private fun getItemView(): View {
        val itemView: View = mock()
        val permissionName: TextView = mock()
        val permissionDescription: TextView = mock()
        val permissionSwitch: Switch = mock()

        doReturn(permissionName).whenever(itemView).findViewById<TextView>(R.id.permissionText)
        doReturn(permissionDescription).whenever(itemView)
            .findViewById<TextView>(R.id.permissionDescription)
        doReturn(permissionSwitch).whenever(itemView).findViewById<Switch>(R.id.permissionSwitch)
        return itemView
    }

    @Test
    fun `permissionNames should hold arrayListOf MiniAppCustomPermissionType`() {
        names.add(MiniAppCustomPermissionType.USER_NAME)
        doReturn(names).whenever(permissionAdapter).permissionNames
        assertEquals(permissionAdapter.permissionNames.size, names.size)
    }

    @Test
    fun `permissionToggles should hold arrayListOf MiniAppCustomPermissionResult`() {
        results.add(MiniAppCustomPermissionResult.DENIED)
        doReturn(results).whenever(permissionAdapter).permissionToggles
        assertEquals(permissionAdapter.permissionToggles.size, results.size)
    }

    @Test
    fun `permissionDescription should hold arrayListOf String`() {
        descriptions.add("dummy description")
        doReturn(descriptions).whenever(permissionAdapter).permissionDescriptions
        assertEquals(permissionAdapter.permissionDescriptions.size, descriptions.size)
    }

    @Test
    fun `permissionPairs should hold arrayListOf Pair includes type & result`() {
        val permissionPairs =
            arrayListOf<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>()
        permissionPairs.add(
            Pair(
                MiniAppCustomPermissionType.USER_NAME, MiniAppCustomPermissionResult.DENIED
            )
        )
        doReturn(permissionPairs).whenever(permissionAdapter).permissionPairs
        assertEquals(permissionAdapter.permissionPairs.size, permissionPairs.size)
    }

    @Test
    fun `getItemCount should return the same size of permissionNames`() {
        val actual = permissionAdapter.itemCount
        val expected = permissionAdapter.permissionNames.size
        assertEquals(expected, actual)
    }

    @Test
    fun `PermissionViewHolder should register textviews inside it`() {
        val itemView = getItemView()

        val holder = spy(MiniAppCustomPermissionAdapter.PermissionViewHolder(itemView))

        verify(itemView).findViewById<TextView>(R.id.permissionText)
        verify(itemView).findViewById<TextView>(R.id.permissionDescription)
        verify(itemView).findViewById<TextView>(R.id.permissionSwitch)
        itemView.findViewById<TextView>(R.id.permissionText).shouldNotBeNull()
        itemView.findViewById<TextView>(R.id.permissionDescription).shouldNotBeNull()
        itemView.findViewById<TextView>(R.id.permissionSwitch).shouldNotBeNull()
        holder.permissionName.shouldNotBeNull()
        holder.permissionDescription.shouldNotBeNull()
        holder.permissionSwitch.shouldNotBeNull()
        holder.permissionName.shouldBeInstanceOf<TextView>()
        holder.permissionDescription.shouldBeInstanceOf<TextView>()
        holder.permissionSwitch.shouldBeInstanceOf<Switch>()
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun `onBindViewHolder should invoke bindView`() {
        val mockTextView: TextView = mock()
        val mockSwitch: Switch = mock()
        val position = 0
        val mockHolder: MiniAppCustomPermissionAdapter.PermissionViewHolder = mock()

        doReturn(mockTextView).whenever(mockHolder).permissionName
        doReturn(mockTextView).whenever(mockHolder).permissionDescription
        doReturn(mockSwitch).whenever(mockHolder).permissionSwitch

        permissionAdapter.onBindViewHolder(mockHolder, position)

        verify(permissionAdapter).bindView(mockHolder, position)
    }

    @Test
    fun `addPermissionList should add names, results and descriptions as expected`() {
        doNothing().whenever(permissionAdapter).notifyDataSetChanged()

        names.add(MiniAppCustomPermissionType.USER_NAME)
        results.add(MiniAppCustomPermissionResult.DENIED)
        descriptions.add("dummy description")

        permissionAdapter.addPermissionList(names, results, descriptions)

        assertEquals(permissionAdapter.permissionNames.size, names.size)
        assertEquals(permissionAdapter.permissionToggles.size, results.size)
        assertEquals(permissionAdapter.permissionDescriptions.size, descriptions.size)
    }

    @Test
    fun `bindView should register names, results and descriptions to views as expected`() {
        doNothing().whenever(permissionAdapter).notifyDataSetChanged()

        names.add(MiniAppCustomPermissionType.USER_NAME)
        results.add(MiniAppCustomPermissionResult.DENIED)
        descriptions.add("dummy description")

        permissionAdapter.addPermissionList(names, results, descriptions)

        assertEquals(permissionAdapter.permissionNames.size, names.size)
        assertEquals(permissionAdapter.permissionToggles.size, results.size)
        assertEquals(permissionAdapter.permissionDescriptions.size, descriptions.size)

        // val permissionViewHolder = MiniAppCustomPermissionAdapter.PermissionViewHolder(getItemView())
        val permissionViewHolder: MiniAppCustomPermissionAdapter.PermissionViewHolder = mock()
        val permissionName: TextView = mock()
        val permissionDescription: TextView = mock()
        val permissionSwitch: Switch = mock()

        doReturn(permissionName).whenever(permissionViewHolder).permissionName
        doReturn(permissionDescription).whenever(permissionViewHolder).permissionDescription
        doReturn(permissionSwitch).whenever(permissionViewHolder).permissionSwitch

        permissionAdapter.bindView(permissionViewHolder, 0)
        verify(permissionViewHolder).permissionName
        verify(permissionViewHolder).permissionDescription
        verify(permissionViewHolder, times(2)).permissionSwitch
    }

    /**
     * region: parsePermissionName
     */
    @Test
    fun `should parse user name type correctly`() {
        val type = MiniAppCustomPermissionType.USER_NAME
        val expected = "User Name"
        val actual = permissionAdapter.parsePermissionName(type)
        assertEquals(expected, actual)
    }

    @Test
    fun `should parse contact list type correctly`() {
        val type = MiniAppCustomPermissionType.CONTACT_LIST
        val expected = "Contact List"
        val actual = permissionAdapter.parsePermissionName(type)
        assertEquals(expected, actual)
    }

    @Test
    fun `should parse profile photo type correctly`() {
        val type = MiniAppCustomPermissionType.PROFILE_PHOTO
        val expected = "Profile Photo"
        val actual = permissionAdapter.parsePermissionName(type)
        assertEquals(expected, actual)
    }

    @Test
    fun `should parse access token type correctly`() {
        val type = MiniAppCustomPermissionType.ACCESS_TOKEN
        val expected = "Access Token"
        val actual = permissionAdapter.parsePermissionName(type)
        assertEquals(expected, actual)
    }

    @Test
    fun `should parse send message type correctly`() {
        val type = MiniAppCustomPermissionType.SEND_MESSAGE
        val expected = "Send Message"
        val actual = permissionAdapter.parsePermissionName(type)
        assertEquals(expected, actual)
    }

    @Test
    fun `should parse location correctly`() {
        val type = MiniAppCustomPermissionType.LOCATION
        val expected = "Device Location"
        val actual = permissionAdapter.parsePermissionName(type)
        assertEquals(expected, actual)
    }

    @Test
    fun `should parse points correctly`() {
        val type = MiniAppCustomPermissionType.POINTS
        val expected = "Rakuten Points"
        val actual = permissionAdapter.parsePermissionName(type)
        assertEquals(expected, actual)
    }

    @Test
    fun `should parse file download correctly`() {
        val type = MiniAppCustomPermissionType.FILE_DOWNLOAD
        val expected = "File Download"
        val actual = permissionAdapter.parsePermissionName(type)
        assertEquals(expected, actual)
    }

    @Test
    fun `should parse unknown type correctly`() {
        val type = MiniAppCustomPermissionType.UNKNOWN
        val expected = "Unknown"
        val actual = permissionAdapter.parsePermissionName(type)
        assertEquals(expected, actual)
    }
    /** end region */

    /**
     * region: permissionResultToText
     */
    @Test
    fun `should return ALLOWED while isChecked is true at the Switch view`() {
        val isChecked = true
        val expected = MiniAppCustomPermissionResult.ALLOWED
        val actual = permissionAdapter.permissionResultToText(isChecked)
        assertEquals(expected, actual)
    }

    @Test
    fun `should return DENIED while isChecked is false at the Switch view`() {
        val isChecked = false
        val expected = MiniAppCustomPermissionResult.DENIED
        val actual = permissionAdapter.permissionResultToText(isChecked)
        assertEquals(expected, actual)
    }
    /** end region */

    /**
     * region: permissionResultToText
     */
    @Test
    fun `should return true while MiniAppCustomPermissionResult is ALLOWED`() {
        val result = MiniAppCustomPermissionResult.ALLOWED
        val expected = true
        val actual = permissionAdapter.permissionResultToChecked(result)
        assertEquals(expected, actual)
    }

    @Test
    fun `should return true while MiniAppCustomPermissionResult is DENIED`() {
        val result = MiniAppCustomPermissionResult.DENIED
        val expected = false
        val actual = permissionAdapter.permissionResultToChecked(result)
        assertEquals(expected, actual)
    }
    /** end region */
}
