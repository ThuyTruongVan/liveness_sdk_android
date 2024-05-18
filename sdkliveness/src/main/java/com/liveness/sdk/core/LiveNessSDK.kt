package com.liveness.sdk.core

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.liveness.sdk.core.api.HttpClientUtils
import com.liveness.sdk.core.model.LivenessRequest
import com.liveness.sdk.core.utils.AppConfig
import com.liveness.sdk.core.utils.AppPreferenceUtils
import com.liveness.sdk.core.utils.CallbackLivenessListener

/**
 * Created by Thuytv on 15/04/2024.
 */
@Keep
class LiveNessSDK {
    companion object {
        @Keep
        fun startLiveNess(
            context: Context,
            mLivenessRequest: LivenessRequest,
            supportFragmentManager: FragmentManager,
            frameView: Int,
            livenessListener: CallbackLivenessListener?
        ) {
            livenessListener?.let {
                AppConfig.livenessListener = it
            }
            AppConfig.mLivenessRequest = mLivenessRequest
            val httpClientUtil = HttpClientUtils.instance
            httpClientUtil?.setVariables(context, mLivenessRequest)
            if (mLivenessRequest.isVideo) {
                val intent = Intent(context, MainLiveNessActivityVideo::class.java)
                intent.putExtra(AppConfig.KEY_BUNDLE_SCREEN, "")
                ContextCompat.startActivity(context, intent, null)
            } else {
                val transaction = supportFragmentManager.beginTransaction()
                val bundle = Bundle()
                bundle.putString(AppConfig.KEY_BUNDLE_SCREEN, "")
                val fragment = MainLiveNessActivity()
                fragment.setFragmentManager(supportFragmentManager)
                fragment.arguments = bundle
                transaction.add(frameView, fragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }

        }

        @Keep
        fun setCustomView(mCustomView: View, mActionView: View?) {
            AppConfig.mCustomView = mCustomView
            AppConfig.mActionView = mActionView
        }

        @Keep
        fun setCustomProgress(mProgressView: View?) {
            AppConfig.mProgressView = mProgressView
        }

        @Keep
        fun setCallbackListener(livenessListener: CallbackLivenessListener?) {
            livenessListener?.let {
                AppConfig.livenessListener = it
            }
        }

        @Keep
        fun setCallbackListenerFace(livenessListener: CallbackLivenessListener?) {
            livenessListener?.let {
                AppConfig.livenessFaceListener = it
            }
        }

        fun clearAllData(context: Context) {
            AppPreferenceUtils(context).removeAllValue()
        }

        @Keep
        fun registerFace(context: Context, mLivenessRequest: LivenessRequest, livenessListener: CallbackLivenessListener?) {
            livenessListener?.let {
                AppConfig.livenessFaceListener = it
            }
            AppConfig.mLivenessRequest = mLivenessRequest
            val httpClientUtil = HttpClientUtils.instance
            httpClientUtil?.setVariables(context, mLivenessRequest)
            if (mLivenessRequest.imageFace.isNullOrEmpty()) {
//                val intent: Intent = if (mLivenessRequest.isVideo) {
//                    Intent(context, MainLiveNessActivityVideo::class.java)
//                } else {
//                    Intent(context, MainLiveNessActivity::class.java)
//                }
//                intent.putExtra(AppConfig.KEY_BUNDLE_SCREEN, AppConfig.TYPE_SCREEN_REGISTER_FACE)
//                ContextCompat.startActivity(context, intent, null)
            } else {
                httpClientUtil?.registerDeviceAndFace(context, mLivenessRequest.imageFace ?: "")
            }
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