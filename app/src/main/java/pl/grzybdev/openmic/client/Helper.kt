package pl.grzybdev.openmic.client

import android.util.Base64
import com.google.common.io.BaseEncoding
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class Helper {

    companion object {
        fun calculateMD5(data: ByteArray): String {
            try {
                // Create Md5 Hash
                val digest: MessageDigest = MessageDigest.getInstance("MD5")
                digest.update(data)

                val messageDigest = digest.digest()

                // Create Hex String
                val hexString = StringBuffer()

                messageDigest.forEach {
                    byte ->
                    run {
                        val num = Integer.toHexString(0xFF and byte.toInt())

                        if (num.length < 2)
                            hexString.append("0$num")
                        else
                            hexString.append(num)
                    }
                }

                return hexString.toString()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }

            return ""
        }

        fun verifyMD5(expected: String, data: ByteArray): Boolean {
            val expectedCRC = BaseEncoding.base16().lowerCase().encode(Base64.decode(expected, Base64.DEFAULT))
            val calculatedCRC = calculateMD5(data)

            return expectedCRC == calculatedCRC
        }
    }
}
