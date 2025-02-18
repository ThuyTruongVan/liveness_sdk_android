package com.liveness.sdk.corev4

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import com.google.mlkit.vision.face.Face
import com.liveness.sdk.corev4.api.HttpClientUtils
import com.liveness.sdk.corev4.facedetector.EllipseView
import com.liveness.sdk.corev4.facedetector.FaceDetectorScan
import com.liveness.sdk.corev4.facedetector.Frame
import com.liveness.sdk.corev4.facedetector.LensFacing
import com.liveness.sdk.corev4.model.ImageResult
import com.liveness.sdk.corev4.model.LivenessModel
import com.liveness.sdk.corev4.model.VerifyLevel
import com.liveness.sdk.corev4.slider.SliderAdapter
import com.liveness.sdk.corev4.slider.SliderView
import com.liveness.sdk.corev4.utils.AppConfig
import com.liveness.sdk.corev4.utils.AppPreferenceUtils
import com.liveness.sdk.corev4.utils.AppUtils
import com.liveness.sdk.corev4.utils.DialogUtils
import com.liveness.sdk.corev4.utils.InformationDialogListener
import com.liveness.sdk.corev4.utils.TotpUtils
import com.nimbusds.jose.shaded.gson.Gson
import com.otaliastudios.cameraview.CameraException
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Engine
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.size.SizeSelectors
import org.json.JSONObject
import java.io.ByteArrayOutputStream
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
    private var mFrameMark: EllipseView? = null
    private lateinit var mFrameImageMax: ImageView

    private lateinit var toolbar: LinearLayout
    private lateinit var btBack: ImageView
    private lateinit var slider: SliderView
    private lateinit var rlVideo: ConstraintLayout
    private lateinit var faceAnim: LottieAnimationView
    private lateinit var loadingAnim: LottieAnimationView
    private lateinit var endAnim: LottieAnimationView
//    private lateinit var test: TextView

    private var mFaceDetector: FaceDetectorScan? = null
    private var mStepScan = 0

    private var permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    private val listColor: ArrayList<Long> = arrayListOf()

    private lateinit var sliderAdapter: SliderAdapter
    private val mHandler = Handler(Looper.getMainLooper())
    private lateinit var mCaptureRunnable: Runnable
    private lateinit var mSuccessRunnable: Runnable
    private lateinit var mBackRunnable: Runnable
    private var typeScreen: String? = null
    private var mFragmentManager: FragmentManager? = null
    private var mImageList: MutableList<String> = ArrayList()
    private var mImagePathList: MutableList<String> = ArrayList()
    private val listColorDefault: ArrayList<Long> =
        arrayListOf(0xFFFF0000L, 0xFF00FF00L, 0xFF0000FFL)
    private var isInit = false
    private var mCount: Float? = 1.0f
    private var mTransactionId: String? = null
    private var isProcess: Boolean = false
    private var isRunning = true
    private var mAngle = 260f
    private val mErrorListener = object : InformationDialogListener {
        override fun onPositiveClick() {
            cameraViewVideo.open()
            faceAnim.visibility = View.VISIBLE
            loadingAnim.visibility = View.GONE
            loadingAnim.progress = 0f
            endAnim.visibility = View.INVISIBLE
            endAnim.progress = 0f
        }

        override fun onNegativeClick() {
            activity?.finish()

        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fm_fragment_face_scan_fm, container, false)
        cameraViewVideo = view.findViewById(R.id.camera_view_video)
        prbLoading = view.findViewById(R.id.pbLoading)
        mFrameMark = view.findViewById(R.id.frMark)
        mFrameImageMax = view.findViewById(R.id.imv_frame_face)
        tvStatus = view.findViewById(R.id.tvStatus)
        slider = view.findViewById(R.id.imageSlider)
        toolbar = view.findViewById(R.id.llToolbar)
        btBack = view.findViewById(R.id.ivBack)
        rlVideo = view.findViewById(R.id.rlVideo)
        faceAnim = view.findViewById(R.id.faceAnim)
        loadingAnim = view.findViewById(R.id.loadingAnim)
        endAnim = view.findViewById(R.id.endAnim)
//        test = view.findViewById(R.id.tvTest)
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
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    AppConfig.livenessListener?.onCallbackLiveness(
                        LivenessModel(status = 6666)
                    )
                    onBackFragment()
                    Log.d("back press", "++++++")
                }
            })
        initRunnable()
        initCamera(view)
        if (checkPermissions()) {
            cameraViewVideo.open()
        } else {
            requestPermissions()
        }
        setScreenBrightness(1f)
//        LottieCompositionFactory.fromRawRes(context, R.raw.anim_3)
//            .addListener { composition: LottieComposition? ->
//                endAnim.setComposition(
//                    composition!!
//                )
//            }
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (AppConfig.mLivenessRequest?.offlineMode == true) {
            if (AppConfig.mLivenessRequest?.dataConfig?.randomColor != null) {
                initListColor(AppConfig.mLivenessRequest?.dataConfig?.randomColor!!)
            } else {
                showToast("Data config fail")
                onBackFragment()
            }
            if (AppConfig.mLivenessRequest?.dataConfig?.randomFrame != null) {
                if (AppConfig.mLivenessRequest?.dataConfig?.randomFrame!! < 60) {
                    AppConfig.mLivenessRequest?.dataConfig?.randomFrame = 60
                }
                if (AppConfig.mLivenessRequest?.dataConfig?.randomFrame!! > 240) {
                    AppConfig.mLivenessRequest?.dataConfig?.randomFrame = 240
                }
                mCount = AppConfig.mLivenessRequest?.dataConfig?.randomFrame!!.div(60f)
            } else {
                showToast("Data config fail")
                onBackFragment()
            }
            isInit = true
        } else {
            if (typeScreen == AppConfig.TYPE_SCREEN_REGISTER_FACE) {
                initListColor(0)
                isInit = true
            } else {
                initAttemp()
            }
        }
    }

    private fun initRunnable() {
        mCaptureRunnable = Runnable {
            cameraViewVideo.takePictureSnapshot()
        }

    }

    private fun getColor(color: Int): Long {
        var result = listColorDefault.last()
        if (color > listColorDefault.size - 1) {
            listColorDefault.removeAt(listColorDefault.size - 1)
        } else {
            result = listColorDefault[color]
            listColorDefault.removeAt(color)
        }
        return result
    }

    private fun initListColor(color: Int) {
        listColor.clear()
        listColor.add(0x00000000L)
        listColor.add(getColor(color))
        if (AppConfig.mLivenessRequest?.colorConfig != null) {
            if (checkColor() && AppConfig.mLivenessRequest?.colorConfig!!.size >= 2) {
                AppConfig.mLivenessRequest?.colorConfig?.apply {
                    for (i in indices) {
                        listColor.add(this[i].removePrefix("#").toLong(16))
                        if (listColor.size >= 4) break
                    }
                }
            } else {
                showToast("color config not math")
                onBackFragment()
            }

        } else {
//            listColor.addAll(listColorDefault)
        }
        sliderAdapter = SliderAdapter()
        sliderAdapter.renewItems(listColor)
        slider.setSliderAdapter(sliderAdapter)
    }


    private fun checkColor(): Boolean {
        AppConfig.mLivenessRequest?.colorConfig?.forEach {
            if (!isValidColor(it.removePrefix("#").toLong(16))) return false
        }
        return true
    }

    private fun isValidColor(color: Long): Boolean {
        return color in 0x00000000L..0xFFFFFFFFL
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
        cameraViewVideo.facing = lensFacing
        cameraViewVideo.engine = Engine.CAMERA2
        cameraViewVideo.setLifecycleOwner(this)
        cameraViewVideo.setPreviewStreamSize(SizeSelectors.minWidth(1560))

        mFaceDetector = FaceDetectorScan(
            view.findViewById(R.id.faceBoundsOverlay),
            AppConfig.mLivenessRequest?.verifyLevel ?: VerifyLevel.MEDIUM
        )
        mFaceDetector?.setFrameImage(cameraViewVideo, mFrameImageMax)
        mFaceDetector?.setonFaceDetectionFailureListener(object :
            FaceDetectorScan.OnFaceDetectionResultListener {
            override fun onSuccess(faceBounds: Face, faceSize: Int) {
                super.onSuccess(faceBounds, faceSize)
            }

            override fun onFaceStatus(status: Int, percent: Int?) {
                if (!isInit || !isAdded || isProcess) return
                restartSection()
                when (status) {
                    0 -> { // small
                        tvStatus.visibility = View.VISIBLE
                        tvStatus.text = getString(R.string.fm_come_closer)
                        prbLoading.visibility = View.GONE
                        mFrameMark?.errorView()
//                        percent?.apply {
//                            prbLoading.setProgress(percent, true)
//                        }
                    }

                    1 -> { // big
                        tvStatus.visibility = View.VISIBLE
                        tvStatus.text = getString(R.string.fm_move_face_farther)
                        prbLoading.visibility = View.GONE
                        mFrameMark?.errorView()
                    }

                    2 -> { // face out
                        tvStatus.visibility = View.VISIBLE
                        tvStatus.text = getString(R.string.fm_face_center_frame)
                        prbLoading.visibility = View.GONE
                        mFrameMark?.errorView()
                    }

                    3 -> { // face euler fail
                        tvStatus.visibility = View.VISIBLE
                        tvStatus.text = getString(R.string.fm_look_straight)
                        prbLoading.visibility = View.GONE
                        mFrameMark?.errorView()
                    }

                    4 -> { // no face
                        tvStatus.visibility = View.VISIBLE
                        tvStatus.text = getString(R.string.fm_face_out_frame)
                        prbLoading.visibility = View.GONE
                        mFrameMark?.errorView()
                    }

                    5 -> { // many face
                        tvStatus.visibility = View.VISIBLE
                        tvStatus.text = getString(R.string.fm_face_many)
                        prbLoading.visibility = View.GONE
                        mFrameMark?.errorView()
                    }

                    else -> {
                        showKeepDevice()
                    }
                }
            }

            override fun onProcessing(isFace: Boolean) {
                super.onProcessing(isFace)
                if (!isInit || isProcess) return
                if (isFace) {
                    if (mStepScan == 0) {
                        mStepScan = 1
                        showKeepDevice()
//                        Handler(Looper.getMainLooper()).post {
//                            test.visibility = View.INVISIBLE
//                        }
                        takePicture(500)
                        mSessionId = UUID.randomUUID().toString()
                    }
                } else {
                    restartSection()
                }
            }

        })


        cameraViewVideo.addCameraListener(object : CameraListener() {

            override fun onPictureTaken(result: PictureResult) {
                super.onPictureTaken(result)
                result.data.let {
//                    Handler(Looper.getMainLooper()).post {
//                        test.visibility = View.VISIBLE
//                    }
                    val mImage: String = Base64.encodeToString(it.scaleImage(), Base64.NO_PADDING)
                    Log.d("++++", "------onPictureTaken--mStepScan: $mStepScan")
                    val index = mStepScan - 1
                    if (index < 0) return
                    Log.d("++++", "------onPictureTaken--mStepScan: $index")
                    mImageList.add(index, mImage)
                    Log.d("++++", "------onPictureTaken--mStepScan: ${mImageList.size}")
                    if (mStepScan <= listColor.size) {
                        mStepScan++
                        updateUIWhenCapture(true)
                    } else {
                        updateUIWhenCapture(false)
                    }
                }

            }

            override fun onCameraError(exception: CameraException) {
                super.onCameraError(exception)
                Log.d("++++onCameraError", exception.message ?: "")
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
        mImageList.clear()
        mSessionId = ""
        tvStatus.visibility = View.GONE
        prbLoading.visibility = View.GONE
        slider.visibility = View.GONE
        slider.currentPagePosition = 0
        mHandler.removeCallbacks(mCaptureRunnable)
        mFrameMark?.defaultView()
    }

    private fun showKeepDevice() {
        tvStatus.visibility = View.VISIBLE
        tvStatus.text = getString(R.string.fm_keep_face)
        prbLoading.visibility = View.GONE
        if (typeScreen != AppConfig.TYPE_SCREEN_REGISTER_FACE) {
            slider.visibility = View.VISIBLE
//            mFrameMark?.loadingViewSemi((mCount!! * 1000).toLong())
        }
    }

    private fun saveBase64Images() {
        val executor = Executors.newFixedThreadPool(if (mImageList.size > 2) 4 else 2)
        val handler = Handler(Looper.getMainLooper())
        var exceptionOccurred: Exception? = null

        for (base64Image in mImageList) {
            executor.execute {
                try {
                    val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    val path = saveImageToInternalStorage(bitmap)
                    synchronized(mImagePathList) { mImagePathList.add(path) }

                } catch (e: Exception) {
                    exceptionOccurred = e
                }
            }
        }
        executor.shutdown()
        Thread {
            while (!executor.isTerminated) {
            }
            Log.d("++++", "------saved image--- $mImagePathList")
            handler.post {
                uploadFile()
            }
        }.start()
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val cachePath = File(requireContext().cacheDir, "images")
        if (!cachePath.exists()) {
            val created = cachePath.mkdirs()
            if (!created) {
                Log.d("++++", "------can't create folder---: ${cachePath.absolutePath}")
                showToast("Error")
                onBackFragment()
            }
        }
        val fileName = "ImageScan" + System.currentTimeMillis() + ".jpg"
        val file = File(cachePath, fileName)
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            bitmap.recycle()
            return file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
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
        resetScreenBrightness()
        mFaceDetector?.setFaceProcessing(false)
        mFaceDetector?.shutDown()
        cameraViewVideo.destroy()
        isRunning = false
        mHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun resetScreenBrightness() {
        val window = requireActivity().window
        val layoutParams = window.attributes
        layoutParams.screenBrightness = -1f
        window.attributes = layoutParams
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

    private fun setScreenBrightness(screenBrightness: Float) {
        val layoutParams = activity?.window?.attributes
        layoutParams?.screenBrightness = screenBrightness
        activity?.window?.attributes = layoutParams
    }

    private fun uploadFace() {
        var imageB64 = ""
        Log.d("++++lstImageInit", mImageList.size.toString())
        for (item in mImageList) {
            imageB64 = item
        }
        registerFace(imageB64)
    }

    private fun uploadFile() {
        faceAnim.visibility = View.GONE
        faceAnim.clearAnimation()
        loadingAnim.visibility = View.VISIBLE
        if (mImageList.size >= 4) {
            callApiUploadSession(mImageList[1], mImageList[0], mImageList[2], mImageList[3])
        } else if (mImageList.size >= 2) {
            callApiUploadSession(mImageList[1], mImageList[0], null, null)
        }

    }

    private fun callApiUploadSession(
    ) {
        prbLoading.visibility = View.VISIBLE
        if (AppConfig.mLivenessRequest?.offlineMode == true) {
            AppConfig.livenessListener?.onCallbackLiveness(
                LivenessModel(
                    imageResult = getImageResult(),
//                    imgTransparent = imageB64,
//                    imgRed = image2B64,
//                    imgGreen = image3B64,
//                    imgBlue = image4B64
                )
            )
            onBackFragment()
        } else {
//            getTOTP(imageB64, image2B64, image3B64, image4B64)
        }
    }

    private fun callApiUploadSession(
        imageB64: String, image2B64: String?, image3B64: String?, image4B64: String?
    ) {
//        prbLoading.visibility = View.VISIBLE
        resetScreenBrightness()
        if (AppConfig.mLivenessRequest?.offlineMode == true) {
            AppConfig.livenessListener?.onCallbackLiveness(LivenessModel(imageResult = getImageResult()))
//            if (activity is FaceMatchActivity) {
            onBackFragment()
//            }
        } else {
            getTOTP(imageB64, image2B64, image3B64, image4B64)
        }
    }

    private fun getImageResult(): List<ImageResult> {
        val result: MutableList<ImageResult> = mutableListOf()
        for (i in 0 until mImageList.size) {
            val imageResult = ImageResult(listColor[i], mImageList[i], getColorString(listColor[i]))
            if (AppConfig.mLivenessRequest?.isSaveImage == true && mImagePathList.size > i) {
                imageResult.imagePath = mImagePathList[i]
            }
            result.add(imageResult)
        }
        return result
    }

    private fun getColorString(color: Long): String {
        return when (color) {
            0xFFFF0000L -> {
                "r"
            }

            0xFF00FF00L -> {
                "g"
            }

            0xFF0000FFL -> {
                "b"
            }

            else -> {
                ""
            }
        }
//        return String.format("#%08X", color or 0xFF000000L)
    }


    private fun getTOTP(
        imageB64: String, image2B64: String?, image3B64: String?, image4B64: String?
    ) {
//        showLoading(true)
//        mAngle = Random.nextInt(180, 300).toFloat()
//        mFrameMark?.loadingViewPrepare(Random.nextLong(3000, 5001), mAngle)

        Thread {

            val tOTP = TotpUtils(requireContext()).getTotp()

            if (tOTP.isEmpty() || tOTP == "-1") {
//                AppConfig.livenessListener?.onCallbackLiveness(LivenessModel(status = -1, message = ""))
                showToast("TOTP null")
            } else {
                if (mTransactionId == null) {
                    initTransaction(
                        tOTP,
                        AppConfig.mLivenessRequest?.clientTransactionId,
                        imageB64,
                        image2B64,
                        image3B64,
                        image4B64
                    )
                } else {
                    checkLiveNessFlash(
                        tOTP, mTransactionId!!, imageB64, image2B64, image3B64, image4B64
                    )
                }

            }
        }.start()
    }

    private fun initTransaction(
        tOTP: String,
        readCardId: String?,
        imageB64: String,
        image2B64: String?,
        image3B64: String?,
        image4B64: String?
    ) {
        val response = HttpClientUtils.instance?.initTransaction(requireContext(), readCardId)
        if (!isRunning) return
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
        image2B64: String?,
        image3B64: String?,
        image4B64: String?
    ) {
        val response = HttpClientUtils.instance?.checkLiveNessFlashV2(
            requireContext(), tOTP, transactionID, imageB64, image2B64, image3B64, image4B64
        )
        if (!isRunning) return
        activity?.runOnUiThread {
            prbLoading.progress = 100
        }
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
            liveNessModel.imageResult = getImageResult()
            activity?.runOnUiThread {
                if (liveNessModel.data?.faceMatchingResult != 1) {
                    var title: String
                    var message: String
                    when (liveNessModel.code) {
                        AppConfig.code_face_error -> {
                            title = getString(R.string.fm_face_math_fail_title)
                            message = getString(R.string.fm_face_math_fail_message)
                        }

                        AppConfig.code_liveness_error -> {
                            title = getString(R.string.fm_face_live_fail_title)
                            message = getString(R.string.fm_face_live_fail_message)
                        }

                        AppConfig.code_accessories_error -> {
                            title = getString(R.string.fm_face_accessories_fail_title)
                            message = getString(R.string.fm_face_accessories_fail_message)
                        }

                        AppConfig.code_quality_error -> {
                            title = getString(R.string.fm_face_quality_fail_title)
                            message = getString(R.string.fm_face_quality_fail_message)
                        }

                        else -> {
                            title = getString(R.string.fm_face_math_fail_title)
                            message = getString(R.string.fm_face_math_fail_message)
                        }
                    }
                    DialogUtils.showConfirmDialog(
                        requireActivity(),
                        title,
                        message,
                        getString(R.string.fm_retry),
                        getString(R.string.fm_skip),
                        mErrorListener
                    )
                    loadingAnim.visibility = View.GONE
                } else {
                    activity?.runOnUiThread {
                        loadingAnim.clearAnimation()
                        loadingAnim.visibility = View.GONE
                        endAnim.visibility = View.VISIBLE
                        endAnim.playAnimation()
                    }

                    mBackRunnable = Runnable {
                        AppConfig.livenessListener?.onCallbackLiveness(liveNessModel)
                        onBackFragment()
                    }

//                    mSuccessRunnable = Runnable {
//                        mHandler.postDelayed(mBackRunnable, 300)
//                    }
                    mHandler.postDelayed(mBackRunnable, 2100)

                }
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

    private fun showSuccessView() {

    }

    private fun initAttemp() {
        showLoading(true)
        Thread {
            try {
                val response = HttpClientUtils.instance?.initTransaction(
                    requireContext(), AppConfig.mLivenessRequest?.clientTransactionId
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
                if (result?.has("data") == true) {
                    mTransactionId = result.getString("data")
                }
                if (!isRunning) return@Thread
                if (status == 200) {
                    val response =
                        HttpClientUtils.instance?.initAttemp(requireContext(), mTransactionId!!)
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
                    if (!isRunning) return@Thread
                    if (status == 200) {
                        var data: JSONObject? = null
                        if (result?.has("data") == true) {
                            data = result.getJSONObject("data")
                        }
                        val color = data?.getInt("randomColor")
                        val fCount = data?.getInt("randomFrame")
                        if (fCount == null) {
                            this.mCount = 1.2f
                        } else {
                            mCount = fCount.div(60f)
                        }
                        color?.apply {
                            initListColor(this)
                            isInit = true
                        }
                        showLoading(false)
                    } else {
                        showLoading(false)
                        activity?.runOnUiThread {
                            AppConfig.livenessListener?.onCallbackLiveness(
                                LivenessModel(
                                    status = status, message = strMessage
                                )
                            )
                            onBackFragment()
                        }
                    }
                } else {
                    showLoading(false)
                    activity?.runOnUiThread {
                        AppConfig.livenessListener?.onCallbackLiveness(
                            LivenessModel(
                                status = status, message = strMessage
                            )
                        )
                        onBackFragment()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()

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
                    requireContext(), AppConfig.encrypted_register_device
                ), request
            )
            if (!isRunning) return@Thread
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
                if (!isRunning) return@Thread
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
                var data: String? = null
                if (result?.has("data") == true) {
                    data = result.getString("data")
                }
                if (status == 200) {
                    showLoading(false)
                    activity?.runOnUiThread {
                        AppConfig.livenessFaceListener?.onCallbackLiveness(
                            LivenessModel(
                                status = status, faceRegisterId = data, faceImage = faceImage
                            )
                        )
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

    private fun updateUIWhenCapture(isSlide: Boolean = true) {
        if (isSlide) slider.currentPagePosition = mStepScan - 1
        if (mStepScan <= listColor.size) {
            if (mStepScan == 2) {
                if (typeScreen != AppConfig.TYPE_SCREEN_REGISTER_FACE) {
                    takePicture((mCount!! * 1000L).toLong() + 500L)
                } else {
                    cameraViewVideo.close()
                    slider.visibility = View.GONE
                    tvStatus.visibility = View.VISIBLE
                    tvStatus.text = getString(R.string.fm_register_process)
                    uploadFace()
                }
            } else {
                takePicture(1500)
            }
        } else {
            cameraViewVideo.close()
            slider.visibility = View.GONE
            tvStatus.visibility = View.VISIBLE
            tvStatus.text = getString(R.string.fm_verifying)
            if (AppConfig.mLivenessRequest?.isSaveImage == true) {
                saveBase64Images()
            } else {
                uploadFile()
            }
        }
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

    fun ByteArray.scaleImage(): ByteArray {
        val stream = ByteArrayOutputStream()
        val bitmap = BitmapFactory.decodeByteArray(this, 0, this.size)
        val primitiveWidth = bitmap.width
        val primitiveHeight = bitmap.height
        var quality = AppConfig.mLivenessRequest?.dataConfig?.quality ?: 90
        if (quality > 100) {
            quality = 100
        }
        if (quality < 0) {
            quality = 50
        }
        var newWidth = if (AppConfig.mLivenessRequest?.offlineMode == true) {
            (primitiveWidth / 1.5f).toInt()
        } else {
            (primitiveWidth / 3f).toInt()
        }
        var newHeight = if (AppConfig.mLivenessRequest?.offlineMode == true) {
            (primitiveHeight / 1.5f).toInt()
        } else {
            (primitiveHeight / 3f).toInt()
        }
        AppConfig.mLivenessRequest?.dataConfig?.maxWidth?.let {
            if (it >= primitiveWidth) {
                newWidth = primitiveWidth
                newHeight = primitiveHeight
            } else {
                newWidth = it
                newHeight = (newWidth * primitiveHeight) / primitiveWidth
            }
        }
        val scaleBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        scaleBitmap.compress(Bitmap.CompressFormat.PNG, quality, stream)
        return stream.toByteArray()
    }

    fun setFragmentManager(fragmentManager: FragmentManager) {
        mFragmentManager = fragmentManager
    }
}