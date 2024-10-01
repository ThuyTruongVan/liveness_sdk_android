package com.liveness.flash

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.liveness.sdk.corev4.LiveNessSDK
import com.liveness.sdk.corev4.model.LivenessModel
import com.liveness.sdk.corev4.model.LivenessRequest
import com.liveness.sdk.corev4.utils.CallbackAPIListener
import com.liveness.sdk.corev4.utils.CallbackLivenessListener
import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * Created by Thuytv on 15/04/2024.
 */
class MainActivity : AppCompatActivity() {
    private var deviceId = ""
    private lateinit var btnRegisterFace: Button
    private lateinit var btnLiveNessFlash: Button
    private lateinit var btTestFlashNew: Button
    private lateinit var btTestFlashFragment: Button
    private lateinit var btRegisterFace: Button

    private lateinit var imvImage: ImageView
    private lateinit var ivTrans: ImageView
    private lateinit var ivRed: ImageView
    private lateinit var ivGreen: ImageView
    private lateinit var ivBlue: ImageView
    private lateinit var swOffline: Switch
    private lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_main_activity)
        btnRegisterFace = findViewById(R.id.btn_register_face)
        btnLiveNessFlash = findViewById(R.id.btn_live_ness_flash)
        btTestFlashNew = findViewById(R.id.btTestLiveFlashNew)
        btTestFlashFragment = findViewById(R.id.btTestLiveFlashFragment)
        btRegisterFace = findViewById(R.id.btRegisterFace)
        imvImage = findViewById(R.id.imv_image)
        ivTrans = findViewById(R.id.ivTrans)
        ivRed = findViewById(R.id.ivRed)
        ivGreen = findViewById(R.id.ivGreen)
        ivBlue = findViewById(R.id.ivBlue)
        swOffline = findViewById(R.id.swOffline)
        tvResult = findViewById(R.id.tvResult)
//        LiveNessSDK.setConfigSDK(this, getLivenessRequest())

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

        btRegisterFace.setOnClickListener {
            LiveNessSDK.registerFace(this, getLivenessRequest(),object : CallbackLivenessListener {
                override fun onCallbackLiveness(data: LivenessModel?) {
//                        Log.d("AKKKKK", "-----data: $data")
                    if (data?.status == 200) {
                        data.faceImage?.apply {
                            val img = base64ToBitmap(this)
                            if (img != null) {
                                ivTrans.setImageBitmap(img)
                            }
                        }
                        data.faceImage = null
                        tvResult.text = "$data"
                    }
                }
            } )
        }

        btTestFlashNew.setOnClickListener {
            LiveNessSDK.startLiveNess(
                this,
                getLivenessRequest(),
                object : CallbackLivenessListener {
                    override fun onCallbackLiveness(data: LivenessModel?) {
//                        Log.d("AKKKKK", "-----data: $data")
                        if (data?.status == 200) {
                            data.livenessImage?.apply {
                                val img = base64ToBitmap(this)
                                if (img != null) {
                                    ivTrans.setImageBitmap(img)
                                }
                            }
                            data.livenessImage = null
                            tvResult.text = "$data"
                        } else {
                            data?.imgTransparent?.apply {
                                val img = base64ToBitmap(this)
                                if (img != null) {
                                    ivTrans.setImageBitmap(img)
                                }
                            }
                            data?.imgRed?.apply {
                                val img = base64ToBitmap(this)
                                if (img != null) {
                                    ivRed.setImageBitmap(img)
                                }
                            }
                            data?.imgGreen?.apply {
                                val img = base64ToBitmap(this)
                                if (img != null) {
                                    ivGreen.setImageBitmap(img)
                                }
                            }
                            data?.imgBlue?.apply {
                                val img = base64ToBitmap(this)
                                if (img != null) {
                                    ivBlue.setImageBitmap(img)
                                }
                            }
                        }
                    }
                })
        }

        btTestFlashFragment.setOnClickListener {
            startActivity(Intent(this, CustomActivity::class.java))
        }
        btnLiveNessFlash.setOnClickListener {
            val transaction = supportFragmentManager.beginTransaction()
            val fragment = FaceFragment()
            fragment.setCallBack(object : CallbackAPIListener {
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
//        val privateKey =
//            "-----BEGIN PRIVATE KEY-----\n" + "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDChqQeJapdPhq6\n" + "oxRo2okcLdTLvAXXUCxUUaUeMjOHnzCBkpEuidqAYw/BbktH+aAhBE4ZlvptuP0M\n" + "iRRRFrd16ckjCFisWIwQsm0LwMPcreegsnzr8a00nsegqq3dFeNS4wxfXLEv7puu\n" + "UIz7h/8tPBtrTEl1r0adV4AwWfw+yIOX58wAVv3gNWMoSPVt3g27N+iSnHAXWQg3\n" + "EkZa9y13yiDPFbBVWsqWo3nIpy+5OkNaNNJ2tlKaxViCSYKo/bxBsEcdgu9l3LWL\n" + "Nw2y8OyDaVUk2j//L0re3Ic+tTqrFmpYCPvntfFlejLjk/TmV2Gh1Gq5K/ACyGWf\n" + "z36Wn1dbAgMBAAECggEAPOg7atUQSrGvXNDDCzGhzJjtD/2HNqt7tcr6kEHXsJc/\n" + "cNKbcp7nM1vRCxelqpuWb8ARVCHZt5E8ajhhjCI4v0q8F6P9X7k54eB8FDn/GZG5\n" + "/K7mPloliVxN1Rib56V6z/EXZqR3NjLXu/Ssr/UdCOTREP+J4LdIvOswz/Lc8Cr7\n" + "rq6EQ+GvQp1/mHuZjwe/8U4uj5pgKRtHwL1bV9Nj/tvGXqBzReEGlXPO6wXlQiDm\n" + "cfhlYQIp7HIujQqQ2bJHezKa4FwDgSUfFwvc0b/4UDqmKRcQcKlcI5f8C2z45rMt\n" + "SeWU3Wo4X0CwepH5K7Np4DInlJBLWmKf+W63KkZ/wQKBgQD5SxIV3pfKqeoQQfr8\n" + "Az5edTlxvIvBfWBe6JqotolCQ0zfvdnL6Sfc9m8Sb2m0ulmUuAgESUF5UxxG0zPp\n" + "A3ao4z9um+0TeqkOaokapYqVCt2g2fe5Ahn9mpXhaGW7NX1RGsyMG0ZKcMwzQnvT\n" + "/RUOsQIzbBtEPsABWuJL7E1JrwKBgQDHwmC0AYkRBm2hEnp2PWVPvy5Fwn3fKb9c\n" + "zkD4sx3T0pVXflXq7kqGvuoxqwUv15PTOeSniRVMeOm0k1MbPX3g/7Mj6/3Ox6nR\n" + "sUKcWf2FR2eYTOpkjyzlTs3J0wEY0F6psZKvCaDIq+iWZOGWnECOx1oNssSe24Mf\n" + "WmanOat0FQKBgQCzhOXfLc4tOTK+xmTQ+hz9tHjLeLVDft/ZOLO27svlFcXUEUk3\n" + "2AzGyAewRN7gMJm1yNitDXvCvKDON+VX1RCsCglxw4Nz1Y7MPNfCgpdLQOncnVoQ\n" + "tny07Y8lJSKtL5WwxBWshy+VB6lk+GlnfQae78Tf9ueju3RVBXeUqPP3LQKBgDRM\n" + "WZEHDeGdMzqGYfVu4YVdYjn1T4sLnQQpI615B4gbi+naM5hMRsq8VQgn5DXAVP1q\n" + "HMkYBLF+voD+STXKhZhQmZAACvUGJRm3NN9GWC96oE6pZHxrV6+5T/tU+OaMNxxq\n" + "VyLofGXNTBmD0+PgiK5Y1uTbNsr4YX3l5xZE7PR5AoGAY02BlFRX8+x+B8H2EEqu\n" + "pDEyFytnXQ25ZeBdkywuj95yX5jlkzFZCsvqVUVmRPglwkR5fR2dWtNprhRgezwL\n" + "9pOFz3bQ1DN3EBY3nO/85aro/mE/nsnnQw4Vj38dtgPRuX60slCxHcQAv9PUJYBz\n" + "Hci6AX3smA2ulUvchtH6uPQ=\n" + "-----END PRIVATE KEY-----"
//        val public_key =
//            "-----BEGIN CERTIFICATE-----\n" + "MIIDjzCCAnegAwIBAgIEPhgWFTANBgkqhkiG9w0BAQsFADBbMScwJQYDVQQDDB5SZWdlcnkgU2Vs\n" + "Zi1TaWduZWQgQ2VydGlmaWNhdGUxIzAhBgNVBAoMGlJlZ2VyeSwgaHR0cHM6Ly9yZWdlcnkuY29t\n" + "MQswCQYDVQQGEwJVQTAgFw0yNDA0MTEwMDAwMDBaGA8yMTI0MDQxMTAzMTMwOVowUTEdMBsGA1UE\n" + "AwwUcXVhbmd0cnVuZ3F0cy5jb20udm4xIzAhBgNVBAoMGlJlZ2VyeSwgaHR0cHM6Ly9yZWdlcnku\n" + "Y29tMQswCQYDVQQGEwJVQTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAIfK7LjzjqCo\n" + "VSzXz/ROHc2IyBMc89GwnR0slF1Lenavs+r+lnjFAxkVonBRTjtMj1pWqlACnd3qiIAD/8GbSagG\n" + "qsV43BDPbioDibWg/9wln82VLwEQohjLTl7VJtKuRAIUcg2nY4r5LNzpdClJx+k7zrIVDKSO8tRa\n" + "onU1dU6KLSmC2ZOzT10zrK4qmjvN/LFp0rlXJtdw++MUOIM9kccyi+3MK7iiraNV7Tlazy9xF0OZ\n" + "ytzgSX5R+oHE3aUS0M+W4p/dhihvLKjiejuw46E0dqEKxaqMJHXj2Qei1Ky1RrdRBNB0oQLCoUGx\n" + "KRaYw1CbZ7QWAgnrbqTvs1Y8pwUCAwEAAaNjMGEwDwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8BAf8E\n" + "BAMCAYYwHQYDVR0OBBYEFIlsqZHH0jmPvIjlF4YARXnamm7AMB8GA1UdIwQYMBaAFIlsqZHH0jmP\n" + "vIjlF4YARXnamm7AMA0GCSqGSIb3DQEBCwUAA4IBAQBfSk1XtHU8ix87g+lVzRQrEf7qsqWiwkN9\n" + "TW05qaPDMoMEoe/MW0YJZ+QwgvGMNLkEWjz/v0p1fVFF6kIolbo1o+1P6D4RCWvyB8S5zV9Mv+aR\n" + "1uWbAYiAA2uql/NrIJ3V1pJhIgRgDsRNuVP8MhNZc6DgJQLZOMKLwXsNHDtGOHk+ZcPiyWcjb4a3\n" + "voZCp4HN8+V2umO+QGuESZhTLihBnXv9HTpKxwWu4tK/4dgngDYM3UmChRjD/H7A3aYV4Xyxkqw2\n" + "rnd2LAr/zUEhFkbs21iG3DF0cHGKI15YzIq5pEhb9l4ePcCIgWgnJDNJPA/QhxpRB1XhP4bpK8kP\n" + "GJ8f\n" + "-----END CERTIFICATE-----"
        val public_key =
            "-----BEGIN CERTIFICATE-----\n" + "MIIE8jCCA9qgAwIBAgIQVAESDxKv/JtHV15tvtt1UjANBgkqhkiG9w0BAQsFADAr\n" + "MQ0wCwYDVQQDDARJLUNBMQ0wCwYDVQQKDARJLUNBMQswCQYDVQQGEwJWTjAeFw0y\n" + "MzA2MDcwNjU1MDNaFw0yNjA2MDkwNjU1MDNaMIHlMQswCQYDVQQGEwJWTjESMBAG\n" + "A1UECAwJSMOgIE7hu5lpMRowGAYDVQQHDBFRdeG6rW4gSG/DoG5nIE1haTFCMEAG\n" + "A1UECgw5Q8OUTkcgVFkgQ1AgROG7ikNIIFbhu6QgVsOAIEPDlE5HIE5HSOG7hiBT\n" + "4buQIFFVQU5HIFRSVU5HMUIwQAYDVQQDDDlDw5RORyBUWSBDUCBE4buKQ0ggVuG7\n" + "pCBWw4AgQ8OUTkcgTkdI4buGIFPhu5AgUVVBTkcgVFJVTkcxHjAcBgoJkiaJk/Is\n" + "ZAEBDA5NU1Q6MDExMDE4ODA2NTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoC\n" + "ggEBAJO6JDU+kNEUIiO6m75LOfgHkwGExYFv0tILHInS9CkK2k0FjmvU8VYJ0cQA\n" + "sGGabpHIwfh07llLfK3TUZlhnlFZYRrYvuexlLWQydjHYPqT+1c3iYaiXXcOqEjm\n" + "OupCj71m93ThFrYzzI2Zx07jccRptAAZrWMjI+30vJN7SDxhYsD1uQxYhUkx7psq\n" + "MqD4/nOyaWzZHLU94kTAw5lhAlVOMu3/6pXhIltX/097Wji1eyYqHFu8w7q3B5yW\n" + "gJYugEZfplaeLLtcTxok4VbQCb3cXTOSFiQYJ3nShlBd89AHxaVE+eqJaMuGj9z9\n" + "rdIoGr9LHU/P6KF+/SLwxpsYgnkCAwEAAaOCAVUwggFRMAwGA1UdEwEB/wQCMAAw\n" + "HwYDVR0jBBgwFoAUyCcJbMLE30fqGfJ3KXtnXEOxKSswgZUGCCsGAQUFBwEBBIGI\n" + "MIGFMDIGCCsGAQUFBzAChiZodHRwczovL3Jvb3RjYS5nb3Yudm4vY3J0L3ZucmNh\n" + "MjU2LnA3YjAuBggrBgEFBQcwAoYiaHR0cHM6Ly9yb290Y2EuZ292LnZuL2NydC9J\n" + "LUNBLnA3YjAfBggrBgEFBQcwAYYTaHR0cDovL29jc3AuaS1jYS52bjA0BgNVHSUE\n" + "LTArBggrBgEFBQcDAgYIKwYBBQUHAwQGCisGAQQBgjcKAwwGCSqGSIb3LwEBBTAj\n" + "BgNVHR8EHDAaMBigFqAUhhJodHRwOi8vY3JsLmktY2Eudm4wHQYDVR0OBBYEFE6G\n" + "FFM4HXne9mnFBZInWzSBkYNLMA4GA1UdDwEB/wQEAwIE8DANBgkqhkiG9w0BAQsF\n" + "AAOCAQEAH5ifoJzc8eZegzMPlXswoECq6PF3kLp70E7SlxaO6RJSP5Y324ftXnSW\n" + "0RlfeSr/A20Y79WDbA7Y3AslehM4kbMr77wd3zIij5VQ1sdCbOvcZXyeO0TJsqmQ\n" + "b46tVnayvpJYW1wbui6smCrTlNZu+c1lLQnVsSrAER76krZXaOZhiHD45csmN4dk\n" + "Y0T848QTx6QN0rubEW36Mk6/npaGU6qw6yF7UMvQO7mPeqdufVX9duUJav+WBJ/I\n" + "Y/EdqKp20cAT9vgNap7Bfgv5XN9PrE+Yt0C1BkxXnfJHA7L9hcoYrknsae/Fa2IP\n" + "99RyIXaHLJyzSTKLRUhEVqrycM0UXg==\n" + "-----END CERTIFICATE-----"
        val privateKey =
            "-----BEGIN PRIVATE KEY-----\n" + "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCiOMdedNfAhAdI\n" + "M1YmUd2hheu2vDMmFHjCfWHon8wv0doubYPY6/uhMcUERpPiFddWqe+Dfr/XwCsa\n" + "EaPOa27ghyUQ8HjdzAxcZ1yTWrgWttGruHlrHoXDPaov3QqvJTUrBclsH8p3ufPp\n" + "gmBC0HrFD0Pl4+vEpki4VvCDJFEGuBaSAqFe7JqUuaOVRG9mBBZWslkNi8XNkAQT\n" + "/Es+zReMf4EXIO2+wMo3aPIhe+sSZ3e3VqFL/10EJqNhurOT5ijUwReMlNb9wcxu\n" + "drfSKjLOgW1n+ZLjo16GdS2ye68B7ZaA0J3DPuDdRXJ5YuoW4UQd8o6CyezIHWpP\n" + "vH1tWFABAgMBAAECggEAB485yy8Kts/wPu8Vfqel+lbxSwyuHYIqtnV9UIfRzhCr\n" + "aCp2UG9+xF47Xh2j2o9F/6XfoXMQoY808vwLdB0Rh6kEkyuBlmRh1xSB/ePmXDic\n" + "wLHSBqnfdd+zxJM6YjsLpTuZzU4V80pZEXKf5b0tW22Arn/Whs1w6hYzEwloNTXf\n" + "4K974i+st1E5/0JjufTBTOTlBtwbphwN9ia/Xs2EY3D6kuJhYZ5lCWDocD21xYWd\n" + "NPM2CWqVXjJYEaqDTIWGwNGb2hkwNG5t/9MnN2On6BR7kgOWU4XxXHoLD3XoErwB\n" + "M3J8QAXGZwb+wRtkzRCVgojA6AQXfu9/QyPjyHW4oQKBgQDYMEC+LuNtjrNju8yF\n" + "LHMFbYbSfBQITE+kJn7iemezkwJw25NuKWl0pcxPe+NtpaHNFDmHnTVrlICTh90c\n" + "qrtge1vsqtgEoaZfdYqkUVvl1jJWBJ+VqQNO2Nxos/6fM0ARDC/9YXHoDWKC4WeS\n" + "PvYJ55MkMHseddpKIUGrZ1xO5QKBgQDAGGFxC9xWhG/CEm/JAFul+uyp9ncG6ro/\n" + "47Tw75M5+2K9wsP2R2c0uoXZtQHFvvi9CADaQkSYrzY3wCqgjDhsR+3psN1R+Pkw\n" + "bgMf3Rt6bMrYemPaGOe9qZ+Dpw/2GnLZfmCcJfKoRfY73YsxlL4/0Zf1va/qZnbp\n" + "pGh4IlvO7QKBgD87teQq0Mi9wYi9aG/XdXkz9Qhh1HYs4+qOe/SAew6SRFeAUhoZ\n" + "sMe2qxDgmr/6f139uWoKOJLT59u/FJSK962bx2JtAiwwn/ox5jBzv551TVnNlmPv\n" + "AJGyap2RcDtegTG7T9ocA3YtXBAOH/4tvkddXbNrHsflDsk5+vxIij5lAoGAFli/\n" + "vS7sCwSNG76ZUoDAKKbwMTWC00MrN5N90SmNrwkXi4vE0DmuP+wS9iigdCirNxJf\n" + "RwS+hiSb4hBw5Qxq4+3aN31jwc18761cn7BRKgTN9DEIvK55Bw9chyxAJxkck0Co\n" + "bIHdoMXCx2QWdUYge7weOXA/rr0MyFFf9dnJZGECgYEAuhJrRoxLdyouTd6X9+R1\n" + "8FWY0XGfsBp+PkN/nnPuK6IJR/IeI+cdiorfm45l4ByF0XEBCDz2xXQ6MVBNz3zF\n" + "MjEQ61dTFRfiTW2ZDqhMTtZH4R4T5NLWf+3ItjkAkOdStszplhHy0bUQIYgptYXd\n" + "5Sw/UvMv83CmlztVC5tGG9o=\n" + "-----END PRIVATE KEY-----"
//        val appId = "com.qts.test"
        val appId = "com.pvcb"
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
//        val transactionId="TEST"
        val transactionId = "51219b1d-fc4e-4005-a988-4183e76fcd97"
//        val transactionId = ""
        //ABCDEFGHIJKLMNOP
        return LivenessRequest(
            duration = 600,
            privateKey = privateKey,
            appId = appId,
            clientTransactionId = transactionId,
//            baseURL = "https://face-matching.vietplus.eu",
            baseURL = "https://ekyc-sandbox.eidas.vn/face-matching",
            publicKey = public_key,
            ownerId = "123",
            optionHeader = optionHeader,
            optionRequest = optionRequest,
            isDebug = true,
            offlineMode = swOffline.isChecked
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

    fun base64ToBitmap(b64Data: String?): Bitmap? {
        return try {
            val decodedString = Base64.decode(b64Data, Base64.DEFAULT)
            val inputStream: InputStream = ByteArrayInputStream(decodedString)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Error) {
            e.printStackTrace()
            null
        }
    }

}