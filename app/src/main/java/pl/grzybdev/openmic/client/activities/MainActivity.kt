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

    private var lastWifiStatus: Boolean = false

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
        val wifiMgr = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        fixedRateTimer("WifiCheck", true, 0L, 1000) {
            if (wifiMgr.isWifiEnabled) { // Wi-Fi adapter is ON
                val wifiInfo = wifiMgr.connectionInfo
                updateWifiStatus(wifiInfo.networkId != -1)
            } else {
                updateWifiStatus(false) // Wi-Fi adapter is OFF
            }
        }
    }

    fun updateWifiStatus(isConnected: Boolean) {
        if (lastWifiStatus == isConnected) return
        lastWifiStatus = isConnected

        if (isConnected)
            broadcastListener.startListening(49152, this)
        else
            broadcastListener.stopListening()

        val textColor: Int
        val text: Int

        if (isConnected) {
            textColor = R.color.main_status_connected
            text = R.string.main_status_wifi_enabled
        } else {
            textColor = R.color.main_status_not_connected
            text = R.string.main_status_wifi_disabled
        }

        runOnUiThread {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                wifiStatus.setTextColor(getColor(textColor))
            } else {
                wifiStatus.setTextColor(ContextCompat.getColor(this, textColor))
            }

            wifiStatus.text = getString(text)
        }
    }
}
