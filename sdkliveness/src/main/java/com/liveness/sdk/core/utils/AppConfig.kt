package com.liveness.sdk.core.utils

import android.annotation.SuppressLint
import android.view.View
import android.widget.Button
import androidx.lifecycle.MutableLiveData
import com.liveness.sdk.core.model.LivenessRequest

/**
 * Created by Thuytv on 16/04/2024.
 */
@SuppressLint("StaticFieldLeak")
internal object AppConfig {
    var livenessListener: CallbackLivenessListener? = null
    var livenessFaceListener: CallbackLivenessListener? = null
    var mLivenessRequest: LivenessRequest? = null

    var mCustomView: View? = null
    var mActionView: View? = null
    var mProgressView: View? = null

    var KEY_BUNDLE_SCREEN = "KEY_BUNDLE_SCREEN"
    var TYPE_SCREEN_REGISTER_FACE = "TYPE_SCREEN_REGISTER_FACE"
    var INTENT_VALUE_BACK = "INTENT_VALUE_BACK"
}