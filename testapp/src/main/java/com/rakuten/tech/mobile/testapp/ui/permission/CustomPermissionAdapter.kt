package com.rakuten.tech.mobile.testapp.ui.permission

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ItemListCustomPermissionBinding
import kotlinx.android.synthetic.main.item_list_custom_permission.view.*

class CustomPermissionAdapter : RecyclerView.Adapter<CustomPermissionAdapter.ViewHolder?>() {
    private var permissionNames = ArrayList<MiniAppCustomPermissionType>()
    private var permissionToggles = ArrayList<MiniAppCustomPermissionResult>()
    private var permissionDescription = ArrayList<String>()
    var permissionPairs =
        arrayListOf<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemListCustomPermissionBinding.inflate(layoutInflater, parent, false)

        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.permissionName.text = toReadable(permissionNames[position])
        holder.permissionSwitch.isChecked =
            permissionResultToChecked(permissionToggles[position])

        holder.permissionSwitch.setOnCheckedChangeListener { _, _ ->
            permissionPairs.removeAt(position)
            permissionPairs.add(
                position,
                Pair(
                    permissionNames[position],
                    permissionResultToText(holder.permissionSwitch.isChecked)
                )
            )
        }

        holder.permissionDescription.text = permissionDescription[position]
    }

    override fun getItemCount(): Int = permissionNames.size

    fun addPermissionList(
        names: ArrayList<MiniAppCustomPermissionType>,
        results: ArrayList<MiniAppCustomPermissionResult>,
        description: ArrayList<String>
    ) {
        permissionNames = names
        permissionToggles = results
        permissionNames.forEachIndexed { position, _ ->
            permissionPairs.add(
                position,
                Pair(permissionNames[position], permissionToggles[position])
            )
        }
        permissionDescription = description
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val permissionName: TextView = itemView.permissionText
        val permissionDescription: TextView = itemView.permissionDescription
        val permissionSwitch: SwitchCompat = itemView.permissionSwitch
    }

    private fun toReadable(type: MiniAppCustomPermissionType): String {
        return when (type) {
            MiniAppCustomPermissionType.USER_NAME -> "User Name"
            MiniAppCustomPermissionType.CONTACT_LIST -> "Contact List"
            MiniAppCustomPermissionType.PROFILE_PHOTO -> "Profile Photo"
            else -> "Unknown"
        }
    }

    private fun permissionResultToText(isChecked: Boolean): MiniAppCustomPermissionResult {
        if (isChecked)
            return MiniAppCustomPermissionResult.ALLOWED

        return MiniAppCustomPermissionResult.DENIED
    }

    private fun permissionResultToChecked(result: MiniAppCustomPermissionResult): Boolean {
        if (result == MiniAppCustomPermissionResult.ALLOWED)
            return true

        return false
    }
}
