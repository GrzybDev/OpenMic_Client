package pl.grzybdev.openmic.client.network

import android.util.Log
import com.google.common.primitives.Ints
import pl.grzybdev.openmic.client.Helper
import java.util.zip.DataFormatException

class Packet {

    companion object {
        private const val MAGIC_VALUE = "OMIC";

        fun getPacket(data: ByteArray, length: Int)  {
            Log.d(Packet::javaClass.name, "getPacket: Parsing packet...")

            val rawData = String(data, 0, length).split("\n");

            if (rawData[0] != MAGIC_VALUE) {
                Log.w(Packet::javaClass.name, "getPacket: Not OpenMic packet, ignoring...")
                return
            }

            if (rawData.size != 4) throw DataFormatException("getPacket: Packet has valid magic value, but it doesn't contain valid data")

            var packetData = rawData[1]
            val packetLengths = rawData[2]
            val packetChecksums = rawData[3].split(0.toChar())

            for (multiplier in 3 downTo 1) {
                val len = Ints.fromByteArray(packetLengths.substring(4 * (multiplier - 1), 4 * multiplier).toByteArray().reversedArray())

                if (len == packetData.length) {
                    Log.d(Packet::javaClass.name, "Correct size")

                    if (Helper.verifyMD5(packetChecksums[multiplier - 1], packetData.toByteArray())) {
                        Log.d(Packet::javaClass.name, "Valid CRC")
                        TODO("Continue interpreting byte")
                    } else {
                        Log.d(Packet::javaClass.name, "Invalid CRC")
                    }
                } else {
                    Log.d(Packet::javaClass.name, "Incorrect size")
                }
            }
        }
    }

}