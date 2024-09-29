package com.liveness.sdk.corev4.facedetector

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.otaliastudios.cameraview.CameraView
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class FaceDetectorScan(private val faceBoundsOverlay: FaceBoundsOverlay) {

    //    companion object {
    private val TAG = "FaceDetector"
    private val MIN_FACE_SIZE = 0.15F
    private var mCameraView: CameraView? = null
    private var mFrameViewMax: View? = null

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
    private var isSmiled = false

    init {
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
                for (face in faces) {
//                        if(checkFaceFrame(face)) {
                    val faceBounds = faces.map { face ->
//                            val result = checkFaceFrame(face.toFaceBounds(this))
//                        val result = checkFaceAvailable(face.toFaceBounds(this))
//                        val resultCenter = checkFaceCenter(face)
//                        onFaceDetectionResultListener?.onProcessing(result && resultCenter)
                        val rectF = face.toFaceBounds(this)

                        if (mFrameViewMax == null) {
                            return@map
                        }
                        val faceOutFrame = isFaceOut(rectF)
                        if (faceOutFrame) {
                            onFaceDetectionResultListener?.onFaceStatus(2, null)
                            return@map
                        }
                        val faceTooSmall = faceSmallOrBig(rectF, true, mFrameViewMax!!)
                        if (faceTooSmall) {
                            onFaceDetectionResultListener?.onFaceStatus(0, percent)
                            return@map
                        }
                        val faceTooBig = faceSmallOrBig(rectF, false, mFrameViewMax!!)
                        if (faceTooBig) {
                            onFaceDetectionResultListener?.onFaceStatus(1, null)
                            return@map
                        }
                        val resultCenter = checkFaceCenter(face)
                        if (!resultCenter) {
                            onFaceDetectionResultListener?.onFaceStatus(3, null)
                            return@map
                        }
                        onFaceDetectionResultListener?.onProcessing(true)
                    }

//                        }

//                        if (isSmiled) {
//                            if (face.leftEyeOpenProbability != null && face.rightEyeOpenProbability != null) {
//                                if (checkEyeBlink(face)) {
//                                    onFaceDetectionResultListener?.onSuccess(face, faces.size)
//                                    isSmiled = false
//                                    onFaceDetectionResultListener?.onProcessing(false)
//                                    isProcessing = true
//                                } else {
////                                val faceBounds = faces.map { face -> face.toFaceBounds(this) }
////                                mainExecutor.execute { faceBoundsOverlay.updateFaces(faceBounds) }
//                                }
//                            }
//                        } else {
//                            if (face.smilingProbability != null && checkFaceFrame(face)) {
//                                val smile = face.smilingProbability ?: 0.0f
//                                if (smile > 0.95) {
//                                    isSmiled = true
//                                    onFaceDetectionResultListener?.onProcessing(true)
//                                }
//                            }
//                        }
                }
            } else {
//                    val faceBounds = faces.map { face -> face.toFaceBounds(this) }
//                    mainExecutor.execute { faceBoundsOverlay.updateFaces(faceBounds) }
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

    private val minFacePercent: Int = 50
    private val maxFacePercent: Int = 95
    private var percent = 0

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
        val offset = 30F
        val borderline = RectF(
            mFrameViewMax!!.left.toFloat(),
            mFrameViewMax!!.top - offset,
            mFrameViewMax!!.right.toFloat(),
            mFrameViewMax!!.bottom - offset
        )

        return (bound.left < borderline.left || bound.top < borderline.top || bound.right > borderline.right || bound.bottom > borderline.bottom)
    }

    private val eulerDescartes = 6f

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

//    private fun Face.toFaceBounds(frame: Frame): RectF {
//        val reverseDimens = frame.rotation == 90 || frame.rotation == 270
//        val width = if (reverseDimens) frame.size.height else frame.size.width
//        val height = if (reverseDimens) frame.size.width else frame.size.height
//        val scaleX = faceBoundsOverlay.width.toFloat() / width
//        val scaleY = faceBoundsOverlay.height.toFloat() / height
//
//        val isFrontLens = frame.lensFacing == LensFacing.FRONT
//        val flippedLeft = if (isFrontLens) width - boundingBox.right else boundingBox.left
//        val flippedRight = if (isFrontLens) width - boundingBox.left else boundingBox.right
//
//        val scaledLeft = scaleX * flippedLeft
//        val scaledTop = scaleY * boundingBox.top
//        val scaledRight = scaleX * flippedRight
//        val scaledBottom = scaleY * boundingBox.bottom
//        val scaledBoundingBox = RectF(scaledLeft, scaledTop, scaledRight, scaledBottom)
//
////        return FaceBounds(
////            trackingId,
////            scaledBoundingBox
////        )
//        return RectF(scaledLeft, scaledTop, scaledRight, scaledBottom)
//    }

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