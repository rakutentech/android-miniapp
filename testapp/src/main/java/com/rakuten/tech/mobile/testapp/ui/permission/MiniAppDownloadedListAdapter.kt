package com.rakuten.tech.mobile.testapp.ui.permission

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rakuten.tech.mobile.miniapp.MiniAppInfo
import com.rakuten.tech.mobile.miniapp.testapp.databinding.DownloadedItemListMiniappBinding
import com.rakuten.tech.mobile.testapp.helper.load

class MiniAppDownloadedListAdapter(private val miniAppListener: MiniAppDownloadedListener) :
    RecyclerView.Adapter<MiniAppDownloadedListAdapter.ViewHolder?>() {
    private var miniApps: List<MiniAppInfo> = listOf()
    private var miniAppPermissions: List<String> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DownloadedItemListMiniappBinding.inflate(layoutInflater, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.miniAppIcon.load(holder.miniAppRoot.context, miniApps[position].icon)
        holder.miniAppName.text = miniApps[position].displayName
        holder.miniAppPermissions.text = miniAppPermissions[position]
        holder.miniAppRoot.setOnClickListener {
            miniAppListener.onMiniAppItemClick(miniApps[position])
        }
    }

    override fun getItemCount(): Int = miniApps.size

    fun addDownloadedList(apps: List<MiniAppInfo>, permissions: List<String>) {
        miniApps = apps
        miniAppPermissions = permissions
        notifyDataSetChanged()
    }

    interface MiniAppDownloadedListener {
        fun onMiniAppItemClick(miniAppInfo: MiniAppInfo)
    }

    inner class ViewHolder(itemView: DownloadedItemListMiniappBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val miniAppRoot: View = itemView.downloadedMiniappRoot
        val miniAppIcon: ImageView = itemView.downloadedMiniappIcon
        val miniAppName: TextView = itemView.downloadedMiniappName
        val miniAppPermissions: TextView = itemView.downloadedMiniappPermissions
    }
}
