package com.liveness.sdk.corev4.model

import androidx.annotation.Keep
import com.nimbusds.jose.shaded.gson.annotations.SerializedName

/**
 * Created by Thuytv on 16/04/2024.
 */
@Keep
data class LivenessModel(
    @field:SerializedName("status")
    var status: Int? = null,
    @field:SerializedName("message")
    var message: String? = null,
    @field:SerializedName("request_id")
    var requestId: String? = null,
    @field:SerializedName("data")
    var data: DataModel? = null,
    @field:SerializedName("code")
    var code: String? = null,
    @field:SerializedName("signature")
    var signature: String? = null,
    @field:SerializedName("success")
    var success: Boolean? = null,
    @field:SerializedName("path_video")
    var pathVideo: String? = null,
    //register field
    @field:SerializedName("face_image")
    var faceImage: String? = null,
    @field:SerializedName("face_register_id")
    var faceRegisterId: String? = null,
    //register field
    @field:SerializedName("liveness_image")
    var livenessImage: String? = null,
    @field:SerializedName("transaction_id")
    var transactionID: String? = null,
    @field:SerializedName("img_transparent")
    var imgTransparent: String? = null,
    @field:SerializedName("img_red")
    var imgRed: String? = null,
    @field:SerializedName("img_green")
    var imgGreen: String? = null,
    @field:SerializedName("img_blue")
    var imgBlue: String? = null
)