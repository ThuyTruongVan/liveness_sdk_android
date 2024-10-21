package com.liveness.sdk.corev4

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.liveness.sdk.corev4.api.HttpClientUtils
import com.liveness.sdk.corev4.model.LivenessRequest
import com.liveness.sdk.corev4.utils.AppConfig
import com.liveness.sdk.corev4.utils.AppPreferenceUtils
import com.liveness.sdk.corev4.utils.CallbackAPIListener
import com.liveness.sdk.corev4.utils.CallbackLivenessListener
import com.liveness.sdk.corev4.utils.RSACryptData

/**
 * Created by Thuytv on 15/04/2024.
 */
@Keep
class LiveNessSDK {
    companion object {
        @Keep
        fun setConfigSDK(context: Context, mLivenessRequest: LivenessRequest) {
            AppConfig.mLivenessRequest = mLivenessRequest
            val httpClientUtil = HttpClientUtils.instance
            httpClientUtil?.setVariables(context, mLivenessRequest)
//            testRSA()
//            val enCryptData = EnCryptData()
//            enCryptData.encryptText(context, AppConfig.key_encrypted_init_transaction, AppConfig.encrypted_init_transaction)
//            enCryptData.encryptText(context, AppConfig.key_encrypted_register_device, AppConfig.encrypted_register_device)
//            enCryptData.encryptText(context, AppConfig.key_encrypted_register_face, AppConfig.encrypted_register_face)
//            enCryptData.encryptText(context, AppConfig.key_encrypted_verify_face, AppConfig.encrypted_verify_face)
//            enCryptData.encryptText(context, AppConfig.key_encrypted_deviceId, AppConfig.encrypted_deviceId)
//            enCryptData.encryptText(context, AppConfig.key_encrypted_deviceOS, AppConfig.encrypted_deviceOS)
//            enCryptData.encryptText(context, AppConfig.key_encrypted_device_name, AppConfig.encrypted_device_name)
//            enCryptData.encryptText(context, AppConfig.key_encrypted_period, AppConfig.encrypted_period)
//            enCryptData.encryptText(context, AppConfig.key_encrypted_secret, AppConfig.encrypted_secret)
//            enCryptData.encryptText(context, AppConfig.key_encrypted_face_image, AppConfig.encrypted_face_image)
//            enCryptData.encryptText(context, AppConfig.key_encrypted_device_id, AppConfig.encrypted_device_id)
//            enCryptData.encryptText(context, AppConfig.key_encrypted_totp, AppConfig.encrypted_totp)
//            enCryptData.encryptText(context, AppConfig.key_encrypted_transaction_id, AppConfig.encrypted_transaction_id)
//            enCryptData.encryptText(context, AppConfig.key_encrypted_image_live, AppConfig.encrypted_image_live)
//            enCryptData.encryptText(context, AppConfig.key_encrypted_color, AppConfig.encrypted_color)
//            enCryptData.encryptText(context, AppConfig.key_encrypted_transaction_id, AppConfig.encrypted_transaction_id)

        }

        @Keep
        fun registerDevice(
            context: Context,
            mRequest: LivenessRequest?,
            callbackAPIListener: CallbackAPIListener?
        ) {
            mRequest?.let {
                AppConfig.mOptionRequest = mRequest
            }
            val httpClientUtil = HttpClientUtils.instance
            httpClientUtil?.registerDevice(context, callbackAPIListener)
        }

        @Keep
        fun registerFace(
            context: Context,
            faceImage: String,
            mRequest: LivenessRequest?,
            callbackAPIListener: CallbackAPIListener?
        ) {
            mRequest?.let {
                AppConfig.mOptionRequest = mRequest
            }
            val httpClientUtil = HttpClientUtils.instance
            httpClientUtil?.registerFace(context, faceImage, callbackAPIListener)
        }

        @Keep
        fun initTransaction(
            context: Context,
            mRequest: LivenessRequest?,
            readCardId: String?,
            callbackAPIListener: CallbackAPIListener?
        ) {
            mRequest?.let {
                AppConfig.mOptionRequest = mRequest
            }
            val httpClientUtil = HttpClientUtils.instance
            httpClientUtil?.initTransaction(context, readCardId, callbackAPIListener)
        }

        @Keep
        fun checkLiveNessFlash(
            context: Context,
            transactionId: String,
            liveImage: String,
            colorBg: Int,
            mRequest: LivenessRequest?,
            callbackAPIListener: CallbackAPIListener?
        ) {
            mRequest?.let {
                AppConfig.mOptionRequest = mRequest
            }
            val httpClientUtil = HttpClientUtils.instance
            httpClientUtil?.checkLiveNessFlash(
                context,
                transactionId,
                liveImage,
                colorBg,
                callbackAPIListener
            )
        }

        @Keep
        fun checkLiveNess(
            context: Context,
            liveImage: String,
            colorBg: Int,
            mRequest: LivenessRequest?,
            callbackAPIListener: CallbackAPIListener?
        ) {
            mRequest?.let {
                AppConfig.mOptionRequest = mRequest
            }
            val httpClientUtil = HttpClientUtils.instance
            httpClientUtil?.checkLiveNess(context, liveImage, colorBg, callbackAPIListener)
        }

        @Keep
        fun checkLiveNessFlashV2(
            context: Context,
            transactionId: String,
            img: String,
            img1: String,
            img2: String,
            img3: String,
            mRequest: LivenessRequest?,
            callbackAPIListener: CallbackAPIListener?
        ) {
            mRequest?.let {
                AppConfig.mOptionRequest = mRequest
            }
            val httpClientUtil = HttpClientUtils.instance
            httpClientUtil?.checkLiveNessFlashV2(
                context,
                transactionId,
                img,
                img1,
                img2,
                img3,
                callbackAPIListener
            )
        }

        @Keep
        fun startLiveNess(
            context: Context,
            mLivenessRequest: LivenessRequest,
            supportFragmentManager: FragmentManager,
            container: Int,
            livenessListener: CallbackLivenessListener?,
            isShowToolbar: Boolean = true
        ) {
            livenessListener?.let {
                AppConfig.livenessListener = it
            }
            AppConfig.mLivenessRequest = mLivenessRequest
            val httpClientUtil = HttpClientUtils.instance
            httpClientUtil?.setVariables(context, mLivenessRequest)
            val transaction = supportFragmentManager.beginTransaction()
            val bundle = Bundle()
            bundle.putString(AppConfig.KEY_BUNDLE_SCREEN, "")
            bundle.putBoolean(AppConfig.KEY_BUNDLE_BOOLEAN, isShowToolbar)
            val fragment = FaceMatchFragment()
            fragment.setFragmentManager(supportFragmentManager)
            fragment.arguments = bundle
            transaction.replace(container, fragment)
            transaction.addToBackStack(FaceMatchFragment::class.java.name)
            transaction.commit()
        }

        @Keep
        fun startLiveNess(
            context: Context,
            mLivenessRequest: LivenessRequest,
            livenessListener: CallbackLivenessListener?
        ) {
            livenessListener?.let {
                AppConfig.livenessListener = it
            }
            AppConfig.mLivenessRequest = mLivenessRequest
            val httpClientUtil = HttpClientUtils.instance
            httpClientUtil?.setVariables(context, mLivenessRequest)
            val intent = Intent(context, FaceMatchActivity::class.java)
            ContextCompat.startActivity(context, intent, null)
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

        @Keep
        fun clearAllData(context: Context) {
            AppPreferenceUtils(context).removeAllValue()
        }

//        @Keep
//        fun registerFace(context: Context, mLivenessRequest: LivenessRequest, livenessListener: CallbackLivenessListener?) {
//            livenessListener?.let {
//                AppConfig.livenessFaceListener = it
//            }
//            AppConfig.mLivenessRequest = mLivenessRequest
//            val httpClientUtil = HttpClientUtils.instance
//            httpClientUtil?.setVariables(context, mLivenessRequest)
//            if (mLivenessRequest.imageFace.isNullOrEmpty()) {
//                val intent = Intent(context, MainLiveNessActivityVideo::class.java)
//                intent.putExtra(AppConfig.KEY_BUNDLE_SCREEN, AppConfig.TYPE_SCREEN_REGISTER_FACE)
//                ContextCompat.startActivity(context, intent, null)
//            } else {
//                httpClientUtil?.registerDeviceAndFace(context, mLivenessRequest.imageFace ?: "")
//            }
//        }

        @Keep
        fun registerFace(
            context: Context,
            mLivenessRequest: LivenessRequest,
            supportFragmentManager: FragmentManager,
            frameView: Int,
            livenessListener: CallbackLivenessListener?,
            isShowToolbar: Boolean = true
        ) {
            livenessListener?.let {
                AppConfig.livenessFaceListener = it
            }
            AppConfig.mLivenessRequest = mLivenessRequest
            val httpClientUtil = HttpClientUtils.instance
            httpClientUtil?.setVariables(context, mLivenessRequest)
            if (mLivenessRequest.imageFace.isNullOrEmpty()) {
                val transaction = supportFragmentManager.beginTransaction()
                val bundle = Bundle()
                bundle.putString(AppConfig.KEY_BUNDLE_SCREEN, AppConfig.TYPE_SCREEN_REGISTER_FACE)
                bundle.putBoolean(AppConfig.KEY_BUNDLE_BOOLEAN, isShowToolbar)
                val fragment = FaceMatchFragment()
                fragment.setFragmentManager(supportFragmentManager)
                fragment.arguments = bundle
                transaction.add(frameView, fragment)
                transaction.addToBackStack(FaceMatchFragment::class.java.name)
                transaction.commit()
            } else {
                httpClientUtil?.registerDeviceAndFace(context, mLivenessRequest.imageFace ?: "")
            }
        }

        @Keep
        fun registerFace(
            context: Context,
            mLivenessRequest: LivenessRequest,
            livenessListener: CallbackLivenessListener?
        ) {
            livenessListener?.let {
                AppConfig.livenessFaceListener = it
            }
            AppConfig.mLivenessRequest = mLivenessRequest
            val httpClientUtil = HttpClientUtils.instance
            httpClientUtil?.setVariables(context, mLivenessRequest)
            if (mLivenessRequest.imageFace.isNullOrEmpty()) {
                val intent = Intent(context, FaceMatchActivity::class.java)
                val bundle = Bundle()
                bundle.putString(AppConfig.KEY_BUNDLE_SCREEN, AppConfig.TYPE_SCREEN_REGISTER_FACE)
                intent.putExtras(bundle)
                ContextCompat.startActivity(context, intent, null)
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

        @Keep
        fun checkVersion(): String {
            return "v1.1.7"
        }

        fun testRSA() {
            val rsa = RSACryptData()
            val url_check_liveness = rsa.encrypt("/eid/v3/checkLiveness")
            val deviceId = rsa.encrypt("deviceId")
            val period = rsa.encrypt("period")
            val clientTransactionId = rsa.encrypt("clientTransactionId")
            val url_init_transaction = rsa.encrypt("/eid/v3/initTransaction")
            val totp = rsa.encrypt("totp")
            val transaction_id = rsa.encrypt("transaction_id")
            val image_live = rsa.encrypt("image_live")
            val color = rsa.encrypt("color")
            val deviceid = rsa.encrypt("deviceid")
            val url_verify_face = rsa.encrypt("/eid/v3/verifyFace")
            val face_image = rsa.encrypt("face_image")
            val url_register_face = rsa.encrypt("/eid/v3/registerFace")
            val deviceOs = rsa.encrypt("deviceOs")
            val deviceName = rsa.encrypt("deviceName")
            val secret = rsa.encrypt("secret")
            val url_register_device = rsa.encrypt("/eid/v3/registerDevice")
//
            Log.d("Thuytv", "-----url_check_liveness: " + url_check_liveness)
            Log.d("Thuytv", "-----deviceId: " + deviceId)
            Log.d("Thuytv", "-----period: " + period)
            Log.d("Thuytv", "-----clientTransactionId: " + clientTransactionId)
            Log.d("Thuytv", "-----url_init_transaction: " + url_init_transaction)
            Log.d("Thuytv", "-----totp: " + totp)
            Log.d("Thuytv", "-----transaction_id: " + transaction_id)
            Log.d("Thuytv", "-----image_live: " + image_live)
            Log.d("Thuytv", "-----color: " + color)
            Log.d("Thuytv", "-----deviceid: " + deviceid)
            Log.d("Thuytv", "-----url_verify_face: " + url_verify_face)
            Log.d("Thuytv", "-----face_image: " + face_image)
            Log.d("Thuytv", "-----url_register_face: " + url_register_face)
            Log.d("Thuytv", "-----deviceOs: " + deviceOs)
            Log.d("Thuytv", "-----deviceName: " + deviceName)
            Log.d("Thuytv", "-----secret: " + secret)
            Log.d("Thuytv", "-----url_register_device: " + url_register_device)
            Log.d("Thuytv", "-----live1: " + rsa.encrypt("image_live1"))
            Log.d("Thuytv", "-----live2: " + rsa.encrypt("image_live2"))
            Log.d("Thuytv", "-----live3: " + rsa.encrypt("image_live3"))
            Log.d("Thuytv", "-----attemp: " + rsa.encrypt("/eid/v3/initLivenessAttemp"))
            Log.d("Thuytv", "-----duration: " + rsa.encrypt("duration"))

//            val strDecrypte = rsa.decrypt(AppConfig.encrypted_transaction_id)
//            Log.d("Thuytv", "-----strDecrypte: " + strDecrypte)
        }

    }
}