package pl.grzybdev.openmic.client.dataclasses

import kotlinx.serialization.Serializable

@Serializable
data class DeviceInfo(val DeviceID: String, val KernelType: String, val OperatingSystem: String)
