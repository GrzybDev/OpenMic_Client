package pl.grzybdev.openmic.client

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import pl.grzybdev.openmic.client.network.BroadcastListener

class OpenMic : Application() {

    companion object {
        lateinit var mFirebaseAnalytics: FirebaseAnalytics
        lateinit var app: OpenMic
    }

    val broadcastListener: BroadcastListener = BroadcastListener()

    override fun onCreate() {
        super.onCreate()

        app = this

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        MobileAds.initialize(this)
    }
}