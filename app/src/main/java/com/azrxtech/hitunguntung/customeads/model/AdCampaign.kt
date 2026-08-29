package com.azrxtech.hitunguntung.customeads.model

/**
 * Data class representasi campaign iklan yang terpilih.
 */
data class AdCampaign(
    val adId: String = "",
    val adType: String = "image",
    val title: String = "",
    val mediaUrl: String = "",
    val targetUrl: String = "",
    val buttonText: String = "Buka",
    val weight: Int = 0,
    val openTargetIn: String = "internal"
)