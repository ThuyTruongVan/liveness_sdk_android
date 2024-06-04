package com.liveness.flash

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import java.io.ByteArrayOutputStream
import java.util.Random

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
        val view: View = inflater.inflate(com.liveness.flash.R.layout.ui_main_live_ness_demo, container, false)

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
        val optionRequest = HashMap<String, String>()
        optionRequest["face_image"] = getImage()
        val request = LivenessRequest()
        request.optionRequest = optionRequest
        LiveNessSDK.checkLiveNessFlash(requireContext(), transactionID, imgLiveNess, bgColor, request,
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
                "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCiOMdedNfAhAdI\n" +
                "M1YmUd2hheu2vDMmFHjCfWHon8wv0doubYPY6/uhMcUERpPiFddWqe+Dfr/XwCsa\n" +
                "EaPOa27ghyUQ8HjdzAxcZ1yTWrgWttGruHlrHoXDPaov3QqvJTUrBclsH8p3ufPp\n" +
                "gmBC0HrFD0Pl4+vEpki4VvCDJFEGuBaSAqFe7JqUuaOVRG9mBBZWslkNi8XNkAQT\n" +
                "/Es+zReMf4EXIO2+wMo3aPIhe+sSZ3e3VqFL/10EJqNhurOT5ijUwReMlNb9wcxu\n" +
                "drfSKjLOgW1n+ZLjo16GdS2ye68B7ZaA0J3DPuDdRXJ5YuoW4UQd8o6CyezIHWpP\n" +
                "vH1tWFABAgMBAAECggEAB485yy8Kts/wPu8Vfqel+lbxSwyuHYIqtnV9UIfRzhCr\n" +
                "aCp2UG9+xF47Xh2j2o9F/6XfoXMQoY808vwLdB0Rh6kEkyuBlmRh1xSB/ePmXDic\n" +
                "wLHSBqnfdd+zxJM6YjsLpTuZzU4V80pZEXKf5b0tW22Arn/Whs1w6hYzEwloNTXf\n" +
                "4K974i+st1E5/0JjufTBTOTlBtwbphwN9ia/Xs2EY3D6kuJhYZ5lCWDocD21xYWd\n" +
                "NPM2CWqVXjJYEaqDTIWGwNGb2hkwNG5t/9MnN2On6BR7kgOWU4XxXHoLD3XoErwB\n" +
                "M3J8QAXGZwb+wRtkzRCVgojA6AQXfu9/QyPjyHW4oQKBgQDYMEC+LuNtjrNju8yF\n" +
                "LHMFbYbSfBQITE+kJn7iemezkwJw25NuKWl0pcxPe+NtpaHNFDmHnTVrlICTh90c\n" +
                "qrtge1vsqtgEoaZfdYqkUVvl1jJWBJ+VqQNO2Nxos/6fM0ARDC/9YXHoDWKC4WeS\n" +
                "PvYJ55MkMHseddpKIUGrZ1xO5QKBgQDAGGFxC9xWhG/CEm/JAFul+uyp9ncG6ro/\n" +
                "47Tw75M5+2K9wsP2R2c0uoXZtQHFvvi9CADaQkSYrzY3wCqgjDhsR+3psN1R+Pkw\n" +
                "bgMf3Rt6bMrYemPaGOe9qZ+Dpw/2GnLZfmCcJfKoRfY73YsxlL4/0Zf1va/qZnbp\n" +
                "pGh4IlvO7QKBgD87teQq0Mi9wYi9aG/XdXkz9Qhh1HYs4+qOe/SAew6SRFeAUhoZ\n" +
                "sMe2qxDgmr/6f139uWoKOJLT59u/FJSK962bx2JtAiwwn/ox5jBzv551TVnNlmPv\n" +
                "AJGyap2RcDtegTG7T9ocA3YtXBAOH/4tvkddXbNrHsflDsk5+vxIij5lAoGAFli/\n" +
                "vS7sCwSNG76ZUoDAKKbwMTWC00MrN5N90SmNrwkXi4vE0DmuP+wS9iigdCirNxJf\n" +
                "RwS+hiSb4hBw5Qxq4+3aN31jwc18761cn7BRKgTN9DEIvK55Bw9chyxAJxkck0Co\n" +
                "bIHdoMXCx2QWdUYge7weOXA/rr0MyFFf9dnJZGECgYEAuhJrRoxLdyouTd6X9+R1\n" +
                "8FWY0XGfsBp+PkN/nnPuK6IJR/IeI+cdiorfm45l4ByF0XEBCDz2xXQ6MVBNz3zF\n" +
                "MjEQ61dTFRfiTW2ZDqhMTtZH4R4T5NLWf+3ItjkAkOdStszplhHy0bUQIYgptYXd\n" +
                "5Sw/UvMv83CmlztVC5tGG9o=\n" +
                "-----END PRIVATE KEY-----"
        val public_key = "-----BEGIN CERTIFICATE-----\n" +
                "MIIE8jCCA9qgAwIBAgIQVAESDxKv/JtHV15tvtt1UjANBgkqhkiG9w0BAQsFADAr\n" +
                "MQ0wCwYDVQQDDARJLUNBMQ0wCwYDVQQKDARJLUNBMQswCQYDVQQGEwJWTjAeFw0y\n" +
                "MzA2MDcwNjU1MDNaFw0yNjA2MDkwNjU1MDNaMIHlMQswCQYDVQQGEwJWTjESMBAG\n" +
                "A1UECAwJSMOgIE7hu5lpMRowGAYDVQQHDBFRdeG6rW4gSG/DoG5nIE1haTFCMEAG\n" +
                "A1UECgw5Q8OUTkcgVFkgQ1AgROG7ikNIIFbhu6QgVsOAIEPDlE5HIE5HSOG7hiBT\n" +
                "4buQIFFVQU5HIFRSVU5HMUIwQAYDVQQDDDlDw5RORyBUWSBDUCBE4buKQ0ggVuG7\n" +
                "pCBWw4AgQ8OUTkcgTkdI4buGIFPhu5AgUVVBTkcgVFJVTkcxHjAcBgoJkiaJk/Is\n" +
                "ZAEBDA5NU1Q6MDExMDE4ODA2NTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoC\n" +
                "ggEBAJO6JDU+kNEUIiO6m75LOfgHkwGExYFv0tILHInS9CkK2k0FjmvU8VYJ0cQA\n" +
                "sGGabpHIwfh07llLfK3TUZlhnlFZYRrYvuexlLWQydjHYPqT+1c3iYaiXXcOqEjm\n" +
                "OupCj71m93ThFrYzzI2Zx07jccRptAAZrWMjI+30vJN7SDxhYsD1uQxYhUkx7psq\n" +
                "MqD4/nOyaWzZHLU94kTAw5lhAlVOMu3/6pXhIltX/097Wji1eyYqHFu8w7q3B5yW\n" +
                "gJYugEZfplaeLLtcTxok4VbQCb3cXTOSFiQYJ3nShlBd89AHxaVE+eqJaMuGj9z9\n" +
                "rdIoGr9LHU/P6KF+/SLwxpsYgnkCAwEAAaOCAVUwggFRMAwGA1UdEwEB/wQCMAAw\n" +
                "HwYDVR0jBBgwFoAUyCcJbMLE30fqGfJ3KXtnXEOxKSswgZUGCCsGAQUFBwEBBIGI\n" +
                "MIGFMDIGCCsGAQUFBzAChiZodHRwczovL3Jvb3RjYS5nb3Yudm4vY3J0L3ZucmNh\n" +
                "MjU2LnA3YjAuBggrBgEFBQcwAoYiaHR0cHM6Ly9yb290Y2EuZ292LnZuL2NydC9J\n" +
                "LUNBLnA3YjAfBggrBgEFBQcwAYYTaHR0cDovL29jc3AuaS1jYS52bjA0BgNVHSUE\n" +
                "LTArBggrBgEFBQcDAgYIKwYBBQUHAwQGCisGAQQBgjcKAwwGCSqGSIb3LwEBBTAj\n" +
                "BgNVHR8EHDAaMBigFqAUhhJodHRwOi8vY3JsLmktY2Eudm4wHQYDVR0OBBYEFE6G\n" +
                "FFM4HXne9mnFBZInWzSBkYNLMA4GA1UdDwEB/wQEAwIE8DANBgkqhkiG9w0BAQsF\n" +
                "AAOCAQEAH5ifoJzc8eZegzMPlXswoECq6PF3kLp70E7SlxaO6RJSP5Y324ftXnSW\n" +
                "0RlfeSr/A20Y79WDbA7Y3AslehM4kbMr77wd3zIij5VQ1sdCbOvcZXyeO0TJsqmQ\n" +
                "b46tVnayvpJYW1wbui6smCrTlNZu+c1lLQnVsSrAER76krZXaOZhiHD45csmN4dk\n" +
                "Y0T848QTx6QN0rubEW36Mk6/npaGU6qw6yF7UMvQO7mPeqdufVX9duUJav+WBJ/I\n" +
                "Y/EdqKp20cAT9vgNap7Bfgv5XN9PrE+Yt0C1BkxXnfJHA7L9hcoYrknsae/Fa2IP\n" +
                "99RyIXaHLJyzSTKLRUhEVqrycM0UXg==\n" +
                "-----END CERTIFICATE-----"
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
        //ABCDEFGHIJKLMNOP
        return LivenessRequest(
            duration = 600, privateKey = privateKey, appId = appId,
            clientTransactionId = "TEST",
            baseURL = "https://ekyc-sandbox.eidas.vn/face-matching", publicKey = public_key, ownerId = "123",
            optionHeader = optionHeader, optionRequest = optionRequest, isDebug = true
        )

    }
    private fun getImage(): String {
        val bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.img_0)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
        val image = stream.toByteArray()
        return android.util.Base64.encodeToString(image, android.util.Base64.NO_PADDING)
    }
}