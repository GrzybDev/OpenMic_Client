package pl.grzybdev.openmic.client.adapters

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import pl.grzybdev.openmic.client.R
import pl.grzybdev.openmic.client.activities.MainActivity
import pl.grzybdev.openmic.client.activities.fragments.main.ConnectingScreen
import pl.grzybdev.openmic.client.dataclasses.packets.BroadcastPacket
import pl.grzybdev.openmic.client.dataclasses.ui.ConnectingScreenArgs
import pl.grzybdev.openmic.client.enums.VersionStatus
import pl.grzybdev.openmic.client.enums.ui.MainFragment
import pl.grzybdev.openmic.client.tools.ServerHelper
import kotlin.concurrent.thread


class DeviceListAdapter(val context: Context, data: List<BroadcastPacket>) : RecyclerView.Adapter<DeviceListAdapter.ViewHolder>() {

    private var mData: List<BroadcastPacket> = data
    private var mInflater: LayoutInflater = LayoutInflater.from(context)

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = mInflater.inflate(R.layout.devices_list, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the views in each row
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: BroadcastPacket = mData[position]

        val icon = ServerHelper.getSystemIcon(item.DeviceInfo.KernelType)
        holder.deviceOsIcon.setImageDrawable(AppCompatResources.getDrawable(context, icon))
        holder.deviceOs.text = item.DeviceInfo.OperatingSystem

        if (ServerHelper.isOfficialServer(item.ServerInfo.AppID)) {
            val versionStatus: String = when (ServerHelper.getVersionStatus(item.ServerInfo.Version)) {
                VersionStatus.OK -> context.getString(R.string.device_info_app_version_ok)
                VersionStatus.UPDATE_AVAILABLE -> context.getString(R.string.device_info_app_version_update_available)
                VersionStatus.OUTDATED -> context.getString(R.string.device_info_app_version_outdated)
                VersionStatus.FUTURE -> context.getString(R.string.device_info_app_version_future)
            }

            val versionString = context.getString(R.string.device_info_app_version, versionStatus, item.ServerInfo.Version)

            holder.appId.text = context.getString(R.string.device_info_app_official)
            holder.appId.setTypeface(null, Typeface.BOLD)

            holder.deviceTrustStatus.text = context.getString(R.string.device_info_trust, context.getString(R.string.device_info_trust_not_trusted), versionString)
        } else {
            holder.appId.text = item.ServerInfo.AppID
            holder.deviceTrustStatus.text = item.ServerInfo.Version
        }

        holder.deviceHostname.text = item.Hostname
        holder.deviceAddress.text = item.Addresses[0]

        holder.connectBtn.setOnClickListener {
            Log.d(javaClass.name, "${item.Addresses[0]}:${item.Port}")

            thread { (context as MainActivity).stopAllManagers() }
            (context as MainActivity).changeFragment(MainFragment.Connecting)

            val args = ConnectingScreenArgs(item.Addresses, item.Port, item.Hostname)
            ConnectingScreen.connectingContext.connect(args)
        }
    }

    // total number of rows
    override fun getItemCount(): Int {
        return mData.size
    }

    // stores and recycles views as they are scrolled off screen
    class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceOsIcon: ImageView = itemView.findViewById(R.id.deviceOsIcon)
        val deviceOs: TextView = itemView.findViewById(R.id.deviceOs)
        val deviceHostname: TextView = itemView.findViewById(R.id.deviceHostname)
        val deviceAddress: TextView = itemView.findViewById(R.id.deviceAddress)
        val appId: TextView = itemView.findViewById(R.id.appId)
        val connectBtn: Button = itemView.findViewById(R.id.connectBtn)
        val deviceTrustStatus: TextView = itemView.findViewById(R.id.deviceTrustStatus)
    }
}