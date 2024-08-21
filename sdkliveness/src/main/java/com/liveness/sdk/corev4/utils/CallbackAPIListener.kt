package com.liveness.sdk.corev4.utils

import androidx.annotation.Keep

/**
 * Created by Thuytv on 16/04/2024.
 */
@Keep
interface CallbackAPIListener {
    fun onCallbackResponse(data: String?)
}