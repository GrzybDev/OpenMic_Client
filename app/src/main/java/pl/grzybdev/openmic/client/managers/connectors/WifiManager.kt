package pl.grzybdev.openmic.client.managers.connectors

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import pl.grzybdev.openmic.client.OpenMic
import pl.grzybdev.openmic.client.activities.MainActivity
import pl.grzybdev.openmic.client.activities.fragments.main.DevicesListScreen
import pl.grzybdev.openmic.client.dataclasses.packets.BroadcastPacket
import pl.grzybdev.openmic.client.enums.manager.ConnectionType
import pl.grzybdev.openmic.client.enums.manager.ManagerStatus
import pl.grzybdev.openmic.client.managers.BaseManager
import kotlin.concurrent.fixedRateTimer

class WifiManager(private val context: Context) : BaseManager() {

    companion object {
        const val WIFI_STATE_PERMISSION = "android.permission.ACCESS_FINE_LOCATION"
        const val WIFI_CHECK_INTENT = "android.net.conn.CONNECTIVITY_CHANGE"
        const val ADD_INDEX = 0
        const val DEVICE_TIMEOUT = 30
    }

    override val broadcastReceiver: BroadcastReceiver
        get() = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                updateState(intent)
            }
        }

    override var discoveredDevices: MutableList<BroadcastPacket> = mutableListOf()
    private var devicesLastHeartBeat: MutableMap<String, Long> = mutableMapOf()

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var connectivityManagerCallback: ConnectivityManager.NetworkCallback
    private lateinit var wifiManager: WifiManager

    private lateinit var devicesListScreenContext: DevicesListScreen

    override var isRunning = false
    override var lastState = false

    override fun startManager() {
        isRunning = true

        Log.d(javaClass.name, "startManager: Initializing WiFi Manager...")

        if (ContextCompat.checkSelfPermission(context, WIFI_STATE_PERMISSION) == PackageManager.PERMISSION_DENIED) {
            Log.d(javaClass.name, "startManager: Disabling WiFi, because we don't have access to device location...")
            (context as MainActivity).updateStatus(ConnectionType.WiFi, ManagerStatus.Disabled)

            return
        }

        connectivityManager = context.getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
        wifiManager = context.applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager

        updateState(null)
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> connectivityManager.registerDefaultNetworkCallback(
                getConnectivityManagerCallback()
            )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> lollipopNetworkAvailableRequest()
            else -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    context.registerReceiver(broadcastReceiver, IntentFilter(WIFI_CHECK_INTENT))
                }
            }
        }

        Log.d(javaClass.name, "initWifiListener: Finished initializing Wi-Fi listener!")
    }

    override fun stopManager() {
        isRunning = false

        Log.d(javaClass.name, "stopManager: Stopping WifiManager...")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && this::connectivityManager.isInitialized) {
            Log.d(javaClass.name, "stopManager: Unregistering connectivityManagerCallback...")
            connectivityManager.unregisterNetworkCallback(connectivityManagerCallback)
            Log.d(javaClass.name, "stopManager: Successfully unregistered connectivityManagerCallback!")
        }

        try {
            Log.d(javaClass.name, "stopManager: Unregistering networkReceiver...")
            context.unregisterReceiver(broadcastReceiver)
            Log.d(javaClass.name, "stopManager: Successfully unregistered networkReceiver!")
        } catch (e: IllegalArgumentException) {
            Log.w(javaClass.name, "stopManager: Cannot unregister networkReceiver, because it's not registered!")
        }

        updateState(null)
    }

    override fun updateState(intent: Intent?) {
        Log.d(javaClass.name, "updateState: Updating Wi-Fi state...")

        if (wifiManager.isWifiEnabled) {
            Log.d(javaClass.name, "updateState: WiFi adapter is ON")

            val wifiInfo = wifiManager.connectionInfo

            if (wifiInfo.networkId != -1 && isRunning) {
                if (lastState) return
                else lastState = true

                (context as MainActivity).updateStatus(ConnectionType.WiFi, ManagerStatus.Ready)
                (context.application as OpenMic).broadcastListener.startListening(49152, context)
            } else {
                if (!lastState) return
                else lastState = false

                (context as MainActivity).updateStatus(ConnectionType.WiFi, ManagerStatus.NotReady)
                (context.application as OpenMic).broadcastListener.stopListening()
            }
        } else {
            if (!lastState) return
            else lastState = false

            Log.d(javaClass.name, "updateState: WiFi adapter is OFF")
            (context as MainActivity).updateStatus(ConnectionType.WiFi, ManagerStatus.NotReady)
            (context.application as OpenMic).broadcastListener.stopListening()
        }
    }

    private fun getConnectivityManagerCallback(): ConnectivityManager.NetworkCallback {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityManagerCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    updateState(null)
                }

                override fun onLost(network: Network) {
                    updateState(null)
                }
            }

            return connectivityManagerCallback
        } else {
            throw IllegalAccessError("getConnectivityManagerCallback: It shouldn't be called on this device")
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun lollipopNetworkAvailableRequest() {
        val builder = NetworkRequest.Builder()
            .addTransportType(android.net.NetworkCapabilities.TRANSPORT_WIFI)

        connectivityManager.registerNetworkCallback(
            builder.build(),
            getConnectivityManagerCallback()
        )
    }

    fun registerDeviceListContext(context: DevicesListScreen) {
        devicesListScreenContext = context
    }

    fun addDevice(data: BroadcastPacket) {
        discoveredDevices.forEach {
            server ->
            run {
                if (data.DeviceInfo.DeviceID == server.DeviceInfo.DeviceID) {
                    Log.d(javaClass.name, "addDevice: Cannot add ${data.DeviceInfo.DeviceID}, because this device is already discovered!")

                    if (devicesLastHeartBeat[data.DeviceInfo.DeviceID] != null) {
                        devicesLastHeartBeat[data.DeviceInfo.DeviceID] = System.currentTimeMillis() / 1000
                    }

                    return
                }
            }
        }

        discoveredDevices.add(ADD_INDEX, data)
        devicesLastHeartBeat[data.DeviceInfo.DeviceID] = System.currentTimeMillis() / 1000

        if (this::devicesListScreenContext.isInitialized) {
            devicesListScreenContext.requireActivity().runOnUiThread {
                devicesListScreenContext.deviceListAdapter.notifyItemInserted(ADD_INDEX)
            }
        }

        fixedRateTimer(data.DeviceInfo.DeviceID + " Heartbeat", true, 0L, 1000) {
            if ((System.currentTimeMillis() / 1000) > devicesLastHeartBeat[data.DeviceInfo.DeviceID]!! + DEVICE_TIMEOUT) {
                devicesLastHeartBeat.remove(data.DeviceInfo.DeviceID)
                Log.d(javaClass.name, "deviceHeartbeat: Haven't received heartbeat from device ${data.DeviceInfo.DeviceID} from 5 seconds, removing from discovered devices list...")

                discoveredDevices.forEachIndexed {
                    index, server ->
                    run {
                        if (server.DeviceInfo.DeviceID == data.DeviceInfo.DeviceID) {
                            discoveredDevices.remove(server)

                            try {
                                devicesListScreenContext.requireActivity().runOnUiThread {
                                    devicesListScreenContext.deviceListAdapter.notifyItemRemoved(index)
                                }
                            } catch (e: IllegalStateException) {
                                Log.e(javaClass.name, e.toString())
                            }
                        }
                    }
                }

                cancel()
            }
        }
    }
}