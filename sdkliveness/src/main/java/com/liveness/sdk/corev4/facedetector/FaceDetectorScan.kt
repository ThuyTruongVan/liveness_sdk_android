package com.liveness.sdk.corev4.facedetector

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.graphics.RectF
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.annotation.GuardedBy
import com.google.android.gms.common.util.concurrent.HandlerExecutor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import com.liveness.sdk.corev4.BuildConfig
import com.liveness.sdk.corev4.model.VerifyLevel
import com.otaliastudios.cameraview.CameraView
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


internal class FaceDetectorScan(
    private val faceBoundsOverlay: FaceBoundsOverlay,
    level: VerifyLevel = VerifyLevel.MEDIUM
) {

    //    companion object {
    private val TAG = "FaceDetector"
    private val MIN_FACE_SIZE = 0.15F
    private var mCameraView: CameraView? = null
    private var mFrameViewMax: View? = null
    private var minFacePercent: Int = 50
    private var maxFacePercent: Int = 94
    private var percent = 0
    private var offset = 10F
    private var eulerDescartes = 5f


    //    }
    private val mlKitFaceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
//            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(MIN_FACE_SIZE).enableTracking().build()
    )
    private var onFaceDetectionResultListener: OnFaceDetectionResultListener? = null
    private lateinit var faceDetectionExecutor: ExecutorService
    private val mainExecutor = HandlerExecutor(Looper.getMainLooper())
    private val lock = Object()

    @GuardedBy("lock")
    private var isProcessing = false

    fun enableProcessing() {
        synchronized(lock) {
            isProcessing = false
        }
    }

    init {
        when (level) {
            VerifyLevel.HIGH -> {
                minFacePercent = 55
                maxFacePercent = 90
                eulerDescartes = 6f
                offset = 0F
            }

            VerifyLevel.MEDIUM -> {
                minFacePercent = 50
                maxFacePercent = 94
                eulerDescartes = 9f
                offset = 10F
            }

            VerifyLevel.LOW -> {
                minFacePercent = 45
                maxFacePercent = 98
                eulerDescartes = 12f
                offset = 20F

            }
        }
        faceBoundsOverlay.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(view: View) {
                faceDetectionExecutor = Executors.newSingleThreadExecutor()
            }

            override fun onViewDetachedFromWindow(view: View) {
                if (::faceDetectionExecutor.isInitialized) {
                    faceDetectionExecutor.shutdown()
                }
            }
        })
    }

    fun setonFaceDetectionFailureListener(listener: OnFaceDetectionResultListener) {
        onFaceDetectionResultListener = listener
    }

    fun shutDown() {
        if (::faceDetectionExecutor.isInitialized) {
            faceDetectionExecutor.shutdown()
        }
    }

    fun process(frame: Frame) {
        synchronized(lock) {
            if (!isProcessing) {
                isProcessing = true
                if (!::faceDetectionExecutor.isInitialized) {
                    val exception = IllegalStateException(
                        "Cannot run face detection. Make sure the face " + "bounds overlay is attached to the current window."
                    )
                    onError(exception)
                } else {
                    faceDetectionExecutor.execute { frame.detectFaces() }
                }
            }
        }
    }

    fun Bitmap.toByteArray(): ByteArray {
        val stream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    fun ByteArray.toBitmap(): Bitmap {
        return BitmapFactory.decodeByteArray(this, 0, this.size)
    }

    private fun Frame.detectFaces() {
        val dataImage = data ?: return
        val inputImage =
            InputImage.fromByteArray(dataImage, size.width, size.height, rotation, format)
        mlKitFaceDetector.process(inputImage).addOnSuccessListener { faces ->
            synchronized(lock) {
                isProcessing = false
            }
            if (faces.size > 0) {
                if (faces.size == 1) {
                    val rectF = faces[0].toFaceBounds(this)
//                    Log.d("--hieudt", rectF.toString())
                    if (mFrameViewMax == null) {
                        return@addOnSuccessListener
                    }
                    if (BuildConfig.DEBUG) {
                        faceBoundsOverlay.updateFaces(listOf(FaceBounds(0, rectF)))
                    }
                    val faceTooSmall = faceSmallOrBig(rectF, true, mFrameViewMax!!)
                    if (faceTooSmall) {
                        onFaceDetectionResultListener?.onFaceStatus(0, percent)
                        return@addOnSuccessListener
                    }
                    val faceTooBig = faceSmallOrBig(rectF, false, mFrameViewMax!!)
                    if (faceTooBig) {
                        onFaceDetectionResultListener?.onFaceStatus(1, null)
                        return@addOnSuccessListener
                    }
                    val faceOutFrame = isFaceOut(rectF)
                    if (faceOutFrame) {
                        onFaceDetectionResultListener?.onFaceStatus(2, null)
                        return@addOnSuccessListener
                    }
                    val resultCenter = checkFaceCenter(faces[0])
                    if (!resultCenter) {
                        onFaceDetectionResultListener?.onFaceStatus(3, null)
                        return@addOnSuccessListener
                    }
                    onFaceDetectionResultListener?.onProcessing(true)
                } else {
                    onFaceDetectionResultListener?.onFaceStatus(5, null)
                }

            } else {
                onFaceDetectionResultListener?.onFaceStatus(4, null)
            }
        }.addOnFailureListener { exception ->
            synchronized(lock) {
                isProcessing = false
            }
            onError(exception)
        }
    }

    fun setFaceProcessing(isProcess: Boolean) {
        isProcessing = isProcess
    }

    fun setFrameImage(cameraView: CameraView, frameViewMax: View) {
        mCameraView = cameraView
        mFrameViewMax = frameViewMax
    }

    private fun checkFaceFrame(face: Face): Boolean {
        if (mFrameViewMax == null) return true
        val boundingBox = face.boundingBox
        val left = boundingBox.left
        val right = boundingBox.right
        val top = boundingBox.top
        val bottom = boundingBox.bottom
        Log.d(
            "Thuytv",
            "----bound--left: " + left + "---right: $right ---top: $top ----bottom: $bottom"
        )
        val mLeft = mFrameViewMax?.left ?: 0
        val mRight = mFrameViewMax?.right ?: 0
        val mTop = mFrameViewMax?.top ?: 0
        val mBottom = mFrameViewMax?.bottom ?: 0
        Log.d(
            "Thuytv",
            "----mFrameView--mLeft: " + mLeft + "---mRight: $mRight ---mTop: $mTop ----mBottom: $mBottom"
        )
        val isResultMax = left > mLeft && top > mTop && bottom < mBottom && right < mRight
        Log.d("Thuytv", "----checkFaceFramev----isResultMax: $isResultMax")

        return isResultMax
    }

    private fun checkFaceFrame(bound: RectF): Boolean {
        if (mCameraView == null || mFrameViewMax == null) return false
        val offsetHorizontal = mCameraView?.top?.toFloat() ?: 0f
        bound.top += offsetHorizontal
        bound.bottom += offsetHorizontal
        val offset = 30F
        val borderline = RectF(
            mFrameViewMax!!.left.toFloat(),
            mFrameViewMax!!.top - offset,
            mFrameViewMax!!.right.toFloat(),
            mFrameViewMax!!.bottom - offset
        )

        return (bound.left > borderline.left && bound.top > borderline.top && bound.right < borderline.right && bound.bottom < borderline.bottom)
    }

    private fun checkFaceAvailable(rectF: RectF): Boolean {
        if (mFrameViewMax == null) return false
        val faceTooSmall = faceSmallOrBig(rectF, true, mFrameViewMax!!)
        if (faceTooSmall) {
            Log.d("Thuytv", "------checkFaceAvailable--: face too SMALL")
        }
        val faceTooBig = faceSmallOrBig(rectF, false, mFrameViewMax!!)
        if (faceTooBig) {
            Log.d("Thuytv", "------checkFaceAvailable--: face too BIG")
        }
        val faceOutFrame = isFaceOut(rectF)
        if (faceOutFrame) {
            Log.d("Thuytv", "------checkFaceAvailable--: face out STANDARD FRAME")
        }
        return !faceTooSmall && !faceTooBig && !faceOutFrame
    }


    private fun faceSmallOrBig(
        faceSquare: RectF, checkSmall: Boolean, standardFrame: View
    ): Boolean {
        var res: Boolean
        val faceAcreage: Int = (faceSquare.width() * faceSquare.height()).toInt()
        val minFrame: Int = Math.min(standardFrame.width, standardFrame.height)
        val frameAcreage = minFrame * minFrame
        res =
            if (checkSmall) {
                percent =
                    ((10000 * faceAcreage).toFloat() / (minFacePercent * frameAcreage)).toInt()
                100 * faceAcreage < minFacePercent * frameAcreage
            } else {
                100 * faceAcreage > maxFacePercent * frameAcreage
            }
        return res
    }

    private fun isFaceOut(bound: RectF): Boolean {
        if (mCameraView == null || mFrameViewMax == null) return true
        val offsetHorizontal = mCameraView?.top?.toFloat() ?: 0f
        bound.top += offsetHorizontal
        bound.bottom += offsetHorizontal
        val borderline = RectF(
            mFrameViewMax!!.left.toFloat() - offset,
            mFrameViewMax!!.top - offset,
            mFrameViewMax!!.right.toFloat() + offset,
            mFrameViewMax!!.bottom + offset
        )
//        Log.d("border", "border$borderline")
//        Log.d("border", "face $bound")
        return (bound.left < borderline.left || bound.top < borderline.top || bound.right > borderline.right || bound.bottom > borderline.bottom)
    }


    private fun checkFaceCenter(face: Face): Boolean {
        if (face.headEulerAngleX < eulerDescartes && face.headEulerAngleY < eulerDescartes
            && face.headEulerAngleX > -eulerDescartes && face.headEulerAngleY > -eulerDescartes
            && face.headEulerAngleZ < eulerDescartes && face.headEulerAngleZ > -eulerDescartes
        ) {
            return true
        }
        return false
    }

    private fun Face.toFaceBounds(frame: Frame): RectF {
        val reverseDimens = frame.rotation == 90 || frame.rotation == 270
        val width = if (reverseDimens) frame.size.height else frame.size.width
        val height = if (reverseDimens) frame.size.width else frame.size.height
        val scaleX = (mCameraView?.width?.toFloat() ?: 0f) / width
        val scaleY = (mCameraView?.height?.toFloat() ?: 0f) / height
        val isFrontLens = frame.lensFacing == LensFacing.FRONT
        val flippedLeft = if (isFrontLens) width - boundingBox.right else boundingBox.left
        val flippedRight = if (isFrontLens) width - boundingBox.left else boundingBox.right
        val scaledLeft = scaleX * flippedLeft
        val scaledTop = scaleY * boundingBox.top
        val scaledRight = scaleX * flippedRight
        val scaledBottom = scaleY * boundingBox.bottom
        return RectF(scaledLeft, scaledTop, scaledRight, scaledBottom)
    }
    fun scaleRectF(rect: RectF, scale: Float): RectF {
        val centerX = rect.centerX()
        val centerY = rect.centerY()
        val halfWidth = rect.width() / 2 * scale
        val halfHeight = rect.height() / 2 * scale

        return RectF(
            centerX - halfWidth,
            centerY - halfHeight,
            centerX + halfWidth,
            centerY + halfHeight
        )
    }


    private fun onError(exception: Exception) {
        onFaceDetectionResultListener?.onFailure(exception)
        Log.e(TAG, "An error occurred while running a face detection", exception)
    }

    interface OnFaceDetectionResultListener {
        fun onSuccess(faceBounds: Face, faceSize: Int) {}
        fun onProcessing(isFace: Boolean) {}

        fun onFaceStatus(status: Int, percent: Int?) {}
        fun onFailure(exception: Exception) {}
    }

    private fun checkEyeBlink(face: Face): Boolean {
        val leftEyeOpenProbability: Float = face.leftEyeOpenProbability ?: 0f
        val rightEyeOpenProbability: Float = face.rightEyeOpenProbability ?: 0f
        Log.d("Thuytv", "-----left: $leftEyeOpenProbability ---right: $rightEyeOpenProbability")
        return leftEyeOpenProbability < 0.4 || rightEyeOpenProbability < 0.4
    }

}