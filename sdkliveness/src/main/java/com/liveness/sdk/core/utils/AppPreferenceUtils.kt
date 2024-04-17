package com.liveness.sdk.core.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by Thuytv8 on 08/04/2019.
 */
internal class AppPreferenceUtils(context: Context?) {

    private var IShare: SharedPreferences? = null

    init {
        if (context != null)
            IShare = context.getSharedPreferences(
                context.applicationInfo.packageName,
                Context.MODE_PRIVATE
            )
    }

    /////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////
    ////// Remove Share Preferences
    /////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////
    fun PreferenceUtilRemove(context: Context?) {
        if (context != null)
            IShare = context.getSharedPreferences(
                context.applicationInfo.packageName,
                Context.MODE_PRIVATE
            )
        IShare?.edit()?.clear()?.apply()
    }

    /////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////
    ////// Get Set By Other Key
    /////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////
    fun setValueLong(key: String, `val`: Long) {
        IShare?.edit()?.putLong(key, `val`)?.apply()
    }

    fun setValueString(key: String?, `val`: String?) {
        IShare?.edit()?.putString(key, `val`)?.apply()
    }

    fun setValueBoolean(key: String, `val`: Boolean) {
        IShare?.edit()?.putBoolean(key, `val`)?.apply()
    }

    fun setValueInteger(key: String, `val`: Int) {
        IShare?.edit()?.putInt(key, `val`)?.apply()
    }

    fun getValueInteger(key: String): Int? {
        return IShare?.getInt(key, -1)
    }

    fun getValueLong(key: String): Long? {
        return IShare?.getLong(key, -1)
    }

    fun getValueBoolean(key: String): Boolean {
        return IShare?.getBoolean(key, false) ?: false
    }

    fun getValueString(key: String?): String? {
        return IShare?.getString(key, "")?.trim { it <= ' ' }
    }

    fun getValueString(key: String?, value: String?): String? {
        return IShare?.getString(key, value)?.trim { it <= ' ' }
    }

    fun removeValue(key: String) {
        IShare?.edit()?.remove(key)?.apply()
    }

    fun setTOTPSecret(context: Context, strKey: String) {
        val secret = KeyStoreUtils.getInstance(context)?.encryptData(strKey)
        setValueString(KEY_TOTP_SECRET, secret)
    }

    fun getTOTPSecret(context: Context): String {
        val secret = getValueString(KEY_TOTP_SECRET)
        if (secret.isNullOrEmpty()) {
            return ""
        }
        return KeyStoreUtils.getInstance(context)?.decryptData(secret) ?: ""
    }


    companion object {
        const val KEY_DEVICE_ID = "KEY_DEVICE_ID"
        const val KEY_TOTP_SECRET = "KEY_TOTP_SECRET"
        const val KEY_SIGNATURE = "KEY_SIGNATURE"
    }
}

