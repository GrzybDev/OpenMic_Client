package pl.grzybdev.openmic.client.dataclasses.packets

import kotlinx.serialization.Serializable
import pl.grzybdev.openmic.client.dataclasses.ServerInfo

@Serializable
data class BroadcastPacket(val ServerInfo: ServerInfo, val Hostname: String, val Addresses: List<String>, val Port: UShort) :
    TransportPacket()
