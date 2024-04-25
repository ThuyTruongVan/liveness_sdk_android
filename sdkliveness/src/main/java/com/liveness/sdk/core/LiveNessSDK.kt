package com.liveness.sdk.core

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat
import com.liveness.sdk.core.api.HttpClientUtils
import com.liveness.sdk.core.model.LivenessRequest
import com.liveness.sdk.core.utils.AppConfig
import com.liveness.sdk.core.utils.AppPreferenceUtils
import com.liveness.sdk.core.utils.CallbackLivenessListener

/**
 * Created by Thuytv on 15/04/2024.
 */
class LiveNessSDK {
    companion object {
        fun startLiveNess(context: Context, mLivenessRequest: LivenessRequest, livenessListener: CallbackLivenessListener) {
            AppConfig.livenessListener = livenessListener
            AppConfig.mLivenessRequest = mLivenessRequest
            val httpClientUtil = HttpClientUtils.instance
            httpClientUtil?.setVariables(context, mLivenessRequest)
            val intent = Intent(context, MainLiveNessActivity::class.java)
            ContextCompat.startActivity(context, intent, null)
        }
        fun setCustomView(mCustomView: View) {
            AppConfig.mCustomView = mCustomView
        }

        fun registerFace(context: Context, mLivenessRequest: LivenessRequest, livenessListener: CallbackLivenessListener) {
            AppConfig.livenessListener = livenessListener
            AppConfig.mLivenessRequest = mLivenessRequest
            val httpClientUtil = HttpClientUtils.instance
            httpClientUtil?.setVariables(context, mLivenessRequest)
            val intent = Intent(context, MainLiveNessActivity::class.java)
            intent.putExtra(AppConfig.KEY_BUNDLE_SCREEN, AppConfig.TYPE_SCREEN_REGISTER_FACE)
            ContextCompat.startActivity(context, intent, null)
        }

        fun getDeviceId(context: Context): String? {
            return AppPreferenceUtils(context).getDeviceId(context)
        }
        fun checkVersion(): String{
            return "v1.1.3"
        }
    }
}