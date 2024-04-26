package com.liveness.sdk.core.utils

import android.content.Context
import android.os.Build
import android.util.Log
import com.liveness.sdk.core.api.HttpClientUtils.Companion.instance
import com.liveness.sdk.core.jws.TOTPGenerator
import com.liveness.sdk.core.utils.AppUtils.getDeviceId
import org.json.JSONObject

internal class TotpUtils(private val mContext: Context) {
    private var secret: String = ""

    init {
        secret = AppPreferenceUtils(mContext).getTOTPSecret(mContext)
    }

    private fun setSecret(secretString: String) {
        secret = secretString
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
                val request = JSONObject()
                request.put("deviceId", AppConfig.mLivenessRequest?.deviceId)
                request.put("deviceOs", "Android")
                request.put("deviceName", Build.MANUFACTURER + " " + Build.MODEL)
                request.put("period", AppConfig.mLivenessRequest?.duration)
                request.put("secret", AppConfig.mLivenessRequest?.secret)
                val response = instance?.postV3("/eid/v3/registerDevice", request)
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