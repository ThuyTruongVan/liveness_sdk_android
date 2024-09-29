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

internal class FaceDetector(private val faceBoundsOverlay: FaceBoundsOverlay) {

    //    companion object {
    private val TAG = "FaceDetector"
    private val MIN_FACE_SIZE = 0.15F
    private var mCameraView: CameraView? = null
    private var mFrameViewMax: View? = null
    private var mFrameViewMin: View? = null

    //    }
    private val mlKitFaceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(MIN_FACE_SIZE)
            .enableTracking()
            .build()
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
                    val exception =
                        IllegalStateException(
                            "Cannot run face detection. Make sure the face " +
                                    "bounds overlay is attached to the current window."
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
        val inputImage = InputImage.fromByteArray(dataImage, size.width, size.height, rotation, format)
        mlKitFaceDetector.process(inputImage)
            .addOnSuccessListener { faces ->
                synchronized(lock) {
                    isProcessing = false
                }
                if (faces.size > 0) {
                    for (face in faces) {
//                        if(checkFaceFrame(face)) {
//                        val faceBounds = faces.map { face ->
//                            Log.d("Thuytv", "----checkFaceFrame: " + checkFaceFrame(face.toFaceBounds(this)))
//                        }

//                        }

                        if (isSmiled) {
                            if (face.leftEyeOpenProbability != null && face.rightEyeOpenProbability != null) {
                                if (checkEyeBlink(face)) {
                                    onFaceDetectionResultListener?.onSuccess(face, faces.size)
                                    isSmiled = false
                                    onFaceDetectionResultListener?.onProcessing(false)
                                    isProcessing = true
                                } else {
//                                val faceBounds = faces.map { face -> face.toFaceBounds(this) }
//                                mainExecutor.execute { faceBoundsOverlay.updateFaces(faceBounds) }
                                }
                            }
                        } else {
                            if (face.smilingProbability != null && checkFaceFrame(face)) {
                                val smile = face.smilingProbability ?: 0.0f
                                if (smile > 0.95) {
                                    isSmiled = true
                                    onFaceDetectionResultListener?.onProcessing(true)
                                }
                            }
                        }
                    }
                } else {
//                    val faceBounds = faces.map { face -> face.toFaceBounds(this) }
//                    mainExecutor.execute { faceBoundsOverlay.updateFaces(faceBounds) }
                }
            }
            .addOnFailureListener { exception ->
                synchronized(lock) {
                    isProcessing = false
                }
                onError(exception)
            }
    }

    fun setFaceProcessing(isProcess: Boolean) {
        isProcessing = isProcess
    }

    fun setFrameImage(cameraView: CameraView, frameViewMax: View, frameViewMin: View) {
        mCameraView = cameraView
        mFrameViewMax = frameViewMax
        mFrameViewMin = frameViewMin
    }

    private fun checkFaceFrame(face: Face): Boolean {
        if (mFrameViewMax == null && mFrameViewMin == null) return true
        val boundingBox = face.boundingBox
        val left = boundingBox.left
        val right = boundingBox.right
        val top = boundingBox.top
        val bottom = boundingBox.bottom
        Log.d("Thuytv", "----bound--left: " + left + "---right: $right ---top: $top ----bottom: $bottom")
        val mLeft = mFrameViewMax?.left ?: 0
        val mRight = mFrameViewMax?.right ?: 0
        val mTop = mFrameViewMax?.top ?: 0
        val mBottom = mFrameViewMax?.bottom ?: 0
//        Log.d("Thuytv", "----mFrameView--mLeft: " + mLeft + "---mRight: $mRight ---mTop: $mTop ----mBottom: $mBottom")
        val isResultMax = left < mLeft && top > mTop && bottom < mBottom && right > mRight
        Log.d("Thuytv", "----checkFaceFramev----isResultMax: $isResultMax")

        val mLeftMin = mFrameViewMin?.left ?: 0
        val mRightMin = mFrameViewMin?.right ?: 0
        val mTopMin = mFrameViewMin?.top ?: 0
        val mBottomMin = mFrameViewMin?.bottom ?: 0
//        Log.d("Thuytv", "----mFrameView--mLeftMin: " + mLeftMin + "---mRightMin: $mRightMin ---mTopMin: $mTopMin ----mBottomMin: $mBottomMin")

        val isResultMin = left < mLeftMin && top > mTopMin && bottom > mBottomMin && right < mRightMin
        Log.d("Thuytv", "----checkFaceFramev----isResultMin: $isResultMin")


        return isResultMax || isResultMin
    }

//    private fun checkFaceFrame(bound: RectF): Boolean {
//        if (mCameraView == null || mFrameView == null) return false
//        val offsetHorizontal = mCameraView?.top?.toFloat() ?: 0f
//        bound.top = bound.top + offsetHorizontal
//        bound.bottom = bound.bottom + offsetHorizontal
//        val offset = 30F
//        val borderline = RectF(
//            mFrameView!!.left + offset,
//            mFrameView!!.top + offset,
//            mFrameView!!.right - offset,
//            mFrameView!!.bottom.toFloat()
//        )
//        return (bound.left > borderline.left && bound.top > borderline.top
//                && bound.right < borderline.right && bound.bottom < borderline.bottom)
//    }

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
        fun onProcessing(isSmile: Boolean) {}
        fun onFailure(exception: Exception) {}
    }

    private fun checkEyeBlink(face: Face): Boolean {
        val leftEyeOpenProbability: Float = face.leftEyeOpenProbability ?: 0f
        val rightEyeOpenProbability: Float = face.rightEyeOpenProbability ?: 0f
        Log.d("Thuytv", "-----left: $leftEyeOpenProbability ---right: $rightEyeOpenProbability")
        return leftEyeOpenProbability < 0.4 || rightEyeOpenProbability < 0.4
    }

}