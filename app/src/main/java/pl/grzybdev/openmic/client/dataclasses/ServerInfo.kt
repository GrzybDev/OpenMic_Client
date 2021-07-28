package pl.grzybdev.openmic.client.dataclasses

import kotlinx.serialization.Serializable

@Serializable
data class ServerInfo(val App: String, val Build: String, val Device: String)
