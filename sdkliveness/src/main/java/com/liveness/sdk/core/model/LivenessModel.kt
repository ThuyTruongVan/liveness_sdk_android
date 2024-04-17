package com.liveness.sdk.core.model

import com.nimbusds.jose.shaded.gson.annotations.SerializedName

/**
 * Created by Thuytv on 16/04/2024.
 */
data class LivenessModel(
    @field:SerializedName("status")
    var status: Int? = null,
    @field:SerializedName("message")
    var message: String? = null,
    @field:SerializedName("data")
    var data: DataModel? = null,
    @field:SerializedName("code")
    var code: String? = null,
    @field:SerializedName("signature")
    var signature: String? = null,
    @field:SerializedName("success")
    var success: Boolean? = null,
    @field:SerializedName("path_video")
    var pathVideo: String? = null
)