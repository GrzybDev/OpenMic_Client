package pl.grzybdev.openmic.client.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import pl.grzybdev.openmic.client.R
import pl.grzybdev.openmic.client.network.BroadcastListener
import kotlin.concurrent.fixedRateTimer


class MainActivity : AppCompatActivity() {

    private val adViewsMutable: MutableList<Int> = mutableListOf(
        R.id.adView_Top,
        R.id.adView_Bottom
    )

    private val adViews: List<Int> = adViewsMutable

    private lateinit var wifiStatus: TextView

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    private val broadcastListener: BroadcastListener = BroadcastListener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        initViews()
        initAds()
        initListeners()
    }

    private fun initViews() {
        wifiStatus = findViewById(R.id.statusWifi)
    }

    private fun initAds() {
        MobileAds.initialize(this) {}

        adViews.forEach {
            adViewID ->

            run {
                val adRequest = AdRequest.Builder().build()
                val adView = findViewById<AdView>(adViewID)

                adView.loadAd(adRequest)
            }
        }
    }

    private fun initListeners() {
        initWifiListener()
    }

    private fun initWifiListener() {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val callback: NetworkCallback = object:NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    updateWifiStatus(true)
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    updateWifiStatus(false)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                cm.registerDefaultNetworkCallback(callback)
            } else {
                val request = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
                cm.registerNetworkCallback(request, callback)
            }
        } else {
            val wifiMgr = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

            fixedRateTimer("WifiCheck", true, 0L, 1000) {
                if (wifiMgr.isWifiEnabled) { // Wi-Fi adapter is ON
                    val wifiInfo = wifiMgr.connectionInfo
                    updateWifiStatus(wifiInfo.networkId == -1)
                } else {
                    updateWifiStatus(false) // Wi-Fi adapter is OFF
                }
            }
        }
    }

    fun updateWifiStatus(isConnected: Boolean) {
        if (isConnected) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                wifiStatus.setTextColor(getColor(R.color.main_status_connected))
            } else {
                wifiStatus.setTextColor(ContextCompat.getColor(this, R.color.main_status_connected))
            }

            wifiStatus.text = getString(R.string.main_status_wifi_enabled)
            broadcastListener.startListening(49152, this)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                wifiStatus.setTextColor(getColor(R.color.main_status_not_connected))
            } else {
                wifiStatus.setTextColor(ContextCompat.getColor(this,
                    R.color.main_status_not_connected
                ))
            }

            wifiStatus.text = getString(R.string.main_status_wifi_disabled)
            broadcastListener.stopListening()
        }
    }
}
