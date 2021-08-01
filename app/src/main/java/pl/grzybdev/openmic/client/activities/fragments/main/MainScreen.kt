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
import pl.grzybdev.openmic.client.R
import pl.grzybdev.openmic.client.enums.manager.ConnectionType
import pl.grzybdev.openmic.client.interfaces.ui.FragmentInterface


class MainScreen : Fragment(), FragmentInterface {

    override val adViewsPortrait: List<Int>
        get() = listOf(R.id.adView_Main_Top, R.id.adView_Main_Bottom)
    override val adViewsLandscape: List<Int>
        get() = listOf(R.id.adView_Main_Big)

    private lateinit var wifiStatus: TextView
    private lateinit var usbStatus: TextView

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
        initAds(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        Log.d(javaClass.name, "onConfigurationChanged: Reloading ads...")
        initAds(this)
    }

    override fun initViews() {
        Log.d(javaClass.name, "initViews: Initializing views...")

        wifiStatus = requireView().findViewById(R.id.statusWifi)
        usbStatus = requireView().findViewById(R.id.statusUSB)
    }

    fun updateListenerStatus(connectionType: ConnectionType, textColor: Int, text: String) {
        val statusTextView: TextView = when (connectionType) {
            ConnectionType.WiFi -> requireView().findViewById(R.id.statusWifi)
            ConnectionType.USB -> requireView().findViewById(R.id.statusUSB)
        }

        requireActivity().runOnUiThread {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                statusTextView.setTextColor(requireContext().getColor(textColor))
            } else {
                statusTextView.setTextColor(ContextCompat.getColor(requireContext(), textColor))
            }

            statusTextView.text = text
        }
    }

}