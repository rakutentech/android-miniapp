package com.rakuten.tech.mobile.miniapp.permission

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rakuten.tech.mobile.miniapp.R
import kotlinx.android.synthetic.main.item_custom_permission.view.permissionDescription
import kotlinx.android.synthetic.main.item_custom_permission.view.permissionSwitch
import kotlinx.android.synthetic.main.item_custom_permission.view.permissionText

internal class MiniAppCustomPermissionAdapter :
    RecyclerView.Adapter<MiniAppCustomPermissionAdapter.ViewHolder?>() {

    private var permissionNames = ArrayList<MiniAppCustomPermissionType>()
    private var permissionToggles = ArrayList<MiniAppCustomPermissionResult>()
    private var permissionDescription = ArrayList<String>()
    var permissionPairs =
        arrayListOf<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_custom_permission, null)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.permissionName.text = parsePermissionName(permissionNames[position])
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

        if (permissionDescription.isNotEmpty())
            holder.permissionDescription.text = permissionDescription[position]

        if (holder.permissionDescription.text.isEmpty())
            holder.permissionDescription.visibility = View.GONE
        else holder.permissionDescription.visibility = View.VISIBLE
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

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val permissionName: TextView = itemView.permissionText
        val permissionDescription: TextView = itemView.permissionDescription
        val permissionSwitch: Switch = itemView.permissionSwitch
    }

    private fun parsePermissionName(type: MiniAppCustomPermissionType): String {
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
