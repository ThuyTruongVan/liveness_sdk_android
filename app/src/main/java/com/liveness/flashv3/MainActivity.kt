package com.liveness.flashv3

import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.liveness.sdk.corev3.model.LivenessRequest
import com.liveness.sdk.corev3.utils.CallbackAPIListener
import java.io.ByteArrayOutputStream

/**
 * Created by Thuytv on 15/04/2024.
 */
class MainActivity : AppCompatActivity() {
    private var deviceId = ""
    private lateinit var btnRegisterFace: Button
    private lateinit var btnLiveNessFlash: Button
    private lateinit var imvImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_main_activity)
        btnRegisterFace = findViewById(R.id.btn_register_face)
        btnLiveNessFlash = findViewById(R.id.btn_live_ness_flash)
        imvImage = findViewById(R.id.imv_image)

//        val mDeviceId = LiveNessSDK.getDeviceId(this)
//        if (mDeviceId?.isNotEmpty() == true) {
//            deviceId = mDeviceId
//            btnRegisterFace.isEnabled = false
//        }
//        if(LiveNessSDK.isRegisterFace(this))

//        LiveNessSDK.setCallbackListener(object : CallbackLivenessListener {
//            override fun onCallbackLiveness(data: LivenessModel?) {
//                Log.d("Thuytv", "------transactionID: ${data?.transactionID}")
//
//                val decodeString = android.util.Base64.decode(data?.livenessImage ?: "", android.util.Base64.NO_PADDING)
//                val bitmap = BitmapFactory.decodeByteArray(decodeString, 0, decodeString.size)
//                imvImage.setImageBitmap(bitmap)
//                showDefaultDialog(this@MainActivity, data?.data?.toString())
//            }
//        })
        btnLiveNessFlash.setOnClickListener {
//            val overlay = LayoutInflater.from(this).inflate(R.layout.ui_custom_view, null)
//            LiveNessSDK.setCustomProgress(overlay)
//            LiveNessSDK.startLiveNess(this, getLivenessRequest(), supportFragmentManager, R.id.frame_view_main, null)
//            val transaction = supportFragmentManager.beginTransaction()
//            val fragment = MainFragment()
//            transaction.replace(R.id.frame_view_main, fragment)
//            transaction.addToBackStack(MainFragment::class.java.name)
//            transaction.commit()

            val transaction = supportFragmentManager.beginTransaction()
            val fragment = FaceFragment()
            fragment.setCallBack(object : CallbackAPIListener{
                override fun onCallbackResponse(data: String?) {
                    showDefaultDialog(this@MainActivity, data)
                }

            })
            val bundle = Bundle()
            bundle.putString("KEY_BUNDLE_SCREEN", "")
            fragment?.arguments = bundle
            transaction.replace(R.id.frame_view_main, fragment)
            transaction.addToBackStack(FaceFragment::class.java.name)
            transaction.commit()
//            LiveNessSDK.testRSA()

//            val encrypted_register_face = encryptAndEncode("/eid/v3/registerFace")
//            val encrypted_init_transaction = encryptAndEncode("/eid/v3/initTransaction")
//            val encrypted_register_device = encryptAndEncode("/eid/v3/registerDevice")
//            val encrypted_verify_face = encryptAndEncode("/eid/v3/verifyFace")
//            Log.d("Thuytv","-----encrypted_register_face: $encrypted_register_face")
//            Log.d("Thuytv","-----encrypted_init_transaction: $encrypted_init_transaction")
//            Log.d("Thuytv","-----encrypted_register_device: $encrypted_register_device")
//            Log.d("Thuytv","-----encrypted_verify_face: $encrypted_verify_face")
//            val decryptedRegisterFace = decodeAndDecrypt(encrypted_register_face!!)
//            val decryptedInitTransaction = decodeAndDecrypt(encrypted_init_transaction!!)
//            val decryptedRegisterDevice = decodeAndDecrypt(encrypted_register_device!!)
//            val decryptedVerifyFace = decodeAndDecrypt(encrypted_verify_face!!)
//            Log.d("Thuytv","-----decryptedRegisterFace: $decryptedRegisterFace")
//            Log.d("Thuytv","-----decryptedInitTransaction: $decryptedInitTransaction")
//            Log.d("Thuytv","-----decryptedRegisterDevice: $decryptedRegisterDevice")
//            Log.d("Thuytv","-----decryptedVerifyFace: $decryptedVerifyFace")

        }
        btnRegisterFace.setOnClickListener {
//            val overlay = LayoutInflater.from(this).inflate(R.layout.ui_register_face, null)
//            LiveNessSDK.setCustomView(overlay, overlay.findViewById(R.id.btn_capture_face))
//            val request = getLivenessRequest()
////            request.imageFace = getImage()
//            LiveNessSDK.registerFace(this, request, supportFragmentManager, R.id.frame_view_main, object : CallbackLivenessListener {
//                override fun onCallbackLiveness(data: LivenessModel?) {
//                    Log.d("Thuytv", "------faceImage: ${data?.message}")
//                    btnRegisterFace.isEnabled = false
//
//                    val decodeString = android.util.Base64.decode(data?.faceImage ?: "", android.util.Base64.NO_PADDING)
//
//                    val bitmap = BitmapFactory.decodeByteArray(decodeString, 0, decodeString.size)
//                    imvImage.setImageBitmap(bitmap)
//                }
//            })
            val transaction = supportFragmentManager.beginTransaction()
            val fragment = FaceFragment()
            val bundle = Bundle()
            bundle.putString("KEY_BUNDLE_SCREEN", "TYPE_SCREEN_REGISTER_FACE")
            fragment?.arguments = bundle
            transaction.replace(R.id.frame_view_main, fragment)
            transaction.addToBackStack(FaceFragment::class.java.name)
            transaction.commit()

        }

    }
    fun encryptAndEncode(data: String): String? {
        return Base64.encodeToString(data.toByteArray(Charsets.UTF_8), Base64.DEFAULT)
    }

    fun decodeAndDecrypt(data: String): String? {
        return String(Base64.decode(data, Base64.DEFAULT))
    }

    private fun getLivenessRequest(): LivenessRequest {
        val privateKey = "-----BEGIN PRIVATE KEY-----\n" +
                "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDChqQeJapdPhq6\n" +
                "oxRo2okcLdTLvAXXUCxUUaUeMjOHnzCBkpEuidqAYw/BbktH+aAhBE4ZlvptuP0M\n" +
                "iRRRFrd16ckjCFisWIwQsm0LwMPcreegsnzr8a00nsegqq3dFeNS4wxfXLEv7puu\n" +
                "UIz7h/8tPBtrTEl1r0adV4AwWfw+yIOX58wAVv3gNWMoSPVt3g27N+iSnHAXWQg3\n" +
                "EkZa9y13yiDPFbBVWsqWo3nIpy+5OkNaNNJ2tlKaxViCSYKo/bxBsEcdgu9l3LWL\n" +
                "Nw2y8OyDaVUk2j//L0re3Ic+tTqrFmpYCPvntfFlejLjk/TmV2Gh1Gq5K/ACyGWf\n" +
                "z36Wn1dbAgMBAAECggEAPOg7atUQSrGvXNDDCzGhzJjtD/2HNqt7tcr6kEHXsJc/\n" +
                "cNKbcp7nM1vRCxelqpuWb8ARVCHZt5E8ajhhjCI4v0q8F6P9X7k54eB8FDn/GZG5\n" +
                "/K7mPloliVxN1Rib56V6z/EXZqR3NjLXu/Ssr/UdCOTREP+J4LdIvOswz/Lc8Cr7\n" +
                "rq6EQ+GvQp1/mHuZjwe/8U4uj5pgKRtHwL1bV9Nj/tvGXqBzReEGlXPO6wXlQiDm\n" +
                "cfhlYQIp7HIujQqQ2bJHezKa4FwDgSUfFwvc0b/4UDqmKRcQcKlcI5f8C2z45rMt\n" +
                "SeWU3Wo4X0CwepH5K7Np4DInlJBLWmKf+W63KkZ/wQKBgQD5SxIV3pfKqeoQQfr8\n" +
                "Az5edTlxvIvBfWBe6JqotolCQ0zfvdnL6Sfc9m8Sb2m0ulmUuAgESUF5UxxG0zPp\n" +
                "A3ao4z9um+0TeqkOaokapYqVCt2g2fe5Ahn9mpXhaGW7NX1RGsyMG0ZKcMwzQnvT\n" +
                "/RUOsQIzbBtEPsABWuJL7E1JrwKBgQDHwmC0AYkRBm2hEnp2PWVPvy5Fwn3fKb9c\n" +
                "zkD4sx3T0pVXflXq7kqGvuoxqwUv15PTOeSniRVMeOm0k1MbPX3g/7Mj6/3Ox6nR\n" +
                "sUKcWf2FR2eYTOpkjyzlTs3J0wEY0F6psZKvCaDIq+iWZOGWnECOx1oNssSe24Mf\n" +
                "WmanOat0FQKBgQCzhOXfLc4tOTK+xmTQ+hz9tHjLeLVDft/ZOLO27svlFcXUEUk3\n" +
                "2AzGyAewRN7gMJm1yNitDXvCvKDON+VX1RCsCglxw4Nz1Y7MPNfCgpdLQOncnVoQ\n" +
                "tny07Y8lJSKtL5WwxBWshy+VB6lk+GlnfQae78Tf9ueju3RVBXeUqPP3LQKBgDRM\n" +
                "WZEHDeGdMzqGYfVu4YVdYjn1T4sLnQQpI615B4gbi+naM5hMRsq8VQgn5DXAVP1q\n" +
                "HMkYBLF+voD+STXKhZhQmZAACvUGJRm3NN9GWC96oE6pZHxrV6+5T/tU+OaMNxxq\n" +
                "VyLofGXNTBmD0+PgiK5Y1uTbNsr4YX3l5xZE7PR5AoGAY02BlFRX8+x+B8H2EEqu\n" +
                "pDEyFytnXQ25ZeBdkywuj95yX5jlkzFZCsvqVUVmRPglwkR5fR2dWtNprhRgezwL\n" +
                "9pOFz3bQ1DN3EBY3nO/85aro/mE/nsnnQw4Vj38dtgPRuX60slCxHcQAv9PUJYBz\n" +
                "Hci6AX3smA2ulUvchtH6uPQ=\n" +
                "-----END PRIVATE KEY-----"
        val public_key = "-----BEGIN CERTIFICATE-----\n" +
                "MIIDjzCCAnegAwIBAgIEPhgWFTANBgkqhkiG9w0BAQsFADBbMScwJQYDVQQDDB5SZWdlcnkgU2Vs\n" +
                "Zi1TaWduZWQgQ2VydGlmaWNhdGUxIzAhBgNVBAoMGlJlZ2VyeSwgaHR0cHM6Ly9yZWdlcnkuY29t\n" +
                "MQswCQYDVQQGEwJVQTAgFw0yNDA0MTEwMDAwMDBaGA8yMTI0MDQxMTAzMTMwOVowUTEdMBsGA1UE\n" +
                "AwwUcXVhbmd0cnVuZ3F0cy5jb20udm4xIzAhBgNVBAoMGlJlZ2VyeSwgaHR0cHM6Ly9yZWdlcnku\n" +
                "Y29tMQswCQYDVQQGEwJVQTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAIfK7LjzjqCo\n" +
                "VSzXz/ROHc2IyBMc89GwnR0slF1Lenavs+r+lnjFAxkVonBRTjtMj1pWqlACnd3qiIAD/8GbSagG\n" +
                "qsV43BDPbioDibWg/9wln82VLwEQohjLTl7VJtKuRAIUcg2nY4r5LNzpdClJx+k7zrIVDKSO8tRa\n" +
                "onU1dU6KLSmC2ZOzT10zrK4qmjvN/LFp0rlXJtdw++MUOIM9kccyi+3MK7iiraNV7Tlazy9xF0OZ\n" +
                "ytzgSX5R+oHE3aUS0M+W4p/dhihvLKjiejuw46E0dqEKxaqMJHXj2Qei1Ky1RrdRBNB0oQLCoUGx\n" +
                "KRaYw1CbZ7QWAgnrbqTvs1Y8pwUCAwEAAaNjMGEwDwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8BAf8E\n" +
                "BAMCAYYwHQYDVR0OBBYEFIlsqZHH0jmPvIjlF4YARXnamm7AMB8GA1UdIwQYMBaAFIlsqZHH0jmP\n" +
                "vIjlF4YARXnamm7AMA0GCSqGSIb3DQEBCwUAA4IBAQBfSk1XtHU8ix87g+lVzRQrEf7qsqWiwkN9\n" +
                "TW05qaPDMoMEoe/MW0YJZ+QwgvGMNLkEWjz/v0p1fVFF6kIolbo1o+1P6D4RCWvyB8S5zV9Mv+aR\n" +
                "1uWbAYiAA2uql/NrIJ3V1pJhIgRgDsRNuVP8MhNZc6DgJQLZOMKLwXsNHDtGOHk+ZcPiyWcjb4a3\n" +
                "voZCp4HN8+V2umO+QGuESZhTLihBnXv9HTpKxwWu4tK/4dgngDYM3UmChRjD/H7A3aYV4Xyxkqw2\n" +
                "rnd2LAr/zUEhFkbs21iG3DF0cHGKI15YzIq5pEhb9l4ePcCIgWgnJDNJPA/QhxpRB1XhP4bpK8kP\n" +
                "GJ8f\n" +
                "-----END CERTIFICATE-----"
        val appId = "com.qts.test"
//        if (deviceId.isNullOrEmpty()) {
//            deviceId = UUID.randomUUID().toString()
//        }
//        deviceId = "f8552f6d-35da-45f0-9761-f38fe1ea33d1"
        val optionHeader: HashMap<String, String> = HashMap()
        optionHeader["header1"] = "test"
        optionHeader["header2"] = "TEST-02"
        val optionRequest: HashMap<String, String> = HashMap()
        optionRequest["request-1"] = "test"
        optionRequest["request-2"] = "TEST-02"
        //ABCDEFGHIJKLMNOP
        return LivenessRequest(
            duration = 600, privateKey = privateKey, appId = appId,
            clientTransactionId = "TEST",
            baseURL = "https://face-matching.vietplus.eu", publicKey = public_key, ownerId = "123",
            optionHeader = optionHeader, optionRequest = optionRequest, isDebug = true
        )

    }

    private fun showDefaultDialog(context: Context, strContent: String?) {
        val alertDialog = AlertDialog.Builder(context)

        alertDialog.apply {
            //setIcon(R.drawable.ic_hello)
            setTitle("Response")
            setMessage(strContent)
            setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            }

        }.create().show()
    }

    private fun getImage(): String {
        val bitmap = BitmapFactory.decodeResource(this.getResources(), com.liveness.sdk.corev3.R.drawable.img_0)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
        val image = stream.toByteArray()
        return android.util.Base64.encodeToString(image, android.util.Base64.NO_PADDING)
    }
}