package com.liveness.sdk.core.utils

import android.annotation.SuppressLint
import android.view.View
import com.liveness.sdk.core.model.LivenessRequest

/**
 * Created by Thuytv on 16/04/2024.
 */
internal object AppConfig {
    var livenessListener: CallbackLivenessListener? = null
    var mLivenessRequest: LivenessRequest? = null
    @SuppressLint("StaticFieldLeak")
    var mCustomView: View? = null

    var KEY_BUNDLE_SCREEN = "KEY_BUNDLE_SCREEN"
    var TYPE_SCREEN_REGISTER_FACE = "TYPE_SCREEN_REGISTER_FACE"
}