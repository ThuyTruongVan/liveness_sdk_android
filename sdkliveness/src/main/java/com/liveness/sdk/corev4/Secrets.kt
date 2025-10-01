package com.liveness.sdk.corev4

class Secrets {

    // Method calls will be added by gradle task hideSecret
    // Example : external fun getWellHiddenSecret(packageName: String): String

    companion object {
        init {
//            try {
//                System.loadLibrary("secrets16k")
//            } catch (e: UnsatisfiedLinkError) {
//                System.loadLibrary("secrets4k")
//            }
            System.loadLibrary("secrets")
        }
    }

    external fun getAAF(packageName: String): String
}