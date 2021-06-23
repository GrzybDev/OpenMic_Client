package pl.grzybdev.openmic.client.network

import java.lang.StringBuilder

class Packet {
    companion object {
        private val magicValue: String = "OMIC";
        private val seperator: Char = 0.toChar();

        fun getHelloPacket(): ByteArray {
            val finalStr: StringBuilder = StringBuilder(magicValue);
            finalStr.append(seperator);
            finalStr.append(ClientPacketType.CLIENT_HELLO.value);
            finalStr.append(seperator);

            return finalStr.toString().toByteArray();
        }
    }
}