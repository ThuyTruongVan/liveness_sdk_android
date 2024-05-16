package com.liveness.sdk.core.api

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.liveness.sdk.core.jws.JwsUtils
import com.liveness.sdk.core.model.LivenessModel
import com.liveness.sdk.core.model.LivenessRequest
import com.liveness.sdk.core.utils.AppConfig
import com.liveness.sdk.core.utils.AppPreferenceUtils
import com.liveness.sdk.core.utils.AppUtils
import com.liveness.sdk.core.utils.AppUtils.showLog
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.Security
import java.security.Signature
import java.security.SignatureException
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.util.UUID

internal class HttpClientUtils {
    private var baseUrl = "https://face-matching.vietplus.eu"
    private var appId: String? = null
    private var privateKey: String? = null
    var cardId: String? = null
    private var requestId: String? = null
    private var context: Context? = null
    private val jwsUtils = JwsUtils.getInstance()


    companion object {
        var instance: HttpClientUtils? = null
            get() {
                if (field == null) {
                    field = HttpClientUtils()
                }
                return field
            }
            private set

        private fun convertStreamToString(inputStream: InputStream?): String {
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = StringBuilder()
            var line: String? = null
            try {
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    inputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return sb.toString()
        }
    }

    fun doPost(appId: String?, urlString: String, requestBody: JSONObject, signature: String?, optionalHeader: Map<String, String>?): String? {
        try {
            if (requestId == null) {
                requestId = if (requestBody.has("request_id")) requestBody.getString("request_id") else null
            }
        } catch (e: JSONException) {
        }
        return doPost(urlString, requestBody, signature, requestId, optionalHeader)
    }

    fun doPost(urlString: String, requestBody: JSONObject, signature: String?, requestId: String?, optionalHeader: Map<String, String>?): String? {
        try {
            showLog("HttpClientUtilBEGIN ==> Url: [$urlString], Body : [$requestBody]")
            val url = URL(urlString)
            var urlConnection: HttpURLConnection? = null
            try {
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "POST"
                urlConnection.readTimeout = 100000
                urlConnection.connectTimeout = 100000
                urlConnection.useCaches = false
                urlConnection.doInput = true
                urlConnection.doOutput = true
                urlConnection.setRequestProperty("Content-Type", "application/json")
                urlConnection.setRequestProperty("appid", appId)
//                urlConnection.setRequestProperty("devicetype", "android")
//                urlConnection.setRequestProperty("sendDate", toISO8601UTC(Date()))
                if (requestId != null) {
                    urlConnection.setRequestProperty("request_id", requestId)
                }
                if (signature != null) {
                    urlConnection.setRequestProperty("signature", signature)
                }
                if (optionalHeader != null) {
                    for ((key, value) in optionalHeader) {
                        urlConnection.setRequestProperty(key, value)
                    }
                }
                if (AppConfig.mLivenessRequest?.optionHeader?.isNotEmpty() == true) {
                    for ((key, value) in AppConfig.mLivenessRequest?.optionHeader!!) {
                        urlConnection.setRequestProperty(key, value)
                    }
                }
                val os = urlConnection.outputStream
                os.write(requestBody.toString().toByteArray(StandardCharsets.UTF_8))
                os.flush()
                os.close()
                val responseCode = urlConnection.responseCode
                if (responseCode < 500) {
                    val inputStream: InputStream = BufferedInputStream(urlConnection.inputStream)
                    val response = readStream(inputStream)
                    showLog("HttpClientUtilEND ==> Result : [$response]")
                    return response
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                urlConnection?.disconnect()
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return null
    }

    private fun doPostV3(urlString: String, requestBody: JSONObject, optionalHeader: Map<String, String>?): String? {
        val encryptedRequestBody = JSONObject()
        if (AppConfig.mLivenessRequest?.optionRequest?.isNotEmpty() == true) {
            for ((key, value) in AppConfig.mLivenessRequest?.optionRequest!!) {
                requestBody.put(key, value)
            }
        }
        val encryptedBody = jwsUtils.encrypt(requestBody)
        try {
            showLog("requestBody: ${requestBody.toString()}")
            showLog("jws: $encryptedBody")

            encryptedRequestBody.put("jws", encryptedBody)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return doPost(urlString, encryptedRequestBody, null, requestId, optionalHeader)
    }

    @Throws(IOException::class)
    private fun readStream(`in`: InputStream): String {
        val br = BufferedReader(InputStreamReader(`in`))
        val sb = StringBuilder()
        var line: String? = null
        while (br.readLine().also { line = it } != null) {
            sb.append(
                """
    $line
    
    """.trimIndent()
            )
        }
        br.close()
        return sb.toString()
    }

    private fun genericSignature(data: JSONObject, key: String?): String? {
        return try {
            val privateKey = key!!.replace("-----BEGIN PRIVATE KEY-----".toRegex(), "").replace("-----END PRIVATE KEY-----".toRegex(), "").replace("\n".toRegex(), "")
            Security.insertProviderAt(BouncyCastleProvider(), 1)
            val keyByte = Base64.decode(privateKey, Base64.DEFAULT)
            val keySpec = PKCS8EncodedKeySpec(keyByte)
            val kf = KeyFactory.getInstance("RSA")
            val rsaPreKey = kf.generatePrivate(keySpec)
            val sign = Signature.getInstance("SHA256withRSA")
            val dataString = data.toString().replace("\\/", "/")
            sign.initSign(rsaPreKey)
            sign.update(dataString.toByteArray(StandardCharsets.UTF_8))
            val signatureBytes = sign.sign()
            org.bouncycastle.util.encoders.Base64.toBase64String(signatureBytes).replace("\n".toRegex(), "")
        } catch (e: NoSuchAlgorithmException) {
            null
        } catch (e: InvalidKeySpecException) {
            null
        } catch (e: InvalidKeyException) {
            null
        } catch (e: SignatureException) {
            null
        }
    }

    fun multipartRequest(appId: String?, signature: String?, urlTo: String?, parmas: Map<String, String?>, files: Map<String, String?>, fileMimeType: String): String? {
        var connection: HttpURLConnection? = null
        var outputStream: DataOutputStream? = null
        var inputStream: InputStream? = null
        val twoHyphens = "--"
        val boundary = "*****" + java.lang.Long.toString(System.currentTimeMillis()) + "*****"
        val lineEnd = "\r\n"
        var result = ""
        var bytesRead: Int
        var bytesAvailable: Int
        var bufferSize: Int
        var buffer: ByteArray
        val maxBufferSize = 1 * 1024 * 1024
        try {
            val url = URL(urlTo)
            connection = url.openConnection() as HttpURLConnection
            connection!!.readTimeout = 100000
            connection.connectTimeout = 100000
            connection.doInput = true
            connection.doOutput = true
            connection.useCaches = false
            connection.requestMethod = "POST"
            connection.setRequestProperty("appid", appId)
            connection.setRequestProperty("signature", signature)
            connection.setRequestProperty("Connection", "Keep-Alive")
            connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0")
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            outputStream = DataOutputStream(connection.outputStream)
            var keys = files.keys.iterator()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = files[key]!!
                val q = value.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val idx = q.size - 1
                val file = File(value)
                val fileInputStream = FileInputStream(file)
                outputStream.writeBytes(twoHyphens + boundary + lineEnd)
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + q[idx] + "\"" + lineEnd)
                outputStream.writeBytes("Content-Type: $fileMimeType$lineEnd")
                outputStream.writeBytes("Content-Transfer-Encoding: binary$lineEnd")
                outputStream.writeBytes(lineEnd)
                bytesAvailable = fileInputStream.available()
                bufferSize = Math.min(bytesAvailable, maxBufferSize)
                buffer = ByteArray(bufferSize)
                bytesRead = fileInputStream.read(buffer, 0, bufferSize)
                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize)
                    bytesAvailable = fileInputStream.available()
                    bufferSize = Math.min(bytesAvailable, maxBufferSize)
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize)
                }
                outputStream.writeBytes(lineEnd)
                fileInputStream.close()
            }
            // Upload POST Data
            keys = parmas.keys.iterator()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = parmas[key]
                outputStream.writeBytes(twoHyphens + boundary + lineEnd)
                outputStream.writeBytes("Content-Disposition: form-data; name=\"$key\"$lineEnd")
                outputStream.writeBytes(lineEnd)
                outputStream.writeBytes(value)
                outputStream.writeBytes(lineEnd)
            }
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)
            if (200 != connection.responseCode) {
                throw RuntimeException()
            }
            inputStream = connection.inputStream
            result = convertStreamToString(inputStream)
            inputStream.close()
            outputStream.flush()
            outputStream.close()
            return result
        } catch (e: Exception) {
            e.printStackTrace()
            //            logger.error(e);
//            throw new CustomException(e);
        }
        return null
    }

    fun setVariables(context: Context?, mLivenessRequest: LivenessRequest?) {
        this.context = context
        this.appId = mLivenessRequest?.appId
        privateKey = mLivenessRequest?.privateKey
        this.baseUrl = mLivenessRequest?.baseURL ?: baseUrl
        jwsUtils.setAppLicense(privateKey)
    }

    fun post(url: String, requestBody: JSONObject): String? {
        return doPost(appId, baseUrl + url, requestBody, genericSignature(requestBody, privateKey), null)
    }

    fun post(url: String, requestBody: JSONObject, optionalHeader: Map<String, String>?): String? {
        return doPost(appId, baseUrl + url, requestBody, genericSignature(requestBody, privateKey), optionalHeader)
    }

    /**
     * Do v3 post, will encrypt request body using JWS - JWE standard
     *
     * @param url
     * @param requestBody
     * @param optionalHeader
     * @return
     */
    @JvmOverloads
    fun postV3(url: String, requestBody: JSONObject, optionalHeader: Map<String, String>? = null): String? {
        return doPostV3(baseUrl + url, requestBody, optionalHeader)
    }

    fun initTransaction(mContext: Context): String? {
        var mDeviceId = AppPreferenceUtils(mContext).getDeviceId() ?: AppConfig.mLivenessRequest?.deviceId
        if (mDeviceId.isNullOrEmpty()) {
            mDeviceId = UUID.randomUUID().toString()
        }
        val request = JSONObject()
        request.put("deviceId", mDeviceId)
        request.put("period", AppConfig.mLivenessRequest?.duration)
        request.put("clientTransactionId", AppConfig.mLivenessRequest?.clientTransactionId)
        return instance?.postV3("/eid/v3/initTransaction", request)
    }

    fun checkLiveNessFlash(mContext: Context, totp: String, transactionId: String, imageLive: String, color: Int): String? {
        val request = JSONObject()
        request.put("totp", totp)
        request.put("transaction_id", transactionId)
        request.put("image_live", imageLive)
        request.put("color", color)
        val optionalHeader = HashMap<String, String>()
        optionalHeader.put("deviceid", AppPreferenceUtils(mContext).getDeviceId() ?: AppConfig.mLivenessRequest?.deviceId ?: "")
        return instance?.postV3("/eid/v3/verifyFace", request, optionalHeader)
    }

    fun registerFace(mContext: Context, faceImage: String): String? {
        var mDeviceId = AppPreferenceUtils(mContext).getDeviceId() ?: AppConfig.mLivenessRequest?.deviceId
        if (mDeviceId.isNullOrEmpty()) {
            mDeviceId = UUID.randomUUID().toString()
        }
        val request = JSONObject()
        request.put("deviceId", mDeviceId)
        request.put("face_image", faceImage)
        val optionalHeader = HashMap<String, String>()
        optionalHeader.put("deviceid", mDeviceId)
        return instance?.postV3("/eid/v3/registerFace", request, optionalHeader)
//        return instance?.post("/eid/v3/verifyFace", request, optionalHeader)
    }

    fun registerDeviceAndFace(mContext: Context, faceImage: String) {
        Thread {
            var mSecret = AppPreferenceUtils(mContext).getTOTPSecret(mContext) ?: AppConfig.mLivenessRequest?.secret
            if (mSecret.isNullOrEmpty() || mSecret.length != 16) {
                mSecret = AppUtils.getSecretValue()
            }
            var mDeviceId = AppPreferenceUtils(mContext).getDeviceId() ?: AppConfig.mLivenessRequest?.deviceId
            if (mDeviceId.isNullOrEmpty()) {
                mDeviceId = UUID.randomUUID().toString()
            }
            val request = JSONObject()
            request.put("deviceId", mDeviceId)
            request.put("deviceOs", "Android")
            request.put("deviceName", Build.MANUFACTURER + " " + Build.MODEL)
            request.put("period", AppConfig.mLivenessRequest?.duration)
            request.put("secret", mSecret)
            val responseDevice = instance?.postV3("/eid/v3/registerDevice", request)
            var result: JSONObject? = null
            if (responseDevice != null && responseDevice.length > 0) {
                result = JSONObject(responseDevice)
            }
            var statusDevice = -1
            if (result?.has("status") == true) {
                statusDevice = result.getInt("status")
            }
            var strMessageDevice = "Error"
            if (result?.has("message") == true) {
                strMessageDevice = result.getString("message")
            }
            if (statusDevice == 200) {
                val response = HttpClientUtils.instance?.registerFace(mContext, faceImage)
                var result: JSONObject? = null
                if (response?.isNotEmpty() == true) {
                    result = JSONObject(response)
                }
                AppUtils.showLog("--result: " + result?.toString())
                var status = -1
                if (result?.has("status") == true) {
                    status = result.getInt("status")
                }
                if (status == 200) {
//                    showLoading(false)
                    AppPreferenceUtils(mContext).setDeviceId(mDeviceId)

                    Handler(Looper.getMainLooper()).post {
                        AppConfig.livenessFaceListener?.onCallbackLiveness(LivenessModel(faceImage = faceImage))
                        AppPreferenceUtils(mContext).setRegisterFace(true)
                    }

                } else {
//                    showLoading(false)
//                    Toast.makeText(mContext, result?.getString("message") ?: "Error", Toast.LENGTH_LONG).show()
//                    showToast(result?.getString("message") ?: "Error")
                    var strMessage = "Error"
                    if (result?.has("message") == true) {
                        strMessage = result.getString("message")
                    }
                    Handler(Looper.getMainLooper()).post {
                        AppConfig.livenessFaceListener?.onCallbackLiveness(
                            LivenessModel(
                                status = status,
                                message = strMessage
                            )
                        )
                    }
                }
            } else {
                Handler(Looper.getMainLooper()).post {
                    AppConfig.livenessFaceListener?.onCallbackLiveness(LivenessModel(status = statusDevice, message = strMessageDevice))
                }
            }
        }.start()

    }

}