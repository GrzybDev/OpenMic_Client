package pl.grzybdev.openmic.client.interfaces.ui

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

interface FragmentInterface {
    val adViewsPortrait: List<Int>
    val adViewsLandscape: List<Int>

    fun initViews()

    fun initAds(context: Fragment) {
        Log.d(javaClass.name, "initAds: Reloading ads...")

        val orientation = context.resources.configuration.orientation
        val adViews: List<Int> = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            adViewsPortrait
        } else {
            adViewsLandscape
        }

        adViews.forEach { adViewID ->
            run {
                val adRequest = AdRequest.Builder().build()
                val adView = context.requireView().findViewById<AdView>(adViewID)

                adView.loadAd(adRequest)
            }
        }
    }
}