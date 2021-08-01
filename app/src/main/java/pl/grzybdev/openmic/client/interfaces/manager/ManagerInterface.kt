package pl.grzybdev.openmic.client.interfaces.manager

import pl.grzybdev.openmic.client.dataclasses.packets.BroadcastPacket
import pl.grzybdev.openmic.client.enums.manager.ConnectionType
import pl.grzybdev.openmic.client.enums.manager.ManagerStatus

interface ManagerInterface {
    fun updateStatus(type: ConnectionType, status: ManagerStatus)

    fun onDeviceDiscovered(type: ConnectionType, data: BroadcastPacket)
}