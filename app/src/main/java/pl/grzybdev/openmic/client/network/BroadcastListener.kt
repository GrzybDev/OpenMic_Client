package pl.grzybdev.openmic.client.network

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import java.io.IOException
import java.net.*
import java.util.zip.DataFormatException
import kotlin.concurrent.thread
import kotlin.properties.Delegates


class BroadcastListener {

    private var isRunning: Boolean = false

    private lateinit var broadcastSocket: DatagramSocket
    private lateinit var broadcastThread: Thread

    private var broadcastPort by Delegates.notNull<Int>()
    private lateinit var context: Context

    fun startListening(port: Int, context: Context): Boolean {
        if (isRunning) throw IllegalStateException("startListening: BroadcastListener is already listening, cannot start!")

        this.broadcastPort = port
        this.context = context

        Log.d(javaClass.name, "startListening: Starting Broadcast Thread...")

        try {
            if (!this::broadcastSocket.isInitialized) {
                val address: InetAddress = getBroadcastAddress() ?: return false

                broadcastSocket = DatagramSocket(broadcastPort, address)
                broadcastSocket.broadcast = true
            }
        } catch (e: IOException) {
            Log.e(javaClass.name, e.toString())

            return false
        }

        broadcastThread = thread { handleBroadcasts() }

        return true
    }

    fun stopListening() {
        if (!isRunning) throw IllegalStateException("stopListening: BroadcastListener is not listening, cannot stop!")

        Log.d(javaClass.name, "stopListening: Interrupting broadcast thread...")
        broadcastThread.interrupt()

        try {
            broadcastThread.join()
        } catch (e: InterruptedException) {
            Log.d(javaClass.name, "stopListening: Error when tried to join broadcastThread ($e)")
        }
    }

    private fun handleBroadcasts() {
        isRunning = true

        Log.d(
            javaClass.name,
            "handleBroadcasts: Listening for broadcasts on ${broadcastSocket.localAddress}:${broadcastSocket.localPort}..."
        )

        while (!broadcastThread.isInterrupted) {
            Log.d(javaClass.name, "handleBroadcasts: Waiting for broadcast packet...")

            val receivedPacket = DatagramPacket(ByteArray(DEFAULT_BUFFER_SIZE), DEFAULT_BUFFER_SIZE)

            try {
                broadcastSocket.receive(receivePacket)
            } catch (e: SocketTimeoutException) {
                // Do nothing
            } catch (e: IOException) {
                Log.e(javaClass.name, e.toString())
            }

            try {
                Packet.getPacket(receivePacket.data, receivePacket.length)
            } catch (e: DataFormatException) {
                // Ignore corrupted packet
                Log.w(javaClass.name, "Failed to parse packet ($e), ignoring...")
            }
        }

        Log.d(javaClass.name, "Finished Broadcast Thread.")
        isRunning = false
    }

    private fun getBroadcastAddress(): InetAddress? {
        val wifiMgr =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val dhcp = wifiMgr.dhcpInfo

        val inetAddress = InetAddress.getByAddress(getIPBytes(dhcp.ipAddress))
        val networkInterface: NetworkInterface = NetworkInterface.getByInetAddress(inetAddress)
            ?: return null

        for (address in networkInterface.interfaceAddresses) {
            if (address.broadcast != null) return address.broadcast
        }

        return null
    }

    private fun getIPBytes(value: Int): ByteArray {
        val quads = ByteArray(4)
        for (k in 0..3) quads[k] = (value shr k * 8).toByte()
        return quads
    }
}