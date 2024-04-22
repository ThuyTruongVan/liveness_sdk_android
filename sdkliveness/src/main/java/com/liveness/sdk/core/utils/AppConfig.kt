package com.liveness.sdk.core.utils

import com.liveness.sdk.core.model.LivenessRequest

/**
 * Created by Thuytv on 16/04/2024.
 */
internal object AppConfig {
    var livenessListener: CallbackLivenessListener? = null
    var mLivenessRequest: LivenessRequest? = null
    var KEY_BUNDLE_SCREEN = "KEY_BUNDLE_SCREEN"
    var TYPE_SCREEN_REGISTER_FACE = "TYPE_SCREEN_REGISTER_FACE"
}