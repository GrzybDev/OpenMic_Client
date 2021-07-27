package pl.grzybdev.openmic.client.activities.fragments.main

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import pl.grzybdev.openmic.client.R
import pl.grzybdev.openmic.client.enums.ConnectionType


class MainScreen : Fragment() {

    private val adViewsPortrait: MutableList<Int> = mutableListOf(
        R.id.adView_Main_Top,
        R.id.adView_Main_Bottom
    )

    private val adViewsLand: MutableList<Int> = mutableListOf(R.id.adView_Main_Big)

    private lateinit var wifiStatus: TextView
    private lateinit var usbStatus: TextView

    private var wifiLastStatus: Boolean = false
    private var usbLastStatus: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(javaClass.name, "onCreateView: MainScreen view has been created")

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_screen, container, false)
    }

    override fun onStart() {
        super.onStart()

        Log.d(javaClass.name, "onStart: MainScreen has been started")

        initViews()
        initAds()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        Log.d(javaClass.name, "onConfigurationChanged")
        initAds()
    }

    private fun initViews() {
        Log.d(javaClass.name, "initViews: Initializing views...")

        wifiStatus = requireView().findViewById(R.id.statusWifi)
        usbStatus = requireView().findViewById(R.id.statusUSB)
    }

    private fun initAds() {
        Log.d(javaClass.name, "initAds: Reloading ads in MainScreen...")

        val orientation = this.resources.configuration.orientation
        val adViews: List<Int> = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            adViewsPortrait
        } else {
            adViewsLand
        }

        adViews.forEach { adViewID ->

            run {
                val adRequest = AdRequest.Builder().build()
                val adView = requireView().findViewById<AdView>(adViewID)

                adView.loadAd(adRequest)
            }
        }
    }

    fun updateListenerStatus(connectionType: ConnectionType, isConnected: Boolean) {
        val statusTextView: TextView = when (connectionType) {
            ConnectionType.WiFi -> wifiStatus
            ConnectionType.USB -> usbStatus
        }

        val changeText: String = when (connectionType) {
            ConnectionType.WiFi -> "Wi-Fi"
            ConnectionType.USB -> "USB"
        }

        Log.d(javaClass.name, "updateListenerStatus: Changing status of $changeText to ${if (isConnected) "enabled" else "disabled"}")

        val textColor: Int
        val text: Int

        if (statusTextView == wifiStatus) {
            if (wifiLastStatus == isConnected) return
            else wifiLastStatus = isConnected

            if (isConnected) {
                textColor = R.color.main_status_connected
                text = R.string.main_status_wifi_enabled
            } else {
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

        requireActivity().runOnUiThread {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                statusTextView.setTextColor(requireContext().getColor(textColor))
            } else {
                statusTextView.setTextColor(ContextCompat.getColor(requireContext(), textColor))
            }

            statusTextView.text = getString(text)
        }
    }

    fun setWifiDisabled() {
        requireActivity().runOnUiThread {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                wifiStatus.setTextColor(requireContext().getColor(R.color.main_status_disabled))
            } else {
                wifiStatus.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.main_status_disabled
                    )
                )
            }

            wifiStatus.text = getString(R.string.main_status_wifi_no_location)
        }
    }

    fun setWifiRestarting() {
        requireActivity().runOnUiThread {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                wifiStatus.setTextColor(requireContext().getColor(R.color.main_status_disabled))
            } else {
                wifiStatus.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.main_status_disabled
                    )
                )
            }

            wifiStatus.text = getString(R.string.main_status_restarting)
        }
    }
}