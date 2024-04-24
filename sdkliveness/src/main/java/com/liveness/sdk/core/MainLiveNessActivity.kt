package com.liveness.sdk.core

import android.Manifest
import android.R.layout
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.mlkit.vision.face.Face
import com.liveness.sdk.core.api.HttpClientUtils
import com.liveness.sdk.core.databinding.UiMainLiveNessBinding
import com.liveness.sdk.core.facedetector.FaceDetector
import com.liveness.sdk.core.facedetector.Frame
import com.liveness.sdk.core.facedetector.LensFacing
import com.liveness.sdk.core.model.LivenessModel
import com.liveness.sdk.core.utils.AppConfig
import com.liveness.sdk.core.utils.AppPreferenceUtils
import com.liveness.sdk.core.utils.AppUtils
import com.liveness.sdk.core.utils.TotpUtils
import com.nimbusds.jose.shaded.gson.Gson
import com.otaliastudios.cameraview.CameraException
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraOptions
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Mode
import org.json.JSONObject
import java.io.File
import java.util.Random


/**
 * Created by Thuytv on 15/04/2024.
 */
internal class MainLiveNessActivity : Activity() {
    private val REQUEST_PERMISSION_CODE = 1231
    private var pathVideo = ""
    private var bgColor = 0
    private var isCapture = false
    private var lstBgDefault: ArrayList<Int> = arrayListOf(R.drawable.img_0, R.drawable.img_1, R.drawable.img_2, R.drawable.img_3)

    private var isFirstVideo = true
    private var typeScreen = ""

    private var permissions = arrayOf(
        Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
    )


    private val binding by lazy {
        UiMainLiveNessBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (AppConfig.mCustomView != null) {
            binding.frameViewCustom.addView(AppConfig.mCustomView)
        }

        typeScreen = intent.getStringExtra(AppConfig.KEY_BUNDLE_SCREEN) ?: ""
        binding.imvBack.setOnClickListener {
            finish()
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permissions = arrayOf(
                Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        initCamera()
        if (checkPermissions()) {
            binding.cameraViewVideo.open()
        } else {
            requestPermissions()
        }
        if (typeScreen == AppConfig.TYPE_SCREEN_REGISTER_FACE) {
            binding.btnCapture.setOnClickListener {
                binding.cameraViewVideo.takePictureSnapshot()
            }
        } else {
            binding.btnCapture.visibility = View.GONE
        }
    }

    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            val resultCamera = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)
            val resultRecord = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
            val resultRead = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE)
            val resultWrite = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            (resultCamera == PackageManager.PERMISSION_GRANTED
                    && resultRecord == PackageManager.PERMISSION_GRANTED
                    && resultRead == PackageManager.PERMISSION_GRANTED
                    && resultWrite == PackageManager.PERMISSION_GRANTED)
        } else {
            val resultCamera = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)
            val resultRecord = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
            resultCamera == PackageManager.PERMISSION_GRANTED && resultRecord == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this@MainLiveNessActivity, permissions, REQUEST_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (checkPermissions()) {
//                Toast.makeText(applicationContext, "Permission granted", Toast.LENGTH_LONG).show()
                binding.cameraViewVideo.open()
            } else {
                Toast.makeText(applicationContext, "Permission denied", Toast.LENGTH_LONG).show()
            }
//            if (grantResults.isNotEmpty()) {
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(applicationContext, "Permission granted", Toast.LENGTH_LONG).show()
//                    init()
//                } else {
//                    Toast.makeText(applicationContext, "Permission denied", Toast.LENGTH_LONG).show()
//                }
//            }
        }
    }

    private fun initCamera() {
        binding.apply {
            pathVideo = Environment.getExternalStorageDirectory().toString() + "/Download/" + "VideoLiveNess" + System.currentTimeMillis() + ".mp4"
            val lensFacing = Facing.FRONT
            setupCamera(lensFacing)
        }
    }

    private fun setupCamera(lensFacing: Facing) = binding.apply {
        val faceDetector = FaceDetector(faceBoundsOverlay)
        faceDetector.setonFaceDetectionFailureListener(object : FaceDetector.OnFaceDetectionResultListener {
            override fun onSuccess(faceBounds: Face) {
                super.onSuccess(faceBounds)
                AppUtils.showLog("Thuytv--faceDetector--onSuccess")
                if (!isCapture) {
                    isCapture = true
                    cameraViewVideo.stopVideo()
                    binding.bgFullScreenDefault.visibility = View.VISIBLE
                    binding.llVideo.visibility = View.GONE
                    bgColor = Random().nextInt(3)
                    AppUtils.showLog("Thuytv----bgColor: $bgColor")
                    binding.bgFullScreenDefault.background = ResourcesCompat.getDrawable(resources, lstBgDefault[bgColor], this@MainLiveNessActivity.theme)
                    Handler(Looper.myLooper()!!).postDelayed({
                        cameraViewVideo.takePictureSnapshot()
                    }, 300)
                }
            }

        })
        cameraViewVideo.facing = lensFacing
        cameraViewVideo.mode = Mode.VIDEO


        cameraViewVideo.addCameraListener(object : CameraListener() {
            override fun onCameraOpened(options: CameraOptions) {
                super.onCameraOpened(options)
                AppUtils.showLog("Thuytv--onCameraOpened : ")
                if (typeScreen != AppConfig.TYPE_SCREEN_REGISTER_FACE) {
                    cameraViewVideo.takeVideoSnapshot(File(pathVideo))
                } else {
                    binding.btnCapture.visibility = View.VISIBLE
                }
            }

            override fun onCameraError(exception: CameraException) {
                super.onCameraError(exception)
                AppUtils.showLog("Thuytv--onCameraError")
            }

            override fun onCameraClosed() {
                super.onCameraClosed()
                AppUtils.showLog("Thuytv--onCameraClosed")
            }

            override fun onPictureTaken(result: PictureResult) {
                super.onPictureTaken(result)
                Log.d("Thuytv", "-----onPictureTaken")
                if (typeScreen != AppConfig.TYPE_SCREEN_REGISTER_FACE) {
                    result.data.let {
                        val imgLiveNess = android.util.Base64.encodeToString(it, android.util.Base64.NO_PADDING)
                        callAPIGEtTOTP(imgLiveNess, bgColor)
                    }
                    binding.bgFullScreenDefault.visibility = View.GONE
                    binding.llVideo.visibility = View.VISIBLE
                    isCapture = false
                } else {
                    result.data.let {
                        val faceImage = android.util.Base64.encodeToString(it, android.util.Base64.NO_PADDING)
                        registerFace(faceImage)
                    }
                }
            }

            override fun onVideoTaken(result: VideoResult) {
                super.onVideoTaken(result)
                Log.d("Thuytv", "-----onVideoTaken")
                result.file.let {
//                    AppConfig.livenessListener?.onCallbackLiveness(LivenessModel(pathVideo = it.absolutePath))
//                    finish()
                }
            }
        })
        if (typeScreen != AppConfig.TYPE_SCREEN_REGISTER_FACE) {
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
//        binding.cameraViewVideo.open()
    }

    override fun onPause() {
        super.onPause()
        binding.cameraViewVideo.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.cameraViewVideo.destroy()
        if (AppConfig.mCustomView != null) {
            binding.frameViewCustom.removeView(AppConfig.mCustomView)
        }
    }

//    private fun demoAPI() {
//        val privateKey = "-----BEGIN PRIVATE KEY-----\n" +
//                "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC7K1QJ6YLRMoNv\n" +
//                "mqV3qkNXJZWxCT95+/xVlKhea/i3QpJk5Ier4hgUqn3MmlU3NXRv6eHZh579uzqM\n" +
//                "1U8PJzMGj2j9XnlOkQtWImSKiMUPQYJ4CSnUGCLpiMomZKjGO4tLiS/yCSJ/BFrI\n" +
//                "TBGYJhJL9wOYYzZdY/mNWSr1+ZsqQFH0Ug7n4XfYrJD7XgtozIgiY5LtbdoV/6At\n" +
//                "TN8H8czzQnLaA8NjDb8npP7ogu99KVivtoAJJhLZJ9nvXyP9Dp7sKi9d33LaxFwH\n" +
//                "OiNWRm3qFGm1G8RZoWg4G1h+ETL0z4COBgvAhToGI5gkkeyw+qFfS9hqLN0hJWAm\n" +
//                "o41oGPo1AgMBAAECggEBAI1qzmNy4JmJjg+MDAufRKQazMBnmWNkhiJvYMt+zvxA\n" +
//                "O3YpyWyQNtueedBWp55AMErCrxd5xiI2DaYNIV/0oTQKtSwC7qrzIlqhP9AASMwf\n" +
//                "FiH14nnTBsXmyb46fd7RbIzVCbnZNww7URBXkU+hLF/jMf84rwHfINWwkqopPxir\n" +
//                "F5Ohqt1G/PxzI3/rc20DzDJX331em5qHBqACp1JcHXtpaFKBOJihVnhYqxon9k1o\n" +
//                "qcR79HNRlIwHWsxsOUEM8zPTbstQaqMgKLFXyENM43C+B/f+Oz2DBdF32RD7jq8Q\n" +
//                "xLR1gidq+KCXEejOBuRexrrT4fQiCb7e2robh8o/IUECgYEA4XubVcqjmZmhIlt9\n" +
//                "PU+63IC1dEVc40PZtJ5AiQvZa+zLCl9ik+9k/dmJE5WUZDki76W3OB+kJq3fUWgQ\n" +
//                "tyo0UkpxHwqryefGg09syu5cNGE/zd7ZREF0aIsHnXaPtroKq8Z6mz4FctLt9egr\n" +
//                "8V0M670N9rQz996+E/KHf4jEeBECgYEA1IBBZNWJDE7lIip8CobnPVh718p2HTuc\n" +
//                "lxeTFrRgI3wWnitYhCGLnJMGDvNv/znApsB7aAgVFz3r5jGKxTPPCwa9gwrKXoJy\n" +
//                "vBWRIL2gajImGU5fOoDQZJ3dGNgNh8anoe0/esMbdIZMFY6rAIWGiE+Y17+Or1UL\n" +
//                "EBUen8o7Y+UCgYBWn17QeZWaF5wAj/cwC6Y0ubl73n3NzS4gpj8Spxuyy3hBFt3P\n" +
//                "CUPaBa0Uef1U92JFgHs/s2Ajf95v7rOlOjB5gKGulDHk0gbAQU4BM8r2UHnrg/Yh\n" +
//                "s6ed1fNp+bdCMnyQ+yH068G6F/BU7Qmcouuo0KtBoH7qdYa+MQj+5LLdkQKBgEdJ\n" +
//                "56ZORLXOWexGWGqnqzfXUWSpVUqlTvkZPY0mYgJFhMj3PbDGGDIk2Kl3XaE/3LOU\n" +
//                "a1IRNBIiAdutzyItKU5HqpglrJJcLOWQTqmvM/usaz+eHTBhOogmtZ+6C3/7Uw1t\n" +
//                "rBghEDrdOvUYcaGxKdrc6Sen6dREMXvpueZdT+NJAoGAIsPaK0Rgu6Z540hiCF2M\n" +
//                "0yYHriXljTAWtdm5FpCfoLwKox1OYLMQlFIXfN1qqmo6m13O+MW3IIU7X/aAk7T6\n" +
//                "UW7GZybBe40J2AxVC48GX+jVk5iQjBzUtEf81jIZp61AD5KijNn33lHf653K09ch\n" +
//                "uw+D9R3JrjzTHoyep6eif/s=\n" +
//                "-----END PRIVATE KEY-----\n"
//        val appId = "com.qts.test"
//        AppConfig.mLivenessRequest = LivenessRequest(duration = 600,privateKey = privateKey, appId = appId, deviceId = AppUtils.getDeviceId(this), clientTransactionId = "TEST" )
//        val httpClientUtil = HttpClientUtils.instance
//        httpClientUtil?.setVariables(this, appId, privateKey, "https://face-matching.vietplus.eu")
//    }

    private fun initTransaction(tOTP: String, imgLiveNess: String, bgColor: Int) {
        val response = HttpClientUtils.instance?.initTransaction(this)
        var result: JSONObject? = null
        if (!response.isNullOrEmpty()) {
            result = JSONObject(response)
        }
        if (result?.has("status") == true && result.getInt("status") == 200) {
            val transactionId = result.getString("data")
            val signature = result.getString("signature")
            checkLiveNessFlash(tOTP, transactionId, imgLiveNess, bgColor)
        } else {
            showToast(result?.getString("message") ?: "Error")
        }
    }

    private fun checkLiveNessFlash(tOTP: String, transactionID: String, imgLiveNess: String, bgColor: Int) {
        val response = HttpClientUtils.instance?.checkLiveNessFlash(this, tOTP, transactionID, imgLiveNess, bgColor)
        var result: JSONObject? = null
        if (response?.isNotEmpty() == true) {
            result = JSONObject(response)
        }
        if (result?.has("status") == true && result.getInt("status") == 200) {
            val liveNessModel = Gson().fromJson<LivenessModel>(response, LivenessModel::class.java)
//            if (liveNessModel.success == true) {
            liveNessModel.pathVideo = pathVideo
            this.runOnUiThread {
                showLoading(false)
                AppConfig.livenessListener?.onCallbackLiveness(liveNessModel)
                finish()
            }
//            } else {
//                showToast(result?.getString("message") ?: "Error")
//            }
        } else {
            showToast(result?.getString("message") ?: "Error")
        }
    }

    fun callAPIGEtTOTP(imgLiveNess: String, bgColor: Int) {
        showLoading(true)
        Thread {
            val tOTP = TotpUtils(this).getTotp()
            if (tOTP.isNullOrEmpty()) {
                showToast("TOTP null")
            } else {
                initTransaction(tOTP, imgLiveNess, bgColor)
            }
        }.start()
    }

    private fun showToast(strToast: String) {
        this.runOnUiThread {
            Toast.makeText(this, strToast, Toast.LENGTH_SHORT).show()
            showLoading(false)
        }
    }

    private fun showLoading(isShow: Boolean) {
        this.runOnUiThread {
            if (isShow) {
                binding.prbLoading.visibility = View.VISIBLE
            } else {
                binding.prbLoading.visibility = View.GONE
            }
        }

    }

    private fun registerFace(faceImage: String) {
        showLoading(true)
        Thread {
            val request = JSONObject()
            request.put("deviceId", AppConfig.mLivenessRequest?.deviceId)
            request.put("deviceOs", "Android")
            request.put("deviceName", Build.MANUFACTURER + " " + Build.MODEL)
            request.put("period", AppConfig.mLivenessRequest?.duration)
            request.put("secret", AppConfig.mLivenessRequest?.secret)
            val responseDevice = HttpClientUtils.instance?.postV3("/eid/v3/registerDevice", request)
            Log.d("Thuytv", "---response: $responseDevice")
            var result: JSONObject? = null
            if (responseDevice != null && responseDevice.length > 0) {
                result = JSONObject(responseDevice)
            }
            if (result != null && result.has("status") && result.getInt("status") == 200) {
                val response = HttpClientUtils.instance?.registerFace(faceImage)
                var result: JSONObject? = null
                if (response?.isNotEmpty() == true) {
                    result = JSONObject(response)
                }
                if (result?.has("status") == true && result.getInt("status") == 200) {
                    AppUtils.showLog("Thuytv------registerFace--success")
                    showLoading(false)
                    AppPreferenceUtils(this).setDeviceId(this, AppConfig.mLivenessRequest?.deviceId ?: "")
                    this.runOnUiThread {
                        AppConfig.livenessListener?.onCallbackLiveness(null)
                        showToast("Register Face Success")
                        finish()
                    }

                } else {
                    showLoading(false)
                    showToast(result?.getString("message") ?: "Error")
                }
            }
        }.start()

    }
}