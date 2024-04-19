package com.liveness.sdk.core.model

import com.nimbusds.jose.shaded.gson.annotations.SerializedName

/**
 * Created by Thuytv on 16/04/2024.
 */
data class LivenessRequest(
    @field:SerializedName("duration")
    var duration: Int? = null,
    @field:SerializedName("device_id")
    var deviceId: String? = null,
    @field:SerializedName("private_key")
    var privateKey: String? = null,
    @field:SerializedName("app_id")
    var appId: String? = null,
    @field:SerializedName("client_transaction_id")
    var clientTransactionId: String? = null,
    @field:SerializedName("secret")
    var secret: String? = null,
    @field:SerializedName("base_url")
    var baseURL: String? = null
)