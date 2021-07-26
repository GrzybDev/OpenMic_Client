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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import pl.grzybdev.openmic.client.R
import pl.grzybdev.openmic.client.network.BroadcastListener


class MainActivity : AppCompatActivity() {

    companion object {
        const val WIFI_STATE_PERMISSION = "android.permission.ACCESS_FINE_LOCATION"
        const val WIFI_CHECK_INTENT = "android.net.conn.CONNECTIVITY_CHANGE"
    }


    private val adViewsMutable: MutableList<Int> = mutableListOf(
        R.id.adView_Top,
        R.id.adView_Bottom
    )

    private val adViews: List<Int> = adViewsMutable

    private var wifiLastStatus: Boolean = false
    private var usbLastStatus: Boolean = false

    private lateinit var wifiStatus: TextView
    private lateinit var usbStatus: TextView

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    private val broadcastListener: BroadcastListener = BroadcastListener()

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var connectivityManagerCallback: ConnectivityManager.NetworkCallback
    private lateinit var wifiManager: WifiManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(javaClass.name, "onCreate: MainActivity has been created")

        setContentView(R.layout.activity_main)

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        initViews()
        initAds()
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

        updateListenerStatus(wifiStatus, false)
        updateListenerStatus(usbStatus, false)
    }

    private fun initViews() {
        wifiStatus = findViewById(R.id.statusWifi)
        usbStatus = findViewById(R.id.statusUSB)
    }

    private fun initAds() {
        Log.d(javaClass.name, "initAds: Initializing ads...")

        MobileAds.initialize(this) {}

        adViews.forEach { adViewID ->

            run {
                val adRequest = AdRequest.Builder().build()
                val adView = findViewById<AdView>(adViewID)

                adView.loadAd(adRequest)
            }
        }
    }

    private fun initWifiListener() {
        Log.d(javaClass.name, "initWifiListener: Initializing Wi-Fi Listener...")

        if (ContextCompat.checkSelfPermission(
                this,
                WIFI_STATE_PERMISSION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            Log.d(javaClass.name, "initWifiListener: Disabling Wi-Fi, because we don't have access to device location...")

            runOnUiThread {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    wifiStatus.setTextColor(getColor(R.color.main_status_disabled))
                } else {
                    wifiStatus.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.main_status_disabled
                        )
                    )
                }

                wifiStatus.text = getString(R.string.main_status_wifi_no_location)
            }

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

    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateWifiState()
        }
    }

    private fun updateWifiState() {
        Log.d(javaClass.name, "updateWifiState: Updating Wi-Fi state...")

        if (wifiManager.isWifiEnabled) {
            Log.d(javaClass.name, "updateWifiState: Wi-Fi adapter is ON")

            val wifiInfo = wifiManager.connectionInfo
            updateListenerStatus(
                wifiStatus,
                wifiInfo.networkId != -1
            ) // networkId returns -1 when not connected to any Wi-Fi network
        } else {
            Log.d(javaClass.name, "updateWifiState: Wi-Fi adapter is OFF")
            updateListenerStatus(wifiStatus, false) // Wi-Fi adapter is OFF
        }
    }

    private fun updateListenerStatus(statusTextView: TextView, isConnected: Boolean) {
        val changeText: String = if (statusTextView == wifiStatus) "Wi-Fi" else "USB"

        Log.d(javaClass.name, "updateListenerStatus: Changing status of $changeText to ${if (isConnected) "enabled" else "disabled"}")

        val textColor: Int
        val text: Int

        if (statusTextView == wifiStatus) {
            if (wifiLastStatus == isConnected) return
            else wifiLastStatus = isConnected

            if (isConnected) {
                broadcastListener.startListening(49152, this)
                textColor = R.color.main_status_connected
                text = R.string.main_status_wifi_enabled
            } else {
                broadcastListener.stopListening()
                textColor = R.color.main_status_not_connected
                text = R.string.main_status_wifi_disabled
            }
        } else {
            if (usbLastStatus == isConnected) return
            else usbLastStatus = isConnected

            if (isConnected) {
                textColor = R.color.main_status_disabled
                text = R.string.main_status_usb_connected
            } else {
                textColor = R.color.main_status_not_connected
                text = R.string.main_status_usb_disconnected
            }
        }

        runOnUiThread {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                statusTextView.setTextColor(getColor(textColor))
            } else {
                statusTextView.setTextColor(ContextCompat.getColor(this, textColor))
            }

            statusTextView.text = getString(text)
        }
    }

    private fun initUSBListener() {
        Log.d(javaClass.name, "initUSBListener: Registering batteryStatusReceiver...")
        registerReceiver(batteryStatusReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        Log.d(javaClass.name, "initUSBListener: Successfully registered batteryStatusReceiver")
    }

    private val batteryStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                updateUSBStatus(intent)
            }
        }
    }

    private fun updateUSBStatus(batteryStatus: Intent) {
        Log.d(javaClass.name, "updateUSBStatus: Updating USB connection status...")

        val status: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL

        // How are we charging?
        val chargePlug: Int = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val usbCharge: Boolean = chargePlug == BatteryManager.BATTERY_PLUGGED_USB

        updateListenerStatus(usbStatus, isCharging && usbCharge)
    }
}
