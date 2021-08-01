package pl.grzybdev.openmic.client.interfaces

import pl.grzybdev.openmic.client.dataclasses.packets.BroadcastPacket

interface IBroadcast {
    fun OnDeviceFound(serverInfo: BroadcastPacket)
}