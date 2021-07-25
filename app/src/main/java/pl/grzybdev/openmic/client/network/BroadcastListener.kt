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

        try {
            broadcastSocket = DatagramSocket(port, getBroadcastAddress())
            broadcastSocket.broadcast = true
            broadcastSocket.soTimeout = 1000

            Log.d(javaClass.name, "Listening for broadcasts on " + broadcastSocket.getLocalAddress() + "...");
        } catch (e: IOException) {
            Log.e(javaClass.name, e.toString());

            return false;
        }

        broadcastThread = thread(start = true) { handleBroadcasts() }

        return true
    }

    fun stopListening() {
        if (!isRunning) throw IllegalStateException("BroadcastListener is NOT running! Cannot stop.")

        broadcastThread.interrupt()
    }

    private fun getBroadcastAddress(): InetAddress? {
        val wifiMgr = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val dhcp = wifiMgr.dhcpInfo

        val inetAddress = InetAddress.getByAddress(getIPBytes(dhcp.ipAddress))
        val networkInterface: NetworkInterface = NetworkInterface.getByInetAddress(inetAddress)
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

        isRunning = false;
    }
}