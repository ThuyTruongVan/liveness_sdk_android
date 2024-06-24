package com.liveness.sdk.corev3.utils

import android.content.Context
import android.os.Build
import android.util.Log
import com.liveness.sdk.corev3.api.HttpClientUtils.Companion.instance
import com.liveness.sdk.corev3.jws.TOTPGenerator
import org.json.JSONObject
import java.util.UUID

internal class TotpUtils(private val mContext: Context) {
    private var secret: String = ""

    init {
        secret = AppPreferenceUtils(mContext).getTOTPSecret(mContext)
    }

    private fun setSecret(secretString: String) {
        secret = secretString
        Log.d("Thuytv", "------setSecret: $secretString")
        AppPreferenceUtils(mContext).setTOTPSecret(mContext, secretString)
    }

    private var totpSecret: String? = null
        /**
         * return totpSecret read from shared pref
         * if null, then will try to call register device api to get secret
         * can only be call after HttpClientUtil.getInstance().setVariables called(normally called when initialize IOKyc)
         *
         * @return
         */
        private get() {
            if (secret.isNotEmpty()) {
                return secret
            }
            try {
                var a = AppPreferenceUtils(mContext).getTOTPSecret(mContext) ?: AppConfig.mLivenessRequest?.secret
                if (a.isNullOrEmpty() || a.length != 16) {
                    a = AppUtils.getSecretValue()
                }
                var b = AppPreferenceUtils(mContext).getDeviceId() ?: AppConfig.mLivenessRequest?.deviceId
                if (b.isNullOrEmpty()) {
                    b = UUID.randomUUID().toString()
                }
                val request = JSONObject()
                request.put(AppUtils.decodeAndDecrypt(mContext,AppConfig.encrypted_deviceId), b)
                request.put(AppUtils.decodeAndDecrypt(mContext,AppConfig.encrypted_deviceOS), "Android")
                request.put(AppUtils.decodeAndDecrypt(mContext,AppConfig.encrypted_device_name), Build.MANUFACTURER + " " + Build.MODEL)
                request.put(AppUtils.decodeAndDecrypt(mContext,AppConfig.encrypted_period), AppConfig.mLivenessRequest?.duration)
                request.put(AppUtils.decodeAndDecrypt(mContext,AppConfig.encrypted_secret), a)
                val response = instance?.postV3(AppUtils.decodeAndDecrypt(mContext,AppConfig.encrypted_register_device), request)
                var result: JSONObject? = null
                if (response != null && response.length > 0) {
                    result = JSONObject(response)
                }
                if (result != null && result.has("status") && result.getInt("status") == 200) {
                    setSecret(result.getString("data"))
                    AppPreferenceUtils(mContext).setValueString(AppPreferenceUtils.KEY_SIGNATURE, result.getString("signature"))
                } else {
                    secret = ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return secret
        }

    fun getTotp(): String {
        if (totpSecret?.isNotEmpty() == true) {
            val generator = TOTPGenerator()
            Log.d("Thuytv", "-----totpSecret: $totpSecret")
            return generator.generateTOTP(totpSecret!!)
        }
        return ""
    }


//    companion object {
//        private var totpUtils: TotpUtils? = null
//        fun getInstance(context: Context): TotpUtils? {
//            if (totpUtils == null) {
//                totpUtils = TotpUtils(context)
//            }
//            return totpUtils
//        }
//    }
}