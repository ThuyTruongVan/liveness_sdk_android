package com.liveness.flash

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.mlkit.vision.face.Face
import com.liveness.flash.facedetector.FaceDetector
import com.liveness.flash.facedetector.Frame
import com.liveness.flash.facedetector.LensFacing
import com.liveness.sdk.core.LiveNessSDK
import com.liveness.sdk.core.R
import com.liveness.sdk.core.model.LivenessModel
import com.liveness.sdk.core.model.LivenessRequest
import com.liveness.sdk.core.utils.CallbackAPIListener
import com.otaliastudios.cameraview.CameraException
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraOptions
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Mode
import org.json.JSONObject
import java.util.Random
import java.util.UUID

/**
 * Created by Thuytv on 18/05/2024.
 */
class FaceFragment : Fragment() {
    private val REQUEST_PERMISSION_CODE = 1231

    //    private var pathVideo = ""
    private var bgColor = 0
    private var isCapture = false
    private var lstBgDefault: ArrayList<Int> = arrayListOf(R.drawable.img_0, R.drawable.img_1, R.drawable.img_2, R.drawable.img_3)

    private var isFirstVideo = true
    private var typeScreen = ""
    private lateinit var cameraViewVideo: CameraView
    private lateinit var btnCapture: Button
    private lateinit var prbLoading: ProgressBar
    private lateinit var bgFullScreenDefault: ImageView
    private lateinit var llVideo: RelativeLayout
    private var frameViewCustom: FrameLayout? = null
    private var mImgLiveNess: String = ""
    private var mFaceImage: String = ""
    private var mFragmentManager: FragmentManager? = null

    private var permissions = arrayOf(
        Manifest.permission.CAMERA
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.ui_main_live_ness, container, false)

        cameraViewVideo = view.findViewById(R.id.camera_view_video)
        btnCapture = view.findViewById(R.id.btn_capture)
        prbLoading = view.findViewById(R.id.prb_loading)
        bgFullScreenDefault = view.findViewById(R.id.bg_full_screen_default)
        llVideo = view.findViewById(R.id.ll_video)
        frameViewCustom = view.findViewById(R.id.frame_view_custom)

        typeScreen = arguments?.getString("KEY_BUNDLE_SCREEN") ?: ""

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permissions = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        // Set config for SDK
        LiveNessSDK.setConfigSDK(requireContext(), getLivenessRequest())

        initCamera(view)
        if (checkPermissions()) {
            cameraViewVideo.open()
        } else {
            requestPermissions()
        }
        if (typeScreen == "TYPE_SCREEN_REGISTER_FACE") {
            btnCapture.visibility = View.VISIBLE
            btnCapture.setOnClickListener {
                cameraViewVideo.takePictureSnapshot()
            }
        } else {
            btnCapture.visibility = View.GONE
        }
        return view
    }

    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            val resultCamera = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            val resultRead = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            val resultWrite = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
            (resultCamera == PackageManager.PERMISSION_GRANTED
                    && resultRead == PackageManager.PERMISSION_GRANTED
                    && resultWrite == PackageManager.PERMISSION_GRANTED)
        } else {
            val resultCamera = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            resultCamera == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(requireActivity(), permissions, REQUEST_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (checkPermissions()) {
//                Toast.makeText(requireContext(), "Permission granted", Toast.LENGTH_LONG).show()
                cameraViewVideo.open()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initCamera(view: View) {
        apply {
//            pathVideo = Environment.getExternalStorageDirectory().toString() + "/Download/" + "VideoLiveNess" + System.currentTimeMillis() + ".mp4"
            val lensFacing = Facing.FRONT
            setupCamera(lensFacing, view)
        }
    }

    private fun setupCamera(lensFacing: Facing, view: View) = apply {
        val faceDetector = FaceDetector(view.findViewById(R.id.faceBoundsOverlay))
        faceDetector.setonFaceDetectionFailureListener(object : FaceDetector.OnFaceDetectionResultListener {
            override fun onSuccess(faceBounds: Face) {
                super.onSuccess(faceBounds)
                if (!isCapture) {
                    isCapture = true
//                    cameraViewVideo.stopVideo()
                    bgFullScreenDefault.visibility = View.VISIBLE
                    llVideo.visibility = View.GONE
                    bgColor = Random().nextInt(3)
                    bgFullScreenDefault.background = ResourcesCompat.getDrawable(resources, lstBgDefault[bgColor], requireContext().theme)
                    Handler(Looper.myLooper()!!).postDelayed({
                        cameraViewVideo.takePictureSnapshot()
                    }, 100)
                }
            }

        })
        cameraViewVideo.facing = lensFacing
        cameraViewVideo.mode = Mode.PICTURE


        cameraViewVideo.addCameraListener(object : CameraListener() {
            override fun onCameraOpened(options: CameraOptions) {
                super.onCameraOpened(options)
                if (typeScreen != "TYPE_SCREEN_REGISTER_FACE") {
//                    cameraViewVideo.takeVideoSnapshot(File(pathVideo))
                }
            }

            override fun onCameraError(exception: CameraException) {
                super.onCameraError(exception)
            }

            override fun onCameraClosed() {
                super.onCameraClosed()
            }

            override fun onPictureTaken(result: PictureResult) {
                super.onPictureTaken(result)
                if (typeScreen != "TYPE_SCREEN_REGISTER_FACE") {
                    result.data.let {
                        mImgLiveNess = android.util.Base64.encodeToString(it, android.util.Base64.NO_PADDING)
                        callAPIGEtTOTP(mImgLiveNess, bgColor)
                    }
                    bgFullScreenDefault.visibility = View.GONE
                    llVideo.visibility = View.VISIBLE
                    isCapture = false
                } else {
                    result.data.let {
                        mFaceImage = android.util.Base64.encodeToString(it, android.util.Base64.NO_PADDING)
                        registerDevice(mFaceImage)
                    }
                }
            }

            override fun onVideoTaken(result: VideoResult) {
                super.onVideoTaken(result)
                result.file.let {
//                    AppConfig.livenessListener?.onCallbackLiveness(LivenessModel(pathVideo = it.absolutePath))
//                    finish()
                }
            }
        })
        if (typeScreen != "TYPE_SCREEN_REGISTER_FACE") {
            cameraViewVideo.addFrameProcessor {
                faceDetector.process(
                    Frame(
                        data = it.getData(),
                        rotation = it.rotationToUser,
                        size = Size(it.size.width, it.size.height),
                        format = it.format,
                        lensFacing = if (cameraViewVideo.facing == Facing.BACK) LensFacing.BACK else LensFacing.FRONT
                    )
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cameraViewVideo.open()
    }

    override fun onPause() {
        super.onPause()
        cameraViewVideo.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraViewVideo.destroy()
    }

    private fun initTransaction(imgLiveNess: String, bgColor: Int) {
        val response = LiveNessSDK.initTransaction(requireContext(), null, object : CallbackAPIListener {
            override fun onCallbackResponse(data: String?) {
                var result: JSONObject? = null
                if (!data.isNullOrEmpty()) {
                    result = JSONObject(data)
                }
                var status = -1
                if (result?.has("status") == true) {
                    status = result?.getInt("status") ?: -1
                }
                if (status == 200) {
                    val transactionId = result?.getString("data") ?: ""
                    checkLiveNessFlash(transactionId, imgLiveNess, bgColor)
                } else {
                    showLoading(false)
                    showToast("Error")
                    onBackFragment()
                }
            }
        })

    }

    private fun checkLiveNessFlash(transactionID: String, imgLiveNess: String, bgColor: Int) {
        LiveNessSDK.checkLiveNessFlash(requireContext(), transactionID, imgLiveNess, bgColor, null,
            object : CallbackAPIListener {
                override fun onCallbackResponse(data: String?) {
                    showToast(data ?: "")
                    showLoading(false)
                    onBackFragment()
                }
            })
    }

    private fun onBackFragment() {
        activity?.supportFragmentManager?.popBackStack()
    }

    fun callAPIGEtTOTP(imgLiveNess: String, bgColor: Int) {
        showLoading(true)
        initTransaction(imgLiveNess, bgColor)
    }

    private fun showToast(strToast: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), strToast, Toast.LENGTH_SHORT).show()
            showLoading(false)
        }
    }

    private fun showLoading(isShow: Boolean) {
        activity?.runOnUiThread {
            if (isShow) {
                prbLoading.visibility = View.VISIBLE
            } else {
                prbLoading.visibility = View.GONE

            }
        }

    }

    private fun registerDevice(faceImage: String) {
        showLoading(true)
        LiveNessSDK.registerDevice(requireContext(), null, object : CallbackAPIListener {
            override fun onCallbackResponse(data: String?) {
                var result: JSONObject? = null
                if (data?.isNotEmpty() == true) {
                    result = JSONObject(data)
                }
                var status = -1
                if (result?.has("status") == true) {
                    status = result.getInt("status")
                }
                if (status == 200) {
                    registerFace(faceImage)
                } else {
                    showToast("Register Device Fail")
                    onBackFragment()
                }
            }

        })
    }

    private fun registerFace(faceImage: String) {
//        showLoading(true)
        LiveNessSDK.registerFace(requireContext(), faceImage, null, object : CallbackAPIListener {
            override fun onCallbackResponse(data: String?) {
                showLoading(false)
                var result: JSONObject? = null
                if (data?.isNotEmpty() == true) {
                    result = JSONObject(data)
                }
                var status = -1
                if (result?.has("status") == true) {
                    status = result.getInt("status")
                }
                if (status == 200) {
                    showToast("Register Face Success")
                    onBackFragment()
                } else {
                    showToast("Register Face Fail")
                    onBackFragment()
                }
            }

        })

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
}