package pl.grzybdev.openmic.client.managers

import android.content.BroadcastReceiver
import android.content.Intent
import pl.grzybdev.openmic.client.dataclasses.packets.BroadcastPacket

abstract class BaseManager {

    abstract val broadcastReceiver: BroadcastReceiver
    abstract var discoveredDevices: List<BroadcastPacket>

    abstract fun startManager()
    abstract fun stopManager()
    abstract fun updateState(intent: Intent?)
}