package com.rakuten.tech.mobile.testapp.ui.display.preload

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.rakuten.tech.mobile.miniapp.MiniApp
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionResult
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.testapp.databinding.ItemListManifestPermissionBinding
import com.rakuten.tech.mobile.testapp.ui.permission.toReadableName

class PreloadMiniAppPermissionAdapter :
    RecyclerView.Adapter<PreloadMiniAppPermissionAdapter.ViewHolder?>() {

    private var manifestPermissions = mutableListOf<PreloadManifestPermission>()
    var manifestPermissionPairs =
        arrayListOf<Pair<MiniAppCustomPermissionType, MiniAppCustomPermissionResult>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemListManifestPermissionBinding.inflate(layoutInflater, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.permissionName.text = manifestPermissions[position].type.toReadableName()
        holder.permissionSwitch.visibility =
            if (manifestPermissions[position].isRequired) View.GONE else View.VISIBLE
        holder.permissionStatus.visibility =
            if (manifestPermissions[position].isRequired) View.VISIBLE else View.GONE

        holder.permissionSwitch.isChecked =
            permissionResultToChecked(manifestPermissionPairs[position].second)
        if (manifestPermissions[position].reason.isNotEmpty())
            holder.permissionReason.text = manifestPermissions[position].reason

        if (holder.permissionReason.text.isEmpty())
            holder.permissionReason.visibility = View.GONE
        else holder.permissionReason.visibility = View.VISIBLE

        if (manifestPermissions[position].optionalInfo.isNotEmpty())
            holder.permissionOptionalInfo.text = manifestPermissions[position].optionalInfo
        else holder.permissionOptionalInfo.visibility = View.GONE

        holder.permissionSwitch.setOnCheckedChangeListener { _, _ ->
            manifestPermissionPairs.removeAt(position)
            manifestPermissionPairs.add(
                position, Pair(
                    manifestPermissions[position].type,
                    permissionResultToText(holder.permissionSwitch.isChecked)
                )
            )
        }
    }

    override fun getItemCount(): Int = manifestPermissions.size

    fun addManifestPermissionList(
        manifestPermissions: MutableList<PreloadManifestPermission>,
        miniApp: MiniApp,
        miniAppId: String,
        shouldShowDialog: Boolean
    ) {
        manifestPermissionPairs.clear()
        this.manifestPermissions = manifestPermissions
        if (shouldShowDialog) {
            val customPermissionResultList = miniApp.getCustomPermissions(miniAppId).pairValues
            this.manifestPermissions.forEachIndexed { position, (_, _) ->
                manifestPermissionPairs.add(position, customPermissionResultList[position])
            }
        } else {
            this.manifestPermissions.forEachIndexed { position, (type, _) ->
                manifestPermissionPairs.add(
                    position,
                    Pair(type, MiniAppCustomPermissionResult.ALLOWED)
                )
            }
        }
        notifyDataSetChanged()
    }


    inner class ViewHolder(itemView: ItemListManifestPermissionBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val permissionName: TextView = itemView.manifestPermissionName
        val permissionStatus: TextView = itemView.manifestPermissionStatus
        val permissionSwitch: SwitchCompat = itemView.manifestPermissionSwitch
        val permissionReason: TextView = itemView.permissionReason
        val permissionOptionalInfo: TextView = itemView.permissionOptionalInfo
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
