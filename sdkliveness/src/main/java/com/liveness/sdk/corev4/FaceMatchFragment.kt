package com.liveness.sdk.corev4

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.mlkit.vision.face.Face
import com.liveness.sdk.corev4.facedetector.FaceDetectorScan
import com.liveness.sdk.corev4.facedetector.Frame
import com.liveness.sdk.corev4.facedetector.LensFacing
import com.liveness.sdk.corev4.slider.SliderAdapter
import com.liveness.sdk.corev4.slider.SliderView
import com.liveness.sdk.corev4.utils.AppConfig
import com.nimbusds.jose.shaded.gson.Gson
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Mode
import com.liveness.sdk.corev4.api.HttpClientUtils
import com.liveness.sdk.corev4.model.LivenessModel
import com.liveness.sdk.corev4.utils.AppPreferenceUtils
import com.liveness.sdk.corev4.utils.AppUtils
import com.liveness.sdk.corev4.utils.TotpUtils
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import java.util.concurrent.Executors

/**
 * Created by Hieudt43 on 26/09/2024.
 */
internal class FaceMatchFragment : Fragment() {
    private var isShowToolbar: Boolean = true
    private val REQUEST_PERMISSION_CODE = 1231

    private var mSessionId = ""
    private lateinit var cameraViewVideo: CameraView
    private lateinit var prbLoading: ProgressBar
    private lateinit var tvStatus: TextView
    private var mFrameMark: FrameLayout? = null
    private lateinit var mFrameImageMax: ImageView

    private lateinit var swSaveImage: Switch
    private lateinit var toolbar: LinearLayout
    private lateinit var btBack: ImageView
    private lateinit var slider: SliderView

    private var mFaceDetector: FaceDetectorScan? = null
    private var mStepScan = 0
    private var lstImageInit: ArrayList<String> = java.util.ArrayList()
    private var lstImageRed: ArrayList<String> = java.util.ArrayList()
    private var lstImageGreen: ArrayList<String> = java.util.ArrayList()
    private var lstImageBlue: ArrayList<String> = java.util.ArrayList()
    private var permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    private val listColor: ArrayList<Int> = arrayListOf()

    private lateinit var sliderAdapter: SliderAdapter
    private val mHandler = Handler(Looper.getMainLooper())
    private lateinit var mCaptureRunnable: Runnable
    private lateinit var mStatusRunnable: Runnable
    private var typeScreen: String? = null
    private var mFragmentManager: FragmentManager? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_face_scan_fm, container, false)
        cameraViewVideo = view.findViewById(R.id.camera_view_video)
        prbLoading = view.findViewById(R.id.pbLoading)
        mFrameMark = view.findViewById(R.id.frMark)
        mFrameImageMax = view.findViewById(R.id.imv_frame_face)
        tvStatus = view.findViewById(R.id.tvStatus)
        slider = view.findViewById(R.id.imageSlider)
        swSaveImage = view.findViewById(R.id.swSaveImage)
        toolbar = view.findViewById(R.id.llToolbar)
        btBack = view.findViewById(R.id.ivBack)
        if (arguments?.containsKey(AppConfig.KEY_BUNDLE_BOOLEAN) == true) {
            isShowToolbar = arguments?.getBoolean(AppConfig.KEY_BUNDLE_BOOLEAN, true) == true
        }
        if (arguments?.containsKey(AppConfig.KEY_BUNDLE_SCREEN) == true) {
            typeScreen = arguments?.getString(AppConfig.KEY_BUNDLE_SCREEN)
        }
        if (!isShowToolbar) {
            toolbar.visibility = View.GONE
        }
        btBack.setOnClickListener {
            onBackFragment()
        }
        initRunnable()
        initCamera(view)
        if (checkPermissions()) {
            cameraViewVideo.open()
        } else {
            requestPermissions()
        }
        initListColor()
        setScreenBrightness(1f)
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun initRunnable() {
        mCaptureRunnable = Runnable {
            cameraViewVideo.takePictureSnapshot()
        }
    }

    private fun initListColor() {
        listColor.add(ContextCompat.getColor(requireContext(), R.color.fm_transparent))
        listColor.add(ContextCompat.getColor(requireContext(), R.color.fm_color_red))
        listColor.add(ContextCompat.getColor(requireContext(), R.color.fm_color_green))
        listColor.add(ContextCompat.getColor(requireContext(), R.color.fm_color_blue))
        sliderAdapter = SliderAdapter()
        sliderAdapter.renewItems(listColor)
        slider.setSliderAdapter(sliderAdapter)
    }


    private fun checkPermissions(): Boolean {
        val resultCamera =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        val resultRecord = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.RECORD_AUDIO
        )
        return resultCamera == PackageManager.PERMISSION_GRANTED && resultRecord == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(requireActivity(), permissions, REQUEST_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (checkPermissions()) {
                cameraViewVideo.open()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initCamera(view: View) {
        val lensFacing = Facing.FRONT
        setupCamera(lensFacing, view)
    }

    private fun setupCamera(lensFacing: Facing, view: View) = apply {
        mFaceDetector = FaceDetectorScan(view.findViewById(R.id.faceBoundsOverlay))
        mFaceDetector?.setFrameImage(cameraViewVideo, mFrameImageMax)
        mFaceDetector?.setonFaceDetectionFailureListener(object :
            FaceDetectorScan.OnFaceDetectionResultListener {
            override fun onSuccess(faceBounds: Face, faceSize: Int) {
                super.onSuccess(faceBounds, faceSize)
            }

            override fun onFaceStatus(status: Int, percent: Int?) {

                restartSection()
                when (status) {
                    0 -> { // small
                        tvStatus.visibility = View.VISIBLE
                        tvStatus.text = getString(R.string.fm_come_closer)
                        prbLoading.visibility = View.VISIBLE
                        percent?.apply {
                            prbLoading.setProgress(percent, true)
                        }
                    }

                    1 -> { // big
                        tvStatus.visibility = View.VISIBLE
                        tvStatus.text = getString(R.string.fm_move_face_farther)
                        prbLoading.visibility = View.GONE
                    }

                    2 -> { // face out
                        tvStatus.visibility = View.VISIBLE
                        tvStatus.text = getString(R.string.fm_face_center_frame)
                        prbLoading.visibility = View.GONE
                    }

                    3 -> { // face euler fail
                        tvStatus.visibility = View.VISIBLE
                        tvStatus.text = getString(R.string.fm_look_straight)
                        prbLoading.visibility = View.GONE
                    }

                    4 -> { // no face
                        tvStatus.visibility = View.GONE
                        prbLoading.visibility = View.GONE
                    }

                    else -> {
                        showKeepDevice()
                    }
                }
            }

            override fun onProcessing(isFace: Boolean) {
                super.onProcessing(isFace)
                if (isFace) {
                    Log.d("Thuytv", "------onProcessing--mStepScan: $mStepScan")
                    if (mStepScan == 0) {
                        mStepScan = 1
                        showKeepDevice()
                        takePicture(1000)
                        mSessionId = UUID.randomUUID().toString()
                    }
                } else {
                    restartSection()
                }
            }

        })
        cameraViewVideo.facing = lensFacing
        cameraViewVideo.mode = Mode.VIDEO


        cameraViewVideo.addCameraListener(object : CameraListener() {

            override fun onPictureTaken(result: PictureResult) {
                super.onPictureTaken(result)
                result.data.let {
                    val mImage =
                        android.util.Base64.encodeToString(it, android.util.Base64.NO_PADDING)
                    Log.d("Thuytv", "------onPictureTaken--mStepScan: $mStepScan")
                    if (mStepScan == 1) {
                        if (lstImageInit.size < 1) {
                            lstImageInit.add(mImage)
                            takePicture(200)
                        } else {
                            mStepScan = 2
                            updateUIWhenCapture()
                        }
                    } else if (mStepScan == 2) {
                        if (lstImageRed.size < 1) {
                            lstImageRed.add(mImage)
                            takePicture(200)
                        } else {
                            mStepScan = 3
                            updateUIWhenCapture()
                        }
                    } else if (mStepScan == 3) {
                        if (lstImageGreen.size < 1) {
                            lstImageGreen.add(mImage)
                            takePicture(200)
                        } else {
                            mStepScan = 4
                            updateUIWhenCapture()
                        }
                    } else if (mStepScan == 4) {
                        if (lstImageBlue.size < 1) {
                            lstImageBlue.add(mImage)
                            takePicture(200)
                        } else {
                            mStepScan = 5
                            updateUIWhenCapture()
                        }
                    } else if (mStepScan == 5) {
                        Log.d("Thuytv", "------uploadFile--start")
                        uploadFile()
                        mStepScan++
                    }

                    Log.d(
                        "Thuytv",
                        "---mStepScan : $mStepScan" + "----lstImageInit: ${lstImageInit.size}"
                    )
                    Log.d(
                        "Thuytv",
                        "---lstImageRed : ${lstImageRed.size}" + "----lstImageBlue: ${lstImageBlue.size}" + "----lstImageGreen: ${lstImageGreen.size}"
                    )
                }

            }

        })
        cameraViewVideo.addFrameProcessor {
            mFaceDetector?.process(
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

    private fun restartSection() {
        mStepScan = 0
        lstImageRed.clear()
        lstImageInit.clear()
        lstImageBlue.clear()
        lstImageGreen.clear()
        mSessionId = ""
        tvStatus.visibility = View.GONE
        prbLoading.visibility = View.GONE
        slider.visibility = View.GONE
        slider.currentPagePosition = 0
        mHandler.removeCallbacks(mCaptureRunnable)
    }

    private fun showKeepDevice() {
        tvStatus.visibility = View.VISIBLE
        tvStatus.text = getString(R.string.fm_keep_face)
        prbLoading.visibility = View.GONE
        Handler(Looper.getMainLooper()).postDelayed({
            slider.visibility = View.VISIBLE
        }, 900)
    }

    private fun saveBitmapToDisk(bitmap: Bitmap?) {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            val appDir = File(root.absolutePath + File.separator + "SaveImage")
            if (!appDir.exists()) {
                val res: Boolean = appDir.mkdir()
                if (!res) {
                    Log.d("Thuytv", "------can't create folder---: ${appDir.absolutePath}")

                }
            }
            val fileName = "ImageScan" + System.currentTimeMillis() + ".jpg"
            val file = File(appDir, fileName)
            try {
                val fos = FileOutputStream(file)
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
                fos.close()
//                val uri: Uri = Uri.fromFile(file)
//                requireActivity().sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
                MediaScannerConnection.scanFile(
                    context, arrayOf(file.toString()), null, null
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
            handler.post {

            }
        }
    }

    private fun takePicture(delay: Long) {
        mHandler.postDelayed(mCaptureRunnable, delay)
    }

    override fun onResume() {
        super.onResume()
        cameraViewVideo.open()
        mFrameMark?.visibility = View.VISIBLE
        slider.currentPagePosition = 0
    }

    override fun onPause() {
        super.onPause()
        cameraViewVideo.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraViewVideo.destroy()
    }

    private fun onBackFragment() {
        if (activity is FaceMatchActivity) {
            activity?.finish()
        } else {
            mFragmentManager?.popBackStack()
        }
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

    private fun setScreenBrightness(brightnessValue: Float) {
        val layoutParams = activity?.window?.attributes
        layoutParams?.screenBrightness = brightnessValue
        activity?.window?.attributes = layoutParams
    }

    private fun uploadFile() {
        var imageB64 = ""
        for (item in lstImageInit) {
            imageB64 = item
        }
        var image2B64 = ""
        for (item in lstImageRed) {
            image2B64 = item
        }
        var image3B64 = ""
        for (item in lstImageGreen) {
            image3B64 = item
        }
        var image4B64 = ""
        for (item in lstImageBlue) {
            image4B64 = item
        }
        if (image4B64.isEmpty()) {
            showToastError("Image Init null")
        } else if (image2B64.isEmpty()) {
            showToastError("Image RED null")
        } else if (image3B64.isEmpty()) {
            showToastError("Image GREEN null")
        } else if (image4B64.isEmpty()) {
            showToastError("Image BLUE null")
        } else {
            callApiUploadSession(imageB64, image2B64, image3B64, image4B64)
        }
    }

    private fun callApiUploadSession(
        imageB64: String, image2B64: String, image3B64: String, image4B64: String
    ) {
        prbLoading.visibility = View.VISIBLE
        if (typeScreen != AppConfig.TYPE_SCREEN_REGISTER_FACE) {
            if (AppConfig.mLivenessRequest?.offlineMode == true) {
                AppConfig.livenessListener?.onCallbackLiveness(
                    LivenessModel(
                        imgTransparent = image2B64,
                        imgRed = image2B64,
                        imgGreen = image3B64,
                        imgBlue = image4B64
                    )
                )
                onBackFragment()
            } else {
                getTOTP(imageB64, image2B64, image3B64, image4B64)
            }

        } else {
            registerFace(imageB64)
        }
    }

    private fun getTOTP(imageB64: String, image2B64: String, image3B64: String, image4B64: String) {
        showLoading(true)
        Thread {
            val tOTP = TotpUtils(requireContext()).getTotp()
            if (tOTP.isEmpty() || tOTP == "-1") {
//                AppConfig.livenessListener?.onCallbackLiveness(LivenessModel(status = -1, message = ""))
                showToast("TOTP null")
            } else {
                initTransaction(
                    tOTP,
                    AppConfig.mLivenessRequest?.clientTransactionId,
                    imageB64,
                    image2B64,
                    image3B64,
                    image4B64
                )
            }
        }.start()
    }

    private fun initTransaction(
        tOTP: String,
        readCardId: String?,
        imageB64: String,
        image2B64: String,
        image3B64: String,
        image4B64: String
    ) {
        val response = HttpClientUtils.instance?.initTransaction(requireContext(), readCardId)
        var result: JSONObject? = null
        if (!response.isNullOrEmpty()) {
            result = JSONObject(response)
        }
        var status = -1
        if (result?.has("status") == true) {
            status = result.getInt("status")
        }
        var strMessage = "Error"
        if (result?.has("message") == true) {
            strMessage = result.getString("message")
        }
        if (status == 200) {
            val transactionId = result?.getString("data") ?: ""
            checkLiveNessFlash(tOTP, transactionId, imageB64, image2B64, image3B64, image4B64)
        } else {
            showToast(strMessage)
        }
    }

    private fun checkLiveNessFlash(
        tOTP: String,
        transactionID: String,
        imageB64: String,
        image2B64: String,
        image3B64: String,
        image4B64: String
    ) {
        val response = HttpClientUtils.instance?.checkLiveNessFlashV2(
            requireContext(), tOTP, transactionID, imageB64, image2B64, image3B64, image4B64
        )
        var result: JSONObject? = null
        if (response?.isNotEmpty() == true) {
            result = JSONObject(response)
        }
        var status = -1
        if (result?.has("status") == true) {
            status = result.getInt("status")
        }
        var strMessage = "Error"
        if (result?.has("message") == true) {
            strMessage = result.getString("message")
        }
        if (status == 200) {
            val liveNessModel = Gson().fromJson(response, LivenessModel::class.java)
            liveNessModel.livenessImage = imageB64
            liveNessModel.transactionID = transactionID
            activity?.runOnUiThread {
                showLoading(false)
                AppConfig.livenessListener?.onCallbackLiveness(liveNessModel)
                onBackFragment()
            }

        } else {
            activity?.runOnUiThread {
                showLoading(false)
                AppConfig.livenessListener?.onCallbackLiveness(
                    LivenessModel(
                        status = status, message = strMessage
                    )
                )
                onBackFragment()
            }
        }
    }

    private fun registerFace(faceImage: String) {
        showLoading(true)
        Thread {
            var mSecret = AppPreferenceUtils(requireContext()).getTOTPSecret(requireContext())
            if (mSecret.isNullOrEmpty() || mSecret.length != 16) {
                mSecret = AppUtils.getSecretValue()
            }
            var mDeviceId = AppPreferenceUtils(requireContext()).getDeviceId()
                ?: AppConfig.mLivenessRequest?.deviceId
            if (mDeviceId.isNullOrEmpty()) {
                mDeviceId = UUID.randomUUID().toString()
            }
            val request = JSONObject()
            request.put(
                AppUtils.decodeAndDecrypt(requireContext(), AppConfig.encrypted_deviceId), mDeviceId
            )
            request.put(
                AppUtils.decodeAndDecrypt(requireContext(), AppConfig.encrypted_deviceOS), "Android"
            )
            request.put(
                AppUtils.decodeAndDecrypt(
                    requireContext(), AppConfig.encrypted_device_name
                ), Build.MANUFACTURER + " " + Build.MODEL
            )
            request.put(
                AppUtils.decodeAndDecrypt(requireContext(), AppConfig.encrypted_period),
                AppConfig.mLivenessRequest?.duration
            )
            request.put(
                AppUtils.decodeAndDecrypt(requireContext(), AppConfig.encrypted_secret), mSecret
            )
            val responseDevice = HttpClientUtils.instance?.postV3(
                AppUtils.decodeAndDecrypt(
                    requireContext(), AppConfig.encrypted_register_face
                ), request
            )
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
                AppPreferenceUtils(requireContext()).setDeviceId(mDeviceId)
                AppPreferenceUtils(requireContext()).setTOTPSecret(requireContext(), mSecret)
                val response = HttpClientUtils.instance?.registerFace(requireContext(), faceImage)
                var result: JSONObject? = null
                if (response?.isNotEmpty() == true) {
                    result = JSONObject(response)
                }
                var status = -1
                if (result?.has("status") == true) {
                    status = result.getInt("status")
                }
                var strMessage = "Error"
                if (result?.has("message") == true) {
                    strMessage = result.getString("message")
                }
                if (status == 200) {
                    showLoading(false)

                    activity?.runOnUiThread {
                        AppConfig.livenessFaceListener?.onCallbackLiveness(LivenessModel(faceImage = faceImage))
                        AppPreferenceUtils(requireContext()).setRegisterFace(true)
                        onBackFragment()
                    }

                } else {
                    showLoading(false)
                    activity?.runOnUiThread {
                        AppConfig.livenessFaceListener?.onCallbackLiveness(
                            LivenessModel(
                                status = status, message = strMessage
                            )
                        )
                        onBackFragment()
                    }
                }
            } else {
                activity?.runOnUiThread {
                    AppConfig.livenessFaceListener?.onCallbackLiveness(
                        LivenessModel(
                            status = statusDevice, message = strMessageDevice
                        )
                    )
                    onBackFragment()
                }
            }
        }.start()

    }

    private fun showToastError(strError: String) {
        activity?.runOnUiThread {
            prbLoading.visibility = View.GONE
            mFrameMark?.visibility = View.GONE
            tvStatus.text = getString(R.string.fm_success)
            showDefaultDialog(strError)
        }
    }

    private fun updateUIWhenCapture() {
        slider.currentPagePosition = mStepScan - 1
        if (mStepScan == 1) {
            takePicture(1000)
        } else if (mStepScan == 2) {
            takePicture(1000)
        } else if (mStepScan == 3) {
            takePicture(1000)
        } else if (mStepScan == 4) {
            takePicture(1000)
        } else {
            cameraViewVideo.close()
            slider.visibility = View.GONE
            tvStatus.visibility = View.VISIBLE
            tvStatus.text = getString(R.string.fm_verifying)
            uploadFile()
            saveImage()
        }
    }

    private fun saveImage() {
        if (swSaveImage.isChecked) {
            for (item in lstImageInit) {
                saveItem(item)
            }
            for (item in lstImageRed) {
                saveItem(item)
            }
            for (item in lstImageGreen) {
                saveItem(item)
            }
            for (item in lstImageBlue) {
                saveItem(item)
            }
        }
    }

    private fun saveItem(imageString: String) {
        val decodedString: ByteArray =
            android.util.Base64.decode(imageString, android.util.Base64.NO_PADDING)
        val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        saveBitmapToDisk(bitmap)
    }

    private fun showDefaultDialog(strContent: String?) {
        activity?.let {
            val alertDialog = AlertDialog.Builder(it)
            alertDialog.apply {
                setTitle("Response")
                setMessage(strContent)
                setPositiveButton("OK") { _: DialogInterface?, _: Int ->
                }

            }.create().show()
        }

    }

    fun setFragmentManager(fragmentManager: FragmentManager) {
        mFragmentManager = fragmentManager
    }
}