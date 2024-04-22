package com.liveness.sdk.core.utils

import com.liveness.sdk.core.model.LivenessModel

/**
 * Created by Thuytv on 16/04/2024.
 */
interface CallbackLivenessListener {
    fun onCallbackLiveness(data: LivenessModel?)
}