package com.rakuten.tech.mobile.miniapp.permission.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.recyclerview.widget.RecyclerView
import com.rakuten.tech.mobile.miniapp.R
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType

/**
 * A RecyclerView Adapter to bind each custom permission view to be displayed in the default UI.
 */
internal class MiniAppCustomPermissionAdapter :
    RecyclerView.Adapter<MiniAppCustomPermissionAdapter.PermissionViewHolder?>() {

    @VisibleForTesting
    var permissionNames = ArrayList<MiniAppCustomPermissionType>()

    @VisibleForTesting
    var permissionToggles = ArrayList<MiniAppCustomPermissionResult>()

    @VisibleForTesting
    var permissionDescriptions = ArrayList<String>()

    @VisibleForTesting
    var permissionPairs =
        arrayListOf<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermissionViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_custom_permission, null)
        return PermissionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PermissionViewHolder, position: Int) {
        bindView(holder, position)
    }

    override fun getItemCount(): Int = permissionNames.size

    fun addPermissionList(
        names: ArrayList<MiniAppCustomPermissionType>,
        results: ArrayList<MiniAppCustomPermissionResult>,
        descriptions: ArrayList<String>
    ) {
        permissionNames = names
        permissionToggles = results
        permissionNames.forEachIndexed { position, _ ->
            permissionPairs.add(
                position,
                Pair(permissionNames[position], permissionToggles[position])
            )
        }
        permissionDescriptions = descriptions
        notifyDataSetChanged()
    }

    /**
     * A RecyclerView ViewHolder contains View for each custom permission to be bound in Adapter.
     */
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    inner class PermissionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val permissionName: TextView = itemView.findViewById(R.id.permissionText)
        val permissionDescription: TextView = itemView.findViewById(R.id.permissionDescription)
        val permissionSwitch: Switch = itemView.findViewById(R.id.permissionSwitch)
    }

    @VisibleForTesting
    fun bindView(holder: PermissionViewHolder, position: Int) {
        holder.permissionName.text = parsePermissionName(permissionNames[position])
        holder.permissionDescription.text = permissionDescriptions[position]
        holder.permissionSwitch.isChecked = permissionResultToChecked(permissionToggles[position])

        holder.permissionSwitch.setOnCheckedChangeListener { _, _ ->
            permissionPairs.removeAt(position)
            val newItem = Pair(
                permissionNames[position],
                permissionResultToText(holder.permissionSwitch.isChecked)
            )
            permissionPairs.add(position, newItem)
        }
    }

    @VisibleForTesting
    fun parsePermissionName(type: MiniAppCustomPermissionType?): String {
        return when (type) {
            MiniAppCustomPermissionType.USER_NAME -> "User Name"
            MiniAppCustomPermissionType.CONTACT_LIST -> "Contact List"
            MiniAppCustomPermissionType.PROFILE_PHOTO -> "Profile Photo"
            MiniAppCustomPermissionType.LOCATION -> "Device Location"
            else -> "Unknown"
        }
    }

    @VisibleForTesting
    fun permissionResultToText(isChecked: Boolean): MiniAppCustomPermissionResult {
        if (isChecked)
            return MiniAppCustomPermissionResult.ALLOWED

        return MiniAppCustomPermissionResult.DENIED
    }

    @VisibleForTesting
    fun permissionResultToChecked(result: MiniAppCustomPermissionResult): Boolean {
        if (result == MiniAppCustomPermissionResult.ALLOWED)
            return true

        return false
    }
}
