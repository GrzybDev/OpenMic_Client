package pl.grzybdev.openmic.client

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import pl.grzybdev.openmic.client.enums.manager.ConnectionType
import pl.grzybdev.openmic.client.managers.BaseManager
import pl.grzybdev.openmic.client.network.BroadcastListener

class OpenMic : Application() {

    val broadcastListener: BroadcastListener = BroadcastListener()

    lateinit var connectionManagers: Map<ConnectionType, BaseManager>
    lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate() {
        super.onCreate()

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        MobileAds.initialize(this)
    }
}