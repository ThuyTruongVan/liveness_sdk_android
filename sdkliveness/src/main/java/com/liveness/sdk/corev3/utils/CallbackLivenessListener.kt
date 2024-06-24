package com.liveness.sdk.corev3.utils

import androidx.annotation.Keep
import com.liveness.sdk.corev3.model.LivenessModel

/**
 * Created by Thuytv on 16/04/2024.
 */
@Keep
interface CallbackLivenessListener {
    fun onCallbackLiveness(data: LivenessModel?)
}