package pl.grzybdev.openmic.client.tools

import android.util.Base64
import com.google.common.io.BaseEncoding
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.zip.Inflater


class Helper {

    companion object {

        fun qUncompress(input: ByteArray): ByteArray {
            val inflater = Inflater()
            inflater.setInput(input, 4,input.size - 4) //Strip off the first 4 bytes - this is a non standard uncompressed size that qCompress adds.

            val outputStream = ByteArrayOutputStream(input.size)
            val buffer = ByteArray(4096)

            while (!inflater.finished()) {
                val count: Int = inflater.inflate(buffer)
                outputStream.write(buffer, 0, count)
            }

            outputStream.close()
            return outputStream.toByteArray()
        }

        fun verifyMD5(expected: String, data: ByteArray): Boolean {
            val expectedCRC = BaseEncoding.base16().lowerCase().encode(Base64.decode(expected, Base64.DEFAULT))
            val calculatedCRC = calculateMD5(data)

            return expectedCRC == calculatedCRC
        }

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
    }
}
