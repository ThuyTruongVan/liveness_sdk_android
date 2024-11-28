package com.liveness.sdk.corev4.model

import androidx.annotation.Keep
import com.nimbusds.jose.shaded.gson.annotations.SerializedName
@Keep
data class DataConfig(
    @field:SerializedName("randomColor")
    var randomColor: Int?,
    @field:SerializedName("randomColor")
    var randomFrame: Int?,
    @field:SerializedName("maxWidth")
    var maxWidth: Int?=null,
    @field:SerializedName("quality")
    var quality: Int?=null
)
