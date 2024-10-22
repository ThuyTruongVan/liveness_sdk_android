package com.liveness.sdk.corev4.model

import com.nimbusds.jose.shaded.gson.annotations.SerializedName

data class DataConfig(
    @field:SerializedName("randomColor")
    var randomColor: Int?,
    @field:SerializedName("randomColor")
    var randomFrame: Int?
)
