package pl.grzybdev.openmic.client.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import pl.grzybdev.openmic.client.OpenMic
import pl.grzybdev.openmic.client.R
import pl.grzybdev.openmic.client.activities.fragments.main.DevicesList
import pl.grzybdev.openmic.client.activities.fragments.main.MainScreen
import pl.grzybdev.openmic.client.dataclasses.packets.BroadcastPacket
import pl.grzybdev.openmic.client.enums.manager.ConnectionType
import pl.grzybdev.openmic.client.enums.manager.ManagerStatus
import pl.grzybdev.openmic.client.enums.ui.MainFragment
import pl.grzybdev.openmic.client.interfaces.manager.ManagerInterface
import pl.grzybdev.openmic.client.managers.BaseManager
import pl.grzybdev.openmic.client.managers.connectors.USBManager
import pl.grzybdev.openmic.client.managers.connectors.WifiManager


class MainActivity : AppCompatActivity(), ManagerInterface {

    private var currentFragment: MainFragment = MainFragment.None

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(javaClass.name, "onCreate: MainActivity has been created")

        setContentView(R.layout.activity_main)
        changeFragment(MainFragment.MainScreen)

        (application as OpenMic).connectionManagers = mapOf(ConnectionType.WiFi to WifiManager(this), ConnectionType.USB to USBManager(this))
    }

    override fun onResume() {
        super.onResume()

        Log.d(javaClass.name, "onResume: Starting all connection managers...")

        (application as OpenMic).connectionManagers.forEach {
                (_, manager) -> manager.startManager()
        }
    }

    override fun onPause() {
        super.onPause()

        Log.d(javaClass.name, "onPause: Stopping all connection managers...")

        (application as OpenMic).connectionManagers.forEach {
                (_, manager) -> manager.stopManager()
        }
    }

    fun changeFragment(newFragment: MainFragment) {
        if (newFragment == currentFragment) return
        else currentFragment = newFragment

        val fragment: Fragment = when (newFragment) {
            MainFragment.None -> return
            MainFragment.MainScreen -> MainScreen()
            MainFragment.DevicesList -> DevicesList()
        }

        // Create new fragment and transaction
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.addToBackStack(null)

        // Commit the transaction
        transaction.commit()
    }

    override fun updateStatus(type: ConnectionType, status: ManagerStatus) {
        val color: Int = when (status) {
            ManagerStatus.Ready -> R.color.main_status_connected
            ManagerStatus.NotReady -> R.color.main_status_not_connected
            ManagerStatus.Disabled -> R.color.main_status_disabled
        }

        val text: String = when (type) {
            ConnectionType.WiFi -> when (status) {
                ManagerStatus.Ready -> getString(R.string.main_status_wifi_enabled)
                ManagerStatus.NotReady -> getString(R.string.main_status_wifi_disabled)
                ManagerStatus.Disabled -> getString(R.string.main_status_wifi_no_location)
            }
            ConnectionType.USB -> when (status) {
                ManagerStatus.Ready -> getString(R.string.main_status_usb_connected)
                ManagerStatus.NotReady -> getString(R.string.main_status_usb_disconnected)
                ManagerStatus.Disabled -> getString(R.string.main_status_usb_not_available)
            }
        }

        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) is MainScreen) {
            val mainScreen: MainScreen = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as MainScreen
            mainScreen.updateListenerStatus(type, color, text)
        }
    }

    override fun onDeviceDiscovered(type: ConnectionType, data: BroadcastPacket) {
        val manager: BaseManager = (application as OpenMic).connectionManagers[type]!!

        when (type) {
            ConnectionType.WiFi -> {
                changeFragment(MainFragment.DevicesList)
                (manager as WifiManager).addDevice(data)
            }
            ConnectionType.USB -> {

            }
        }
    }

}
