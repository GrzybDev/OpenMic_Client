package pl.grzybdev.openmic.client.activities.fragments.main

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pl.grzybdev.openmic.client.OpenMic
import pl.grzybdev.openmic.client.R
import pl.grzybdev.openmic.client.adapters.DeviceListAdapter
import pl.grzybdev.openmic.client.enums.manager.ConnectionType
import pl.grzybdev.openmic.client.interfaces.ui.FragmentInterface
import pl.grzybdev.openmic.client.managers.BaseManager

class DevicesList : Fragment(), FragmentInterface {

    override val adViewsPortrait: List<Int>
        get() = listOf(R.id.adView_deviceList)
    override val adViewsLandscape: List<Int>
        get() = listOf(R.id.adView_devicesList_land_top)

    private lateinit var devicesList: RecyclerView
    private lateinit var deviceListAdapter: DeviceListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_devices_list, container, false)
    }

    override fun onStart() {
        super.onStart()

        Log.d(javaClass.name, "onStart: DeviceList has been started")

        initViews()
        initAds(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        Log.d(javaClass.name, "onConfigurationChanged: Reloading ads...")
        initAds(this)
    }

    override fun initViews() {
        devicesList = requireView().findViewById(R.id.devicesList)

        val manager: BaseManager = (requireActivity().application as OpenMic).connectionManagers[ConnectionType.WiFi]!!
        deviceListAdapter = DeviceListAdapter(requireContext(), manager.discoveredDevices)

        devicesList.adapter = deviceListAdapter
        devicesList.layoutManager = LinearLayoutManager(requireContext())
    }

}