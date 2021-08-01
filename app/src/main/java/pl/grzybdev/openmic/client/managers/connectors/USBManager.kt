package pl.grzybdev.openmic.client.managers.connectors

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import pl.grzybdev.openmic.client.activities.MainActivity
import pl.grzybdev.openmic.client.dataclasses.packets.BroadcastPacket
import pl.grzybdev.openmic.client.enums.manager.ConnectionType
import pl.grzybdev.openmic.client.enums.manager.ManagerStatus
import pl.grzybdev.openmic.client.managers.BaseManager

class USBManager(private val context: Context) : BaseManager() {
    override val broadcastReceiver: BroadcastReceiver
        get() = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null) {
                    updateState(intent)
                }
            }
        }

    override var discoveredDevices: List<BroadcastPacket> = emptyList()

    override fun startManager() {
        Log.d(javaClass.name, "startManager: Registering batteryStatusReceiver...")
        context.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        Log.d(javaClass.name, "startManager: Successfully registered batteryStatusReceiver")
    }

    override fun stopManager() {
        Log.d(javaClass.name, "stopManager: Stopping USBManager...")

        try {
            Log.d(javaClass.name, "stopManager: Unregistering batteryStatusReceiver...")
            context.unregisterReceiver(broadcastReceiver)
            Log.d(javaClass.name, "stopManager: Successfully unregistered batteryStatusReceiver!")
        } catch (e: IllegalArgumentException) {
            Log.w(javaClass.name, "stopManager: Cannot unregister batteryStatusReceiver, because it's not registered!")
        }
    }

    override fun updateState(intent: Intent?) {
        Log.d(javaClass.name, "updateUSBStatus: USB status has been updated")

        val status: Int = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: return
        val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        // How are we charging?
        val chargePlug: Int = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val usbCharge: Boolean = chargePlug == BatteryManager.BATTERY_PLUGGED_USB

        (context as MainActivity).updateStatus(ConnectionType.USB, if (isCharging && usbCharge) ManagerStatus.Ready else ManagerStatus.NotReady)
    }

}