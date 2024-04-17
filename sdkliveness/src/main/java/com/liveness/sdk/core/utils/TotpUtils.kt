package com.liveness.sdk.core.utils

import android.content.Context
import android.os.Build
import android.util.Log
import com.liveness.sdk.core.api.HttpClientUtils.Companion.instance
import com.liveness.sdk.core.jws.TOTPGenerator
import com.liveness.sdk.core.utils.AppUtils.getDeviceId
import org.json.JSONObject

internal class TotpUtils private constructor(private val mContext: Context) {
    private var secret: String = ""
    private val generator: TOTPGenerator

    init {
        secret = AppPreferenceUtils(mContext).getTOTPSecret(mContext)
        generator = TOTPGenerator()
    }

    private fun setSecret(secretString: String) {
        secret = secretString
        AppPreferenceUtils(mContext).setTOTPSecret(mContext, secretString)
    }

    private val totpSecret: String
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
                Log.d("Thuytv", "---response: $response")
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
        if (totpSecret.isEmpty()) {
            return ""
        }
        return generator.generateTOTP(totpSecret)
    }


    companion object {
        private var totpUtils: TotpUtils? = null
        fun getInstance(context: Context): TotpUtils? {
            if (totpUtils == null) {
                totpUtils = TotpUtils(context)
            }
            return totpUtils
        }
    }
}