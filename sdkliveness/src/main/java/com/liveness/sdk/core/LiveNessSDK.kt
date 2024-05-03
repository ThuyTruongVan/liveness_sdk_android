package com.liveness.sdk.core

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import com.google.android.gms.common.annotation.KeepName
import com.liveness.sdk.core.api.HttpClientUtils
import com.liveness.sdk.core.model.LivenessRequest
import com.liveness.sdk.core.utils.AppConfig
import com.liveness.sdk.core.utils.AppPreferenceUtils
import com.liveness.sdk.core.utils.CallbackLivenessListener
import com.liveness.sdk.core.utils.DeCryptData
import com.liveness.sdk.core.utils.KeyStoreUtils

/**
 * Created by Thuytv on 15/04/2024.
 */
@Keep
class LiveNessSDK {
    companion object {
        @Keep
        fun startLiveNess(context: Context, mLivenessRequest: LivenessRequest, livenessListener: CallbackLivenessListener) {
            AppConfig.livenessListener = livenessListener
            AppConfig.mLivenessRequest = mLivenessRequest
            val httpClientUtil = HttpClientUtils.instance
            httpClientUtil?.setVariables(context, mLivenessRequest)
            val intent = Intent(context, MainLiveNessActivity::class.java)
            ContextCompat.startActivity(context, intent, null)
        }

        @Keep
        fun setCustomView(mCustomView: View, mActionView: View?) {
            AppConfig.mCustomView = mCustomView
            AppConfig.mActionView = mActionView
        }

        @Keep
        fun registerFace(context: Context, mLivenessRequest: LivenessRequest, livenessListener: CallbackLivenessListener) {
            AppConfig.livenessListener = livenessListener
            AppConfig.mLivenessRequest = mLivenessRequest
            val httpClientUtil = HttpClientUtils.instance
            httpClientUtil?.setVariables(context, mLivenessRequest)
            val intent = Intent(context, MainLiveNessActivity::class.java)
            intent.putExtra(AppConfig.KEY_BUNDLE_SCREEN, AppConfig.TYPE_SCREEN_REGISTER_FACE)
            ContextCompat.startActivity(context, intent, null)
        }

        @Keep
        fun getDeviceId(context: Context): String? {
            return AppPreferenceUtils(context).getDeviceId()
        }

        @Keep
        fun isRegisterFace(context: Context): Boolean {
            return AppPreferenceUtils(context).isRegisterFace()
        }

//        fun getDeviceId2(context: Context): String? {
//            return AppPreferenceUtils(context).getDeviceId2()
//        }
//
//        fun getTOTPSecret2(context: Context): String? {
//            return KeyStoreUtils.getInstance(context)?.decryptData(AppPreferenceUtils(context).getTOTPSecret())
//        }

        @Keep
        fun checkVersion(): String {
            return "v1.1.7"
        }
    }
}