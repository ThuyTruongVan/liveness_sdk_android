package com.liveness.flashv3

import android.Manifest
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.mlkit.vision.face.Face
import com.liveness.flashv3.facedetector.FaceDetector
import com.liveness.flashv3.facedetector.Frame
import com.liveness.flashv3.facedetector.LensFacing
import com.liveness.sdk.corev3.LiveNessSDK
import com.liveness.sdk.corev3.model.LivenessRequest
import com.liveness.sdk.corev3.utils.CallbackAPIListener
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
    private var mCallbackAPIListener: CallbackAPIListener? = null

    private var permissions = arrayOf(
        Manifest.permission.CAMERA
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.ui_main_live_ness_demo, container, false)

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
//                cameraViewVideo.takePictureSnapshot()
                registerFace()
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

//        LiveNessSDK.checkLiveNess(requireContext(), transactionID, imgLiveNess, bgColor, null,
//            object : CallbackAPIListener {
//                override fun onCallbackResponse(data: String?) {
//                    Log.d("Thuytv","------checkLiveNess: $data" )
//                    showToast(data ?: "")
//                    showLoading(false)
//                    onBackFragment()
//                }
//            })
    }

    private fun checkLiveNess(imgLiveNess: String, bgColor: Int) {
        LiveNessSDK.checkLiveNess(requireContext(), imgLiveNess, bgColor, null,
            object : CallbackAPIListener {
                override fun onCallbackResponse(data: String?) {
                    Log.d("Thuytv", "------checkLiveNess: $data")
                    mCallbackAPIListener?.onCallbackResponse(data)
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
//        initTransaction(imgLiveNess, bgColor)
        checkLiveNess(imgLiveNess, bgColor)
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
        val testImage = "/H8zXPeFvEN94Z1xLmA7W5SSNxww9CPqB+Ve8Rx7k5rxvxtYwweKp1iAG5g7f7x5NddGo2+VnHWo\n" +
                "pe9E9903WdP1WNWs7uGRigcxq4LoD/eAOR1q9mvmWWWSMJcwMwmiOeD1Fd/4b+J1zEBHqam8iOMS\n" +
                "KVEijvx0b9K7XBo5FJHrlJWNpvirRtVRPIvokkc4EUrBXz6YPX8K2cg9DUMoWikopAFFFFABRRSU\n" +
                "wK1LSUEgDJ6CgB1A64rm9R8caJYK6rcfaZkOPLiU/wDoXT9a5DUPijfEn7NaW9upHVyXI/Hgfoap\n" +
                "RbJbSPUpJI4Y2kldURRksxwAPc1yWteP9P0/MViBeT85IOEX8cfN+H515fe+ItV1PK3V5K8Z5w7H\n" +
                "af8AgOcVmPtJy7O4/uluPy/CrVPuS5m/qnjbWtRZg2oPDHk4jhbZx6ccn8a50M7H5cjPVqd8h5CK\n" +
                "PoKN1aqKRO5IMIuF4/GkLdMfT86QnIpuc5FMCNuuMfWtrwlqi6X4ht5ZHKxElX54wRjn9D+FYvf6\n" +
                "0zO1w3cVM480Wioy5Xc901AZWsJ+HzirGiamNZ0OKbJMsYEcmf7wA5/HOaiuF2ua8OcXF2Z68JJq\n" +
                "4qOOhpkik59PWow3NTow2+vsagu5lXVuJOuQ3qBXP3kBjPzID7jiutnjDZKnB9KxLtHHDpwema0i\n" +
                "yWjkL5EYHjkdKo2r7JOc1vXdqrZxk/UVk/Y2WTOOM1snoYtam3ZSbyMKP51vWykqM9KwNPTaAe9d\n" +
                "Db5KDLAVlI0iXUYDjr9KsICfaq8QAwc81aj64xWZZajHYCrsAORxVWEVoQR8jNAi/CPlzXhviG8F\n" +
                "94mvZ1bcglYKQewOBXrniTVxoegzXQP73G2NT3JIH6da8NBJUs33nOfwrrw0Lyuc2InaNiyHKpnP\n" +
                "QU6JUaRlx26iqzNxj14qxbffJPevTPPJUklhbAfcB0Ujn862rDxhrumlRbanMBgDypyHA9huyMfS\n" +
                "sKXiUmkOHUggHHrQ4piues6P8U7aaRItXtTa4XBnjJdSf90DP5ZrurHUbLUoPPsrqK4j6bo2zj6+\n" +
                "n4180qzoQVfOOm7kVoabquo6dOJLKaWJ8jLRSFc+x9R7Gs3S7FKfc+j6K81034olfLj1OzGScGSM\n" +
                "lcfgeP1rttL8Q6Zq4H2S6VnxkxkEMPwPWs3FotNM1KKSlqRnBa58RtL06OSKyP2q46Kdp2Z9z1P4\n" +
                "fnXnOt+JtT1qQvNNJsbkRhtqLz2XJ/XmshdqA4HP55ppOc5rpjBIycmwZ5X5eQjnOE4pBtU8Dn17\n" +
                "0AjvSHGaskeG5NJnJpAOPeloGIeDnmlBpCaToaAH54oBpuaM0ADDOfXtUbDK8Gpf51GevtQBs+Ft\n" +
                "d/sfUgZi5t5FKOoGfofz/rXpFyFljEsbBkYZBHQivGHXHzCur8LeJ/su2wvG/wBGJ+VscoSe/t1z\n" +
                "XFiaPN7y3OrD1uX3WdaRg9OKkRwOOKlkhBG5SCp5B9arnIbmvNPRQsnIJFZ1w23OQPxFX2Hy1Qnj\n" +
                "znihAzKuNjdQCfpVVY4wSGQHPrmr8kBOf61EtufQ/wBKq5NhsISM4CgDtxWjC/y1FHalkwFOaljh\n" +
                "ZDhgaTY0i2jYq3AcnHeqiIfxxWhbRHjNIZdt1yRWrAu3Gaq28PT1rnfGviVdNsn021cG7mXa7DnY\n" +
                "p/qRVRjd2IlKyOV8ea2usayLaBj9mtQUz0y2eT+grlM5Oe3amsecA/U0E54r1aUOSJ5lSpzMeuWO\n" +
                "atQjBFQxrgVLGSX4rdGRJMOc1EKmlViQAKI4cHJpgMjiLn2q4ihRgU0DGOlP70wHg7cYp6beq5Rj\n" +
                "/Ehwf/r1Ax5qaP7uadgOi0rxfrOkkKs/2qH/AJ53BJI+nP8AhXaW/wAS9H8hPtMVzDNj5kEW4A+x\n" +
                "zyK8rBPWn5PoaiVOLBSaMfPFNpM4ozmmIM0uaTmkzQMeOlGTTM0oPbNADuo6UlLnikPNACZoHWgi\n" +
                "igBc0HB7Z9aBS4NAERUjg8+lV3BVsirpjJ7fhUTwlQT1X1xUtAdB4a8XNpuLW7DSWp6AEZQ56j/C\n" +
                "u/hNvqFstzayrJGwB4PI+vpXizoQciruma/f6PMHtp3Vf4kz8rfUdDXJWw6nqtzqo4hw0ex655Bz\n" +
                "TGst1Yel+PtLuwFvwLV8cuMsrH6Y4/Wustr/AE27UGC9tnz0AkGfy61wSpTi9Ud0asZbMxpLE7un\n" +
                "6UR2BJ6dfaulFoshBwp+hzUgs1AOFFRqXoYMViVOdv6VOdLMoyBg1ti3jQEuyqB3JoW90yM4k1Cz\n" +
                "TH96df8AGmotg2kYYsGRvmUir8FqARxijUPFPhiyU+dqEEjgZAhG8n8Rx+tcHrPxLDb49KgMXpLI\n" +
                "244+mMD9a2hRm9kYzrRS1Z1PiXxHBoVoY4wHumB2gEfLx1NeOXl7LeXUs0jFpJGLO3qTRcX13qMx\n" +
                "kmlkdmPJY5pVt1VeTzXdSoqCv1OGrWc9OhAOBjvUsSEnNTLaA/X+VWkhRRgDmuhIwIVjzwBxU8Ke\n" +
                "W3vT8AdKRBlwapIRK+PSmAVOy5XNNAx0HNUMQjA5pvenE+9MdsAn2pANB3y9OlTlsLUMAIX3pXcB\n" +
                "sUxEqnP0p+72qJTgc04HigDJz3ozTM80ZqAJM8U3NNzThzQMWilCk0pXCE0wAGpUAxUS1ZjTpTAG\n" +
                "iDDPSo9gzVhlwKiGGPNAChAB0pwXPalHAoGAKBEbLjNOXa/s2MZpJDUathsigCGe3KHpgVTkjGTk\n" +
                "VsiQEH9Qaje2jlJwdpPp0qXG4zCaIjocULLcRfclYY9DWnJZlGwy7gO6kf1quYFLcOB7MMGocB3G\n" +
                "x6xqcIAS7mXHo5/xqc+IdYYYOoXRH/XVv8ahW2ZuijHsRTlhYf8ALJv++ankHzMa2qahLw9xK31Y\n" +
                "mojcXZ6M3vzV2OEnrGR9RUoUDjgfjVKmLmZmpDcPwWOPrU8diM5Y5q6ilm9vYVIIzv8AlA7fe5/+\n" +
                "tVKArkSQBQdo6VII+eDx/eqUKB1Jb69qRid1XYQgGBgU4A5pFIzUn0oAMcZoQfMKU9Kav3ulAE+R\n" +
                "jFNPHSlBOMUh57UwGk4Gc1Wkbc4Wp3OFqvENz7jQMsDjr2FQscsKlJOM1AxBlHsaQFlhhB/jSK3y\n" +
                "/wD16ZK37vpTYm/digRnkUlKATS4INSAqLnoKnSLBGaIcelWMVSQEe3FMk4XvUvTpUUuelADEHIq\n" +
                "4o6VWjHNWlPFAwcnacVCn3qfI3Bpic0wJSO1Np9NNICKT7tRpyaklHy1HHjdQIdIMcikjl6e1SlA\n" +
                "V61RcGOT8aQzUBEi89ahKY4IyKjglyBVkHPpVAU3SFT86Ck22oHT8jVl0DdRxUXkqOMUmhEavagc\n" +
                "Lk+9SrIB91cA+gpPLX0pRwcUDJgcqaQUgPFLkUCFLcU1qQZ65oJ59aAFT61KKhXrUimmA5uhpFNI\n" +
                "xxQtAE46dqRjzSA4FMY5oAjnb5DTYQQuTSSctinj7uKAHtwo9aqqczYqeT7tVo/9dSAmnOIyRUcW\n" +
                "fLFFy2I8UsR/djmgD//Z"
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

    private fun registerFace() {
//        showLoading(true)
        val testImage = "/H8zXPeFvEN94Z1xLmA7W5SSNxww9CPqB+Ve8Rx7k5rxvxtYwweKp1iAG5g7f7x5NddGo2+VnHWo\n" +
                "pe9E9903WdP1WNWs7uGRigcxq4LoD/eAOR1q9mvmWWWSMJcwMwmiOeD1Fd/4b+J1zEBHqam8iOMS\n" +
                "KVEijvx0b9K7XBo5FJHrlJWNpvirRtVRPIvokkc4EUrBXz6YPX8K2cg9DUMoWikopAFFFFABRRSU\n" +
                "wK1LSUEgDJ6CgB1A64rm9R8caJYK6rcfaZkOPLiU/wDoXT9a5DUPijfEn7NaW9upHVyXI/Hgfoap\n" +
                "RbJbSPUpJI4Y2kldURRksxwAPc1yWteP9P0/MViBeT85IOEX8cfN+H515fe+ItV1PK3V5K8Z5w7H\n" +
                "af8AgOcVmPtJy7O4/uluPy/CrVPuS5m/qnjbWtRZg2oPDHk4jhbZx6ccn8a50M7H5cjPVqd8h5CK\n" +
                "PoKN1aqKRO5IMIuF4/GkLdMfT86QnIpuc5FMCNuuMfWtrwlqi6X4ht5ZHKxElX54wRjn9D+FYvf6\n" +
                "0zO1w3cVM480Wioy5Xc901AZWsJ+HzirGiamNZ0OKbJMsYEcmf7wA5/HOaiuF2ua8OcXF2Z68JJq\n" +
                "4qOOhpkik59PWow3NTow2+vsagu5lXVuJOuQ3qBXP3kBjPzID7jiutnjDZKnB9KxLtHHDpwema0i\n" +
                "yWjkL5EYHjkdKo2r7JOc1vXdqrZxk/UVk/Y2WTOOM1snoYtam3ZSbyMKP51vWykqM9KwNPTaAe9d\n" +
                "Db5KDLAVlI0iXUYDjr9KsICfaq8QAwc81aj64xWZZajHYCrsAORxVWEVoQR8jNAi/CPlzXhviG8F\n" +
                "94mvZ1bcglYKQewOBXrniTVxoegzXQP73G2NT3JIH6da8NBJUs33nOfwrrw0Lyuc2InaNiyHKpnP\n" +
                "QU6JUaRlx26iqzNxj14qxbffJPevTPPJUklhbAfcB0Ujn862rDxhrumlRbanMBgDypyHA9huyMfS\n" +
                "sKXiUmkOHUggHHrQ4piues6P8U7aaRItXtTa4XBnjJdSf90DP5ZrurHUbLUoPPsrqK4j6bo2zj6+\n" +
                "n4180qzoQVfOOm7kVoabquo6dOJLKaWJ8jLRSFc+x9R7Gs3S7FKfc+j6K81034olfLj1OzGScGSM\n" +
                "lcfgeP1rttL8Q6Zq4H2S6VnxkxkEMPwPWs3FotNM1KKSlqRnBa58RtL06OSKyP2q46Kdp2Z9z1P4\n" +
                "fnXnOt+JtT1qQvNNJsbkRhtqLz2XJ/XmshdqA4HP55ppOc5rpjBIycmwZ5X5eQjnOE4pBtU8Dn17\n" +
                "0AjvSHGaskeG5NJnJpAOPeloGIeDnmlBpCaToaAH54oBpuaM0ADDOfXtUbDK8Gpf51GevtQBs+Ft\n" +
                "d/sfUgZi5t5FKOoGfofz/rXpFyFljEsbBkYZBHQivGHXHzCur8LeJ/su2wvG/wBGJ+VscoSe/t1z\n" +
                "XFiaPN7y3OrD1uX3WdaRg9OKkRwOOKlkhBG5SCp5B9arnIbmvNPRQsnIJFZ1w23OQPxFX2Hy1Qnj\n" +
                "znihAzKuNjdQCfpVVY4wSGQHPrmr8kBOf61EtufQ/wBKq5NhsISM4CgDtxWjC/y1FHalkwFOaljh\n" +
                "ZDhgaTY0i2jYq3AcnHeqiIfxxWhbRHjNIZdt1yRWrAu3Gaq28PT1rnfGviVdNsn021cG7mXa7DnY\n" +
                "p/qRVRjd2IlKyOV8ea2usayLaBj9mtQUz0y2eT+grlM5Oe3amsecA/U0E54r1aUOSJ5lSpzMeuWO\n" +
                "atQjBFQxrgVLGSX4rdGRJMOc1EKmlViQAKI4cHJpgMjiLn2q4ihRgU0DGOlP70wHg7cYp6beq5Rj\n" +
                "/Ehwf/r1Ax5qaP7uadgOi0rxfrOkkKs/2qH/AJ53BJI+nP8AhXaW/wAS9H8hPtMVzDNj5kEW4A+x\n" +
                "zyK8rBPWn5PoaiVOLBSaMfPFNpM4ozmmIM0uaTmkzQMeOlGTTM0oPbNADuo6UlLnikPNACZoHWgi\n" +
                "igBc0HB7Z9aBS4NAERUjg8+lV3BVsirpjJ7fhUTwlQT1X1xUtAdB4a8XNpuLW7DSWp6AEZQ56j/C\n" +
                "u/hNvqFstzayrJGwB4PI+vpXizoQciruma/f6PMHtp3Vf4kz8rfUdDXJWw6nqtzqo4hw0ex655Bz\n" +
                "TGst1Yel+PtLuwFvwLV8cuMsrH6Y4/Wustr/AE27UGC9tnz0AkGfy61wSpTi9Ud0asZbMxpLE7un\n" +
                "6UR2BJ6dfaulFoshBwp+hzUgs1AOFFRqXoYMViVOdv6VOdLMoyBg1ti3jQEuyqB3JoW90yM4k1Cz\n" +
                "TH96df8AGmotg2kYYsGRvmUir8FqARxijUPFPhiyU+dqEEjgZAhG8n8Rx+tcHrPxLDb49KgMXpLI\n" +
                "244+mMD9a2hRm9kYzrRS1Z1PiXxHBoVoY4wHumB2gEfLx1NeOXl7LeXUs0jFpJGLO3qTRcX13qMx\n" +
                "kmlkdmPJY5pVt1VeTzXdSoqCv1OGrWc9OhAOBjvUsSEnNTLaA/X+VWkhRRgDmuhIwIVjzwBxU8Ke\n" +
                "W3vT8AdKRBlwapIRK+PSmAVOy5XNNAx0HNUMQjA5pvenE+9MdsAn2pANB3y9OlTlsLUMAIX3pXcB\n" +
                "sUxEqnP0p+72qJTgc04HigDJz3ozTM80ZqAJM8U3NNzThzQMWilCk0pXCE0wAGpUAxUS1ZjTpTAG\n" +
                "iDDPSo9gzVhlwKiGGPNAChAB0pwXPalHAoGAKBEbLjNOXa/s2MZpJDUathsigCGe3KHpgVTkjGTk\n" +
                "VsiQEH9Qaje2jlJwdpPp0qXG4zCaIjocULLcRfclYY9DWnJZlGwy7gO6kf1quYFLcOB7MMGocB3G\n" +
                "x6xqcIAS7mXHo5/xqc+IdYYYOoXRH/XVv8ahW2ZuijHsRTlhYf8ALJv++ankHzMa2qahLw9xK31Y\n" +
                "mojcXZ6M3vzV2OEnrGR9RUoUDjgfjVKmLmZmpDcPwWOPrU8diM5Y5q6ilm9vYVIIzv8AlA7fe5/+\n" +
                "tVKArkSQBQdo6VII+eDx/eqUKB1Jb69qRid1XYQgGBgU4A5pFIzUn0oAMcZoQfMKU9Kav3ulAE+R\n" +
                "jFNPHSlBOMUh57UwGk4Gc1Wkbc4Wp3OFqvENz7jQMsDjr2FQscsKlJOM1AxBlHsaQFlhhB/jSK3y\n" +
                "/wD16ZK37vpTYm/digRnkUlKATS4INSAqLnoKnSLBGaIcelWMVSQEe3FMk4XvUvTpUUuelADEHIq\n" +
                "4o6VWjHNWlPFAwcnacVCn3qfI3Bpic0wJSO1Np9NNICKT7tRpyaklHy1HHjdQIdIMcikjl6e1SlA\n" +
                "V61RcGOT8aQzUBEi89ahKY4IyKjglyBVkHPpVAU3SFT86Ck22oHT8jVl0DdRxUXkqOMUmhEavagc\n" +
                "Lk+9SrIB91cA+gpPLX0pRwcUDJgcqaQUgPFLkUCFLcU1qQZ65oJ59aAFT61KKhXrUimmA5uhpFNI\n" +
                "xxQtAE46dqRjzSA4FMY5oAjnb5DTYQQuTSSctinj7uKAHtwo9aqqczYqeT7tVo/9dSAmnOIyRUcW\n" +
                "fLFFy2I8UsR/djmgD//Z"
        LiveNessSDK.registerFace(requireContext(), testImage, null, object : CallbackAPIListener {
            override fun onCallbackResponse(data: String?) {
                Log.d("Thuytv", "------data: $data")
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

    fun setCallBack(mCallbackAPIListener: CallbackAPIListener) {
        this.mCallbackAPIListener = mCallbackAPIListener
    }
}