package pl.grzybdev.openmic.client.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pl.grzybdev.openmic.client.R
import pl.grzybdev.openmic.client.dataclasses.packets.BroadcastPacket


class DeviceListAdapter(context: Context, data: List<BroadcastPacket>) : RecyclerView.Adapter<DeviceListAdapter.ViewHolder>() {

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
    }

    // total number of rows
    override fun getItemCount(): Int {
        return mData.size
    }

    // stores and recycles views as they are scrolled off screen
    class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceOsIcon: ImageView = itemView.findViewById(R.id.deviceOsIcon)
        val deviceServerAppInfo: TextView = itemView.findViewById(R.id.deviceServerAppInfo)
        val deviceHostname: TextView = itemView.findViewById(R.id.deviceHostname)
        val deviceAddress: TextView = itemView.findViewById(R.id.deviceAddress)
        val connectBtn: Button = itemView.findViewById(R.id.connectBtn)
        val deviceTrustStatus: TextView = itemView.findViewById(R.id.deviceTrustStatus)
    }
}