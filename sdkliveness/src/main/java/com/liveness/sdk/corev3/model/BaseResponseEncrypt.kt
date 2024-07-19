package com.liveness.sdk.corev3.model

import androidx.annotation.Keep
import com.nimbusds.jose.shaded.gson.annotations.SerializedName

/**
 * Created by Thuytv on 16/04/2024.
 */
@Keep
data class BaseResponseEncrypt(
    @field:SerializedName("jws")
    var jws: String? = null
)