package pl.grzybdev.openmic.client.network

import pl.grzybdev.openmic.client.BuildConfig
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
            finalStr.append(BuildConfig.VERSION_NAME + "\n" + BuildConfig.VERSION_CODE);
            finalStr.append(seperator);
            finalStr.append(BuildConfig.DEBUG);
            finalStr.append(seperator);
            finalStr.append("Android\n" + android.os.Build.VERSION.RELEASE);
            finalStr.append(seperator);
            finalStr.append(android.os.Build.MANUFACTURER + "\n" + android.os.Build.MODEL);
            finalStr.append(seperator);
            finalStr.append(android.os.Build.FINGERPRINT);
            finalStr.append(seperator);

            return finalStr.toString().toByteArray();
        }
    }
}