package pl.grzybdev.openmic.client.activities.fragments.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import pl.grzybdev.openmic.client.R
import pl.grzybdev.openmic.client.dataclasses.ui.ConnectingScreenArgs
import pl.grzybdev.openmic.client.interfaces.ui.FragmentInterface
import java.util.*
import kotlin.concurrent.schedule

class ConnectingScreen : Fragment(), FragmentInterface {

    companion object {
        const val TOO_LONG_TIME: Long = 5000

        lateinit var connectingContext: ConnectingScreen
    }

    override val adViewsPortrait: List<Int> = listOf()
    override val adViewsLandscape: List<Int> = listOf()

    private var isConnected = false

    init {
        connectingContext = this
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_connecting_screen, container, false)
    }

    override fun initViews() {}

    fun connect(args: ConnectingScreenArgs) {
        Log.d(javaClass.name, "Connecting to ${args.ip[0]}:${args.port}...")

        requireActivity().runOnUiThread {
            requireView().findViewById<TextView>(R.id.connectingText).text = getString(R.string.connecting_main, args.hostname)
            requireView().findViewById<TextView>(R.id.connectingAddress).text = getString(R.string.connecting_main_address, args.ip[0], args.port.toInt(), 1, args.ip.size)
        }

        Timer("TimeoutText", false).schedule(TOO_LONG_TIME) {
            if (!isConnected) {
                requireActivity().runOnUiThread {
                    requireView().findViewById<LinearLayout>(R.id.connectingTooLong).visibility = VISIBLE
                }
            }
        }
    }
}