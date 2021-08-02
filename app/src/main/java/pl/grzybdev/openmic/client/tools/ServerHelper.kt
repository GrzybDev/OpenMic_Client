package pl.grzybdev.openmic.client.tools

import pl.grzybdev.openmic.client.R
import android.os.Build

import android.text.TextUtils
import pl.grzybdev.openmic.client.BuildConfig
import pl.grzybdev.openmic.client.enums.VersionStatus


class ServerHelper {
    companion object {
        private const val SERVER_APP_ID = "pl.grzybdev.openmic.server"

        fun getSystemIcon(os: String): Int {
            return when (os) {
                "winnt" -> R.drawable.ic_os_windows
                "darwin" -> R.drawable.ic_os_mac
                "linux" -> R.drawable.ic_os_linux
                else -> R.drawable.ic_baseline_help_24
            }
        }

        fun getVersionStatus(version: String): VersionStatus {
            val appVerParts = BuildConfig.VERSION_NAME.split(".")
            val srvVerParts = version.split(".")

            val appMajor = if (appVerParts.isNotEmpty()) appVerParts[0].toInt() else 0
            val srvMajor = if (srvVerParts.isNotEmpty()) srvVerParts[0].toInt() else 0

            if (srvMajor > appMajor) return VersionStatus.FUTURE
            else if (appMajor > srvMajor) return VersionStatus.OUTDATED

            val appMinor = if (appVerParts.size > 1) appVerParts[1].toInt() else 0
            val srvMinor = if (srvVerParts.size > 1) srvVerParts[1].toInt() else 0

            if (srvMinor != appMinor) return VersionStatus.UPDATE_AVAILABLE

            return VersionStatus.OK
        }

        fun isOfficialServer(appId: String) : Boolean {
            return appId == SERVER_APP_ID
        }
    }
}