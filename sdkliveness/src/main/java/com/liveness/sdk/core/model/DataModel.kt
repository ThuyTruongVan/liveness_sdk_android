package com.liveness.sdk.core.model

import com.nimbusds.jose.shaded.gson.annotations.SerializedName

/**
 * Created by Thuytv on 16/04/2024.
 */
data class DataModel(
    @field:SerializedName("sim")
    var sim: String? = null,
    @field:SerializedName("faceMatchingScore")
    var faceMatchingScore: String? = null,
    @field:SerializedName("fakeScore")
    var fakeScore: String? = null,
    @field:SerializedName("livenessThermalScore")
    var livenessThermalScore: String? = null,
    @field:SerializedName("livenessFlashScore")
    var livenessFlashScore: String? = null
)