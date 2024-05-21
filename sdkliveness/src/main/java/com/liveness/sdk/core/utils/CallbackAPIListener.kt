package com.liveness.sdk.core.utils

import androidx.annotation.Keep
import com.google.android.gms.common.annotation.KeepName
import com.liveness.sdk.core.model.LivenessModel

/**
 * Created by Thuytv on 16/04/2024.
 */
@Keep
interface CallbackAPIListener {
    fun onCallbackResponse(data: String?)
}