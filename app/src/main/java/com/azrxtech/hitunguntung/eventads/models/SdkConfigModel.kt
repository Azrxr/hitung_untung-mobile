package com.azrxtech.hitunguntung.eventads.models

data class GoogleSdkConfig(
    val isActive: Boolean = false,
    val isDebugViewEnabled: Boolean = false,
    val gtmIsActive: Boolean = false
)

data class MetaSdkConfig(
    val isActive: Boolean = false,
    val metaAppId: String = "",
    val metaClientToken: String = ""
)

data class TiktokSdkConfig(
    val isActive: Boolean = false,
    val androidId: String = "",
    val androidTiktokId: String = "",
    val appleId: String = "",
    val appleTiktokId: String = "",
    val stage: String = "sandbox"
)

data class EventSdkConfig(
    val google: GoogleSdkConfig = GoogleSdkConfig(),
    val meta: MetaSdkConfig = MetaSdkConfig(),
    val tiktok: TiktokSdkConfig = TiktokSdkConfig()
)
