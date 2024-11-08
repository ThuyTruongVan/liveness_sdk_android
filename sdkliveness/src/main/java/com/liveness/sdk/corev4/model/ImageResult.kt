package com.liveness.sdk.corev4.model

import androidx.annotation.Keep
import com.nimbusds.jose.shaded.gson.annotations.SerializedName

@Keep
data class ImageResult(
    @field:SerializedName("color")
    var color: Long,
    @field:SerializedName("image")
    var image: String,
    @field:SerializedName("color_string")
    var colorString: String,
    @field:SerializedName("image_path")
    var imagePath: String? = null,
)