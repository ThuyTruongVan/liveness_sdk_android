package com.liveness.sdk.corev4.utils

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.OrtTrainingSession
import android.util.Log
import java.io.File
import java.nio.FloatBuffer
import java.nio.LongBuffer
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Collections
import kotlin.math.exp


class ORTTrainer{
    private var ortEnv: OrtEnvironment? = null
    private var ortTrainingSession: OrtTrainingSession? = null
    private var ortSession: OrtSession? = null

    constructor() {
        ortEnv = OrtEnvironment.getEnvironment()
    }

    public fun performInference(imgData: FloatBuffer, cacheDir: File, modelName: String): List<List<Float>> {
        if (ortSession == null) {
            val inferenceModelPath: Path = Paths.get(cacheDir.path, modelName)
            ortSession = ortEnv?.createSession(inferenceModelPath.toString())
        }
        ortEnv.use {
            val shape = longArrayOf(1, 3, 224, 224)
            val tensor = OnnxTensor.createTensor(ortEnv, imgData, shape)
            tensor.use {
                val output = ortSession?.run(Collections.singletonMap("input", tensor))
                output.use {
                    @Suppress("UNCHECKED_CAST")
                    Log.d("--hieudt", "$output")
                    val rawOutput = (output?.get(0)?.value) as Array<Array<Array<FloatArray>>>

                    val featuresList = mutableListOf<List<List<Float>>>()

                    for (batch in rawOutput) {
                        val batchList = mutableListOf<List<Float>>()
                        for (channel in batch) {
                            val channelList = mutableListOf<Float>()
                            for (floatArray in channel) {
                                channelList.addAll(floatArray.toList())
                            }
                            batchList.add(channelList)
                        }
                        featuresList.add(batchList)
                    }

                    val outputList: MutableList<Float> = ArrayList()
                    for (innerArray in featuresList) {
                        for (subArray in innerArray) {
                            for (value in subArray) {
                                outputList.add(value)
                            }
                        }
                    }
                    val out = listOf(outputList)
                    return out
                }

            }
        }
    }
}
