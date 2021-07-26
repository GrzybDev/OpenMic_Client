package pl.grzybdev.openmic.client.activities

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
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentContainerView
import com.google.firebase.analytics.FirebaseAnalytics
import pl.grzybdev.openmic.client.R
import pl.grzybdev.openmic.client.activities.fragments.main.MainScreen
import pl.grzybdev.openmic.client.enums.ConnectionType
import pl.grzybdev.openmic.client.network.BroadcastListener


class MainActivity : AppCompatActivity() {

    companion object {
        const val WIFI_STATE_PERMISSION = "android.permission.ACCESS_FINE_LOCATION"
        const val WIFI_CHECK_INTENT = "android.net.conn.CONNECTIVITY_CHANGE"
    }

    private val broadcastListener: BroadcastListener = BroadcastListener()

    private lateinit var fragmentContainer: FragmentContainerView

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var connectivityManagerCallback: ConnectivityManager.NetworkCallback
    private lateinit var wifiManager: WifiManager

    private var wifiLastStatus: Boolean = false
    private var usbLastStatus: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(javaClass.name, "onCreate: MainActivity has been created")

        setContentView(R.layout.activity_main)

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        fragmentContainer = findViewById(R.id.fragmentContainer)
    }

    override fun onResume() {
        super.onResume()

        Log.d(javaClass.name, "onResume: App is in foreground again")

        initWifiListener()
        initUSBListener()
    }

    override fun onPause() {
        super.onPause()

        Log.d(javaClass.name, "onPause: App is not in foreground anymore")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && this::connectivityManager.isInitialized) {
            Log.d(javaClass.name, "Unregistering connectivityManagerCallback...")
            connectivityManager.unregisterNetworkCallback(connectivityManagerCallback)
            Log.d(javaClass.name, "Successfully unregistered connectivityManagerCallback!")
        }

        try {
            Log.d(javaClass.name, "Unregistering networkReceiver...")
            unregisterReceiver(networkReceiver)
            Log.d(javaClass.name, "Successfully unregistered networkReceiver!")
        } catch (e: IllegalArgumentException) {
            Log.w(javaClass.name, "Cannot unregister networkReceiver, because it's not registered!")
        }

        try {
            Log.d(javaClass.name, "Unregistering batteryStatusReceiver...")
            unregisterReceiver(batteryStatusReceiver)
            Log.d(javaClass.name, "Successfully unregistered batteryStatusReceiver!")
        } catch (e: IllegalArgumentException) {
            Log.w(javaClass.name, "Cannot unregister batteryStatusReceiver, because it's not registered!")
        }

        Log.d(javaClass.name, "Updating listener statuses...")

        updateStatus(ConnectionType.WiFi, false)
        updateStatus(ConnectionType.USB, false)
    }

    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateWifiState()
        }
    }

    private val batteryStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                updateUSBStatus(intent)
            }
        }
    }

    private fun initWifiListener() {
        Log.d(javaClass.name, "initWifiListener: Initializing WiFi Listener...")

        if (ContextCompat.checkSelfPermission(
                this,
                WIFI_STATE_PERMISSION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            Log.d(javaClass.name, "initWifiListener: Disabling WiFi, because we don't have access to device location...")

            val mainScreen: MainScreen = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as MainScreen
            mainScreen.setWifiDisabled()

            return
        }

        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        updateWifiState()
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> connectivityManager.registerDefaultNetworkCallback(
                getConnectivityManagerCallback()
            )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> lollipopNetworkAvailableRequest()
            else -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    registerReceiver(
                        networkReceiver,
                        IntentFilter(WIFI_CHECK_INTENT)
                    )
                }
            }
        }

        Log.d(javaClass.name, "initWifiListener: Finished initializing Wi-Fi listener!")
    }

    private fun updateWifiState() {
        Log.d(javaClass.name, "updateWifiState: Updating Wi-Fi state...")

        if (wifiManager.isWifiEnabled) {
            Log.d(javaClass.name, "updateWifiState: WiFi adapter is ON")

            val wifiInfo = wifiManager.connectionInfo
            updateStatus(
                ConnectionType.WiFi,
                wifiInfo.networkId != -1
            ) // networkId returns -1 when not connected to any Wi-Fi network
        } else {
            Log.d(javaClass.name, "updateWifiState: WiFi adapter is OFF")
            updateStatus(ConnectionType.WiFi, false) // Wi-Fi adapter is OFF
        }
    }

    private fun getConnectivityManagerCallback(): ConnectivityManager.NetworkCallback {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityManagerCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    updateWifiState()
                }

                override fun onLost(network: Network) {
                    updateWifiState()
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

    private fun initUSBListener() {
        Log.d(javaClass.name, "initUSBListener: Registering batteryStatusReceiver...")
        registerReceiver(batteryStatusReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        Log.d(javaClass.name, "initUSBListener: Successfully registered batteryStatusReceiver")
    }

    private fun updateUSBStatus(batteryStatus: Intent) {
        Log.d(javaClass.name, "updateUSBStatus: USB status has been updated")

        val status: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL

        // How are we charging?
        val chargePlug: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val usbCharge: Boolean = chargePlug == BatteryManager.BATTERY_PLUGGED_USB

        updateStatus(ConnectionType.USB, isCharging && usbCharge)
    }

    private fun updateStatus(type: ConnectionType, isConnected: Boolean) {
        when (type) {
            ConnectionType.WiFi -> { if (wifiLastStatus == isConnected) return else wifiLastStatus = isConnected }
            ConnectionType.USB -> { if (usbLastStatus == isConnected) return else usbLastStatus = isConnected }
        }

        Log.d(javaClass.name, "updateStatus: $type is now ${if (isConnected) "enabled" else "disabled"}")

        val mainScreen: MainScreen = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as MainScreen
        mainScreen.updateListenerStatus(type, isConnected)

        if (type == ConnectionType.WiFi) {
            if (isConnected) {
                broadcastListener.startListening(49153, this)
            } else {
                broadcastListener.stopListening()
            }
        }
    }

}
