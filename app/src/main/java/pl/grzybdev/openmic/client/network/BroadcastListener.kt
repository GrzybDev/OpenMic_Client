package pl.grzybdev.openmic.client.network

import android.content.Context
import android.net.wifi.WifiManager
import android.system.Os.socket
import android.util.Log
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface
import kotlin.concurrent.thread


class BroadcastListener {

    private var isRunning: Boolean = false

    private lateinit var broadcastSocket: DatagramSocket
    private lateinit var broadcastThread: Thread

    private lateinit var context: Context

    fun startListening(port: Int, context: Context): Boolean {
        if (isRunning) throw IllegalStateException("BroadcastListener is already listening!")

        this.context = context

        Log.d(javaClass.name, "Starting Broadcast Thread...")

        try {
            if (!this::broadcastSocket.isInitialized) {
                broadcastSocket = DatagramSocket(port, getBroadcastAddress())
                broadcastSocket.broadcast = true
                broadcastSocket.soTimeout = 1000
            }
        } catch (e: IOException) {
            Log.e(javaClass.name, e.toString());

            return false;
        }

        broadcastThread = thread() { handleBroadcasts() }

        return true
    }

    fun stopListening() {
        if (this::broadcastThread.isInitialized) {
            Log.d(javaClass.name, "Interrupting broadcast thread...")
            broadcastThread.interrupt()
        } else {
            Log.w(javaClass.name, "Cannot interrupt broadcast thread because it's not initialized yet!")
        }
    }

    private fun getBroadcastAddress(): InetAddress? {
        val wifiMgr = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
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

    private fun handleBroadcasts() {
        isRunning = true

        Log.d(javaClass.name, "Listening for broadcasts on ${broadcastSocket.localAddress}:${broadcastSocket.localPort}...");

        while (!broadcastThread.isInterrupted) {
            Log.d(javaClass.name, "Waiting for broadcast packet...")

            val receiveData = ByteArray(1024)
            val receivePacket = DatagramPacket(receiveData, receiveData.size)

            try {
                broadcastSocket.receive(receivePacket)
            } catch (e: IOException) {
                Log.e(javaClass.name, e.toString())
            }

            val broadcast = String(receivePacket.data)

            if (broadcast.startsWith("OpenMic")) {
                // We got OpenMic broadcast packet
                Log.d(javaClass.name, "Got OpenMic broadcast packet! Parsing...")
                Log.d(javaClass.name, broadcast)
            }
        }

        Log.d(javaClass.name, "Finished Broadcast Thread.")

        isRunning = false;
    }
}