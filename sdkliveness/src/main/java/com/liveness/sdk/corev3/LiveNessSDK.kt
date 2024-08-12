package com.liveness.sdk.corev3

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.liveness.sdk.corev3.api.HttpClientUtils
import com.liveness.sdk.corev3.jws.JwsUtils
import com.liveness.sdk.corev3.model.LivenessRequest
import com.liveness.sdk.corev3.utils.AppConfig
import com.liveness.sdk.corev3.utils.AppPreferenceUtils
import com.liveness.sdk.corev3.utils.CallbackAPIListener
import com.liveness.sdk.corev3.utils.CallbackLivenessListener
import com.liveness.sdk.corev3.utils.RSACryptData

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

        fun setClientTransactionId(clientTransactionId: String) {
            AppConfig.clientTransactionIdReadCard = clientTransactionId
        }

        @Keep
        fun registerDevice(context: Context, mRequest: LivenessRequest?, callbackAPIListener: CallbackAPIListener?) {
            mRequest?.let {
                AppConfig.mOptionRequest = mRequest
            }
            val httpClientUtil = HttpClientUtils.instance
            httpClientUtil?.registerDevice(context, callbackAPIListener)
        }

        @Keep
        fun registerFace(context: Context, faceImage: String, mRequest: LivenessRequest?, callbackAPIListener: CallbackAPIListener?) {
            mRequest?.let {
                AppConfig.mOptionRequest = mRequest
            }
            val httpClientUtil = HttpClientUtils.instance
            httpClientUtil?.registerFace(context, faceImage, callbackAPIListener)
        }

        @Keep
        fun initTransaction(context: Context, mRequest: LivenessRequest?, readCardId: String?, callbackAPIListener: CallbackAPIListener?) {
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
            httpClientUtil?.checkLiveNessFlash(context, transactionId, liveImage, colorBg, callbackAPIListener)
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
                transaction.replace(frameView, fragment)
                transaction.addToBackStack(MainLiveNessActivity::class.java.name)
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

        @Keep
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
//                if (mLivenessRequest.isVideo) {
                val intent = Intent(context, MainLiveNessActivityVideo::class.java)
                intent.putExtra(AppConfig.KEY_BUNDLE_SCREEN, AppConfig.TYPE_SCREEN_REGISTER_FACE)
                ContextCompat.startActivity(context, intent, null)
//                }
            } else {
                httpClientUtil?.registerDeviceAndFace(context, mLivenessRequest.imageFace ?: "")
            }
        }

        @Keep
        fun registerFace(
            context: Context, mLivenessRequest: LivenessRequest, supportFragmentManager: FragmentManager,
            frameView: Int, livenessListener: CallbackLivenessListener?
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
                val fragment = MainLiveNessActivity()
                fragment.setFragmentManager(supportFragmentManager)
                fragment.arguments = bundle
                transaction.add(frameView, fragment)
                transaction.addToBackStack(MainLiveNessActivity::class.java.name)
                transaction.commit()
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

        //        fun testRSA() {
//            val rsa = RSACryptData()
//            val url_check_liveness = rsa.encrypt("/eid/v3/checkLiveness")
//            val deviceId = rsa.encrypt("deviceId")
//            val period = rsa.encrypt("period")
//            val clientTransactionId = rsa.encrypt("clientTransactionId")
//            val url_init_transaction = rsa.encrypt("/eid/v3/initTransaction")
//            val totp = rsa.encrypt("totp")
//            val transaction_id = rsa.encrypt("transaction_id")
//            val image_live = rsa.encrypt("image_live")
//            val color = rsa.encrypt("color")
//            val deviceid = rsa.encrypt("deviceid")
//            val url_verify_face = rsa.encrypt("/eid/v3/verifyFace")
//            val face_image = rsa.encrypt("face_image")
//            val url_register_face = rsa.encrypt("/eid/v3/registerFace")
//            val deviceOs = rsa.encrypt("deviceOs")
//            val deviceName = rsa.encrypt("deviceName")
//            val secret = rsa.encrypt("secret")
//            val url_register_device = rsa.encrypt("/eid/v3/registerDevice")
////
//            Log.d("Thuytv", "-----url_check_liveness: " + url_check_liveness)
//            Log.d("Thuytv", "-----deviceId: " + deviceId)
//            Log.d("Thuytv", "-----period: " + period)
//            Log.d("Thuytv", "-----clientTransactionId: " + clientTransactionId)
//            Log.d("Thuytv", "-----url_init_transaction: " + url_init_transaction)
//            Log.d("Thuytv", "-----totp: " + totp)
//            Log.d("Thuytv", "-----transaction_id: " + transaction_id)
//            Log.d("Thuytv", "-----image_live: " + image_live)
//            Log.d("Thuytv", "-----color: " + color)
//            Log.d("Thuytv", "-----deviceid: " + deviceid)
//            Log.d("Thuytv", "-----url_verify_face: " + url_verify_face)
//            Log.d("Thuytv", "-----face_image: " + face_image)
//            Log.d("Thuytv", "-----url_register_face: " + url_register_face)
//            Log.d("Thuytv", "-----deviceOs: " + deviceOs)
//            Log.d("Thuytv", "-----deviceName: " + deviceName)
//            Log.d("Thuytv", "-----secret: " + secret)
//            Log.d("Thuytv", "-----url_register_device: " + url_register_device)
//
////            val strDecrypte = rsa.decrypt(AppConfig.encrypted_transaction_id)
////            Log.d("Thuytv", "-----strDecrypte--encrypted_transaction_id: " + strDecrypte)
//        }
        fun testDecrypt() {
            val str =
                "eyJraWQiOiJjODg3MDdlZi03YzQzLTQzNDItOGY0Yi0wNzE1NDE0MTcwZWEiLCJhbGciOiJQUzI1NiIsImN0eSI6IkpXRSIsInR5cCI6IkpPU0UifQ.ZXlKcFlYUWlPakUzTWpFek1UUTJOekFzSW1WdVl5STZJa0V4TWpoSFEwMGlMQ0poYkdjaU9pSlNVMEV0VDBGRlVDMHlOVFlpTENKcmFXUWlPaUpqT0RnM01EZGxaaTAzWXpRekxUUXpOREl0T0dZMFlpMHdOekUxTkRFME1UY3daV0VpZlEuT2I0cGtRR2pwWnZac29iTHVaVDUzSTBIRW1ZcUMwT1VUeXlReUJUMTRGS1cwWlBYZFo4WWdET0RzV3VvOUVpcWVweG5PWXJncFlzOEF6ZUY1UlQzYi1QdWpVUnNhOThockhOdVZ1bGZubWI4eHQ1eENVcUlOVGk0Vm9qRUxkcmlHQzFPSXBMakJlVUEzUkREekVPV1lZb04wWXVNcWVYM0tVVm9OUUtKaU5ULWRoMk9WMWJYRkJtTDBoUUZhN0FIcXNCSTVuRjZ0eVd0TkFRaVZuSzJYdDM0dnZlMkcxd2lXWnJXWG5ZLUhNZUhYblNZODZpMmpGRFFNeHpveXQ3cXA2cWxYbkM3a3dlS0cyVEZGMVVFX3pMT2VULTVHTGYzYjMtQ3NDZDZ3UWliOGljaU5DeGFsSE5CVXk5V1hBNnFtS3gtVThXMXFHLXY2cW5ZU3FEUy13LkpMdkt4RnlSaFhaSDY5YlQuNV9CZm9rQWd2OTVpUm9UNklSa2dKN2toVGxtX2cya3BrQ0JfeTlVRnppMnZkWjZFeG9OTFU4MTB4UVJWaU5Jc2VUZWstbUE5QnhRUWFiSzBkdmFIUkp0Q3doY2M1b3pxeWU2c0h1TjFNTzZyM3Nwb2ZPU2xpSG4yeFRoamgtbVc3bWpCOUpmaDY3QWE5VlJkXzBIYlJHcDdYaUNiNXR2aDh1amdvZmhONE5NSXFTbkozeFRPYkV1RFdOazVQNjNpOTJjQVJuZDJsTENuQ0RiOWIxejZUWVlEOFdVbGdhY1QwOThuQ1Q3TUNWWUdaSVRTMmstNXZKLWFQaWRVQVVaelBiMTI3T0ZlMGYxZHlSdWt5RHBOZ0pBdWVWZDBSYmVpaF9BN05LM3N3b1Ytd3NJb2dUQVFCMkpZcDdKZnltQUdrMVl6UXQtWm1QY2lLdUU1a1BXSURxcTk3bTZkZjN3US1jOXRQQTdQNjFkOFE2cGVnYnhMNXF6VF9VeEszNGRGM2JPdkc2WndBLTVXOUxfZjZvX3RfMHd4SUI2aHNpbTdTWXBLODhhRVFXT2twbHBYaGNCMnU0SWc1M3NocUtfNGtBa1kweVM2N21GejhtZzcxOWtrbHRZd0xhUHhoWU8xZWVZQ1VINTVDek5zTjhtMzI1djMzVXpjaTBKbm1GV1pnTmluUDU0NklxZkR0Xzc4R3IwZWZxOHpWQXRNLVc3X3ZzaGtCMDg4OE5KSllYcGl0ZEZudFVvLXM4d1didzJId3NwekV1R2VPandrUXJEQ2RsZE9scVRLbXJYOXFLclM4QXBhNWc3bVVKYTZJaXM2X2FxOE96NVdYVU9ZWGZNeTNGa082aTJUaGJjRWxTZDc5TkV1MUZaQU1ycGlFWjNiZGMzNUlpWGVxYTVWWDNHbXZKbkVXY3FORGtIOUJtbFpLSlpQYzhYOV9QcjVadEZEa2VQSFhjVU1sQjI2cGpFQ2dsbkxneHVEU2ROZDNFU29TN1FlMmdrNVpyME1rSjZZLWdIcXI2bzdVUzZxUkxlM3A5OGt0dms0N0hmVDZGUDhoNlc3RFJVTlRUbGNRSVIwYks1SXJBVkEySHh5Y0w4cWlqVVhMcGgwM0QyWUw1YzVwckhpZTRxNVhmRGUzOGNHa2FObHg4REZVRWplNXV1STIwbFFzbjlfb2MtUXpUeU5VSzFzVVNwZW5McC1WcS1yVUFZSWlPTTdHeVRzM0xyaURTR1VLbU4yanM4Zm5lMU1mcjJOU1BPZF9FZXRGOHI3aVFzRkNlV3pnX01EbEtTeTQ2NThUWldTVWtmQ2YyRFI4MFptNUEwazdCVEF1TnJ0ckp6US1OSFR4MDNGb0Qxa1lpcllFdE1BcHY5RmdyUzlpZ2pzRF9ELU1PcEhJUEVHX21NZExBVWJieHJVSzdfYVhwcndnWEFzZWJkZGhrWjFoWkpQUzV4QWkxZ2ZwRTZYbGhrZTNXNGl2WHZxY0xsQWhSSXU0c2JOa2pOTks3d1pISXhCckJPZ1VVN2JnQmJzZEpHSG5fQ3J1TnBmSjdwYlZfR0FjcmNycGtyaHZXbXdNc2xJcmJRM3NMUGNuYTBYdXRBWHg4cjBBN0hiVUlmdHJCQThuVlpxd21CNzdQdlF1c2QxVWttYXNwaHhLQnRfSENXbWwwMzQxbUNrcUlZWVhzX2NKWlZDcjBuS1UwYk5yR195NHhpMEtNdUZXWGJCd0REc2pSOUQ3bXlDNWJSUlN2aHowZ1BWYm1HZXFtOEN4TjJRMFlJYmFHYmlsejgtUHh2b0FicXBxcW9RS2VxYTRSYi01eTJmTS1HSDk2U2V4UTF0X2w4SzcwUWdaZFVQR055NEdxaXRvQXNBdzRGY2NUaGYwc3hTQTFuWXg4bXVWMG41VDFEY1lqa1plR2pVcnVmV2JVdXlyOWpNTUcxWUlTOGx6MjhtTmVnakVESWFuYzhoSmtaQnJnM25hX3NDR1NsVlBpWjZMbUVNS2RKSlB6TVB3Mk1ZQmtHcm5jbk1IZ2pNanRsM3hYNGtXYlhjYmtEVkd5WWxfSjRFbWRXemNUTjBIaTZmTmQzZkphcl9XbWxpM3FpaExhRkh3V3FzSzVIbWpEWXpIZVVtcFdLYU9QZGlvUzZSRU50QjZ1bWsyaWpMT2ppTnNBV2FBSUJQRkZybGJiSGJyNHdZVHJWQmUwSWlERVVYVXgtNkk3b1JKY3ppSTRDSldXWndKRzVwcGQzcExRUnNtZzF0Y3pQYVJrTXFPSHg4U1EwUEIxRXZ0MGNvcVRGSzd2RFYwUjZNTWpYYXF2RDJDbnlpbXptbEFmbk1UZjN2cGJTOHY2Y0JkNWk3UTBJTmpjeWhLem5WMjJxMUdBY2poanFRRUdWdGFOeWp6MFoxMUxMNlpMaV8yZWNBTEhLOGNkTDBFZXJEWU5OX1NZb09YNmZPbjRIdlppdEpCTTNXejVlZ1lNdXliTHJiQ2pJQTRsdUh0LXBESVo4eVBGQmxTYm9iUDJqWE1fOEZCZjJnQU80WEdqT3RsczZUdnlWS09kVkxMbHExNzJseUpaQkR4N2tlcmh5YWhhQzJNUEEzRTJQdHhsbllCQVI5aGNhWEZfV2xhUklTSFZITVgwXy1TZEJHdTlILTJQbmwtM2ZqWmlCaVdXZjEtOExTV3RWWmgyTnNqVzhkMEZqOEpkUUlHWWp2UDhkUl9SQU5kYW1QOFhQNEx1Y2tENUdWOFp6U1l2Wjl0ZGZBS1ctSGJJRmdBblNJU2ZMTDlYalpVck1FTHBHN2hHeThQNExPeGdoV3lQMWhKb25BTEEySlV2V2Y2LUdNYzJueW84NlRkM0dtQ05zZnI1TmxVSld0VnUyMlhXb1Fna25OSlhKYldNaXFPeGNoZjBDTk05Nk12aE1GeE45Z3pscGFlQmRsOFV3aHlOeFdyOUZ0QzV4NXJNSktMcHJYa09DOHctX2NISDJnYmhWdmhBQmh0djlBalB2clFlMF9TN19paDlVWE93b0NFUGtQc1hWcFZfdWZuNkZfMF9JRjBUSWRDQjVndzI4aWFYTWd5UlFGWllPazRjeVZ2bmdJWmM2SjIyWXpFWVdBaXMzRUo5TWs5eDQ2amxVaU4yLVBDdmR3dGtLRTl6a0E2bUVjbmJEMWtZSHdzNmlsX2xoYVFfQ3BmTENXUUVGbWVpNEFoYnpNelRpclhNbUJ2cE5VNW"
            val data = JwsUtils.getInstance().decryptJWS(str)
            Log.d("Thuytv", "----testDecrypt: $data")
        }
    }
}