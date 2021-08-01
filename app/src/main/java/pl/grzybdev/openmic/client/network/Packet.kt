package pl.grzybdev.openmic.client.network

import android.util.Base64
import android.util.Log
import com.google.common.primitives.Ints
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import pl.grzybdev.openmic.client.tools.Helper
import pl.grzybdev.openmic.client.dataclasses.packets.BroadcastPacket
import pl.grzybdev.openmic.client.dataclasses.packets.TransportPacket
import pl.grzybdev.openmic.client.enums.PacketType
import java.util.zip.DataFormatException


class Packet {

    companion object {
        private const val MAGIC_VALUE = "OMIC"
        private val PACKET_TYPE_MAP = PacketType.values().associateBy(PacketType::type)

        fun getPacket(data: ByteArray, length: Int): TransportPacket  {
            Log.d(Packet::class.java.name, "getPacket: Parsing packet...")

            val rawData = String(data, 0, length).split("\n")

            if (!rawData[0].startsWith(MAGIC_VALUE)) {
                Log.w(Packet::class.java.name, "getPacket: Not OpenMic packet, ignoring...")
                throw DataFormatException("getPacket: Not OpenMic packet")
            }

            val packetTypeInt = Ints.fromByteArray(Base64.decode(rawData[0].substring(MAGIC_VALUE.length), Base64.DEFAULT))
            val packetType = PACKET_TYPE_MAP[packetTypeInt]

            if (rawData.size != 4)
                throw DataFormatException("getPacket: Packet has valid magic value, but it doesn't contain valid data")

            var packetData = rawData[1].toByteArray()
            val packetLengths = Base64.decode(rawData[2], Base64.DEFAULT)
            val packetChecksums = rawData[3].split(0.toChar())

            for (multiplier in 3 downTo 1) {
                val len = Ints.fromByteArray(packetLengths.copyOfRange(4 * (multiplier - 1), 4 * multiplier))

                if (len == packetData.size) {
                    if (Helper.verifyMD5(packetChecksums[multiplier - 1], packetData)) {
                        when (multiplier) {
                            3 -> packetData = Base64.decode(packetData, Base64.DEFAULT)
                            2 -> packetData = Helper.qUncompress(packetData)
                        }
                    } else {
                        Log.d(Packet::class.java.name, "getPacket: Invalid CRC (Iteration: ${4 - multiplier}, Expected: ${packetChecksums[multiplier - 1]}, Calculated: ${Helper.calculateMD5(packetData)})")
                        throw DataFormatException("Invalid CRC")
                    }
                } else {
                    Log.d(Packet::class.java.name, "getPacket: Invalid data length (Iteration: ${4 - multiplier}, Expected: $len, Got: ${packetData.size})")
                    throw DataFormatException("Invalid data length")
                }
            }

            val packet: TransportPacket

            when (packetType) {
                PacketType.BROADCAST_PACKET -> packet = Json.decodeFromString<BroadcastPacket>(String(packetData))
                else -> {
                    Log.d(Packet::class.java.name, "getPacket: Unknown packet type!")
                    throw DataFormatException("Unknown packet type!")
                }
            }

            Log.d(Packet::class.java.name, "getPacket: Successfully parsed packet!")
            return packet
        }
    }

}