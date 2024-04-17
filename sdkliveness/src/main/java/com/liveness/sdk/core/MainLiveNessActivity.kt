package com.liveness.sdk.core

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import android.view.View
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
import com.liveness.sdk.core.model.LivenessRequest
import com.liveness.sdk.core.utils.AppConfig
import com.liveness.sdk.core.utils.AppPreferenceUtils
import com.liveness.sdk.core.utils.AppUtils
import com.liveness.sdk.core.utils.TotpUtils
import com.nimbusds.jose.shaded.gson.Gson
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraOptions
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Mode
import org.json.JSONObject
import java.io.File
import java.util.Base64
import java.util.Random


/**
 * Created by Thuytv on 15/04/2024.
 */
internal class MainLiveNessActivity : Activity() {
    private val REQUEST_PERMISSION_CODE = 1231
    private val pathVideo = Environment.getExternalStorageDirectory().toString() + "/Download/" + "VideoLiveNess" + System.currentTimeMillis() + ".mp4"
    private var bgColor = 0
    private var lstBgDefault: ArrayList<Int> = arrayListOf(R.drawable.img_0, R.drawable.img_1, R.drawable.img_2, R.drawable.img_3)

    private val permissions = arrayOf(
        Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
    )


    private val binding by lazy {
        UiMainLiveNessBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.imvBack.setOnClickListener {
            finish()
        }
        if (checkPermissions()) {
            init()
        } else {
            requestPermissions()
        }
    }

    private fun checkPermissions(): Boolean {
        val resultCamera = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)
        val resultRecord = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        return resultCamera == PackageManager.PERMISSION_GRANTED && resultRecord == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this@MainLiveNessActivity, permissions, REQUEST_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (checkPermissions()) {
                Toast.makeText(applicationContext, "Permission granted", Toast.LENGTH_LONG).show()
                init()
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

    private fun init() = binding.apply {
        val lensFacing = Facing.FRONT
        setupCamera(lensFacing)
    }

    private fun setupCamera(lensFacing: Facing) = binding.apply {
        val faceDetector = FaceDetector(faceBoundsOverlay)
        faceDetector.setonFaceDetectionFailureListener(object : FaceDetector.OnFaceDetectionResultListener {
            override fun onSuccess(faceBounds: Face) {
                super.onSuccess(faceBounds)
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

        })
        cameraViewVideo.facing = lensFacing
        cameraViewVideo.mode = Mode.VIDEO
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

        cameraViewVideo.addCameraListener(object : CameraListener() {
            override fun onCameraOpened(options: CameraOptions) {
                super.onCameraOpened(options)
                cameraViewVideo.takeVideoSnapshot(File(pathVideo))
            }

            override fun onPictureTaken(result: PictureResult) {
                super.onPictureTaken(result)
                Log.d("Thuytv", "-----onPictureTaken")
                result.data.let {
                    val imgLiveNess = android.util.Base64.encodeToString(it, android.util.Base64.NO_PADDING)
                    callAPIGEtTOTP(imgLiveNess, bgColor)
                }
                binding.bgFullScreenDefault.visibility = View.GONE
                binding.llVideo.visibility = View.VISIBLE

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
    }

    override fun onResume() {
        super.onResume()
        binding.cameraViewVideo.open()
    }

    override fun onPause() {
        super.onPause()
        binding.cameraViewVideo.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.cameraViewVideo.destroy()
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
            AppUtils.showLog("Thuytv------checkLiveNessFlash--success")
            val liveNessModel = Gson().fromJson<LivenessModel>(response, LivenessModel::class.java)
            if (liveNessModel.success == true) {
                liveNessModel.pathVideo = pathVideo
                this.runOnUiThread {
                    showLoading(false)
                    AppConfig.livenessListener?.onCallbackLiveness(liveNessModel)
                    finish()
                }
            } else {
                showToast(result?.getString("message") ?: "Error")
            }
        } else {
            showToast(result?.getString("message") ?: "Error")
        }
    }

    fun callAPIGEtTOTP(imgLiveNess: String, bgColor: Int) {
        showLoading(true)
        Thread {
            val tOTP = TotpUtils.getInstance(this)?.getTotp()
            Log.d("Thuytv", "-----tOTP: $tOTP")
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
}