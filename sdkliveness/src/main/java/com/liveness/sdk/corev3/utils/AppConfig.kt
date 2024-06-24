package com.liveness.sdk.corev3.utils

import android.annotation.SuppressLint
import android.view.View
import com.liveness.sdk.corev3.model.LivenessRequest

/**
 * Created by Thuytv on 16/04/2024.
 */
@SuppressLint("StaticFieldLeak")
internal object AppConfig {
    var livenessListener: CallbackLivenessListener? = null
    var livenessFaceListener: CallbackLivenessListener? = null
    var mLivenessRequest: LivenessRequest? = null
    var mOptionRequest: LivenessRequest? = null

    var mCustomView: View? = null
    var mActionView: View? = null
    var mProgressView: View? = null

    var KEY_BUNDLE_SCREEN = "KEY_BUNDLE_SCREEN"
    var TYPE_SCREEN_REGISTER_FACE = "TYPE_SCREEN_REGISTER_FACE"
    var INTENT_VALUE_BACK = "INTENT_VALUE_BACK"

    val encrypted_register_face = "BgGaNm0kc7FxDsKeUnM2SETGhPHiWVTwdsO8okdsrF1JnzUqxFHctLVn8SUSIHZ/uGo9mK9k5nqR\n" +
            "XXcKdxqWmIQVL/y0lmss48qcdXBJYcuNb73TCauQq/zudQn8Ch9wisPy/F992AFYCrK95HlVfyyk\n" +
            "nyDMTLE8krJXSrtjR+M="
    val encrypted_init_transaction = "RGEeL6Wf5b3X3P7SnV7YjSvkOL5HsXS9meJ/eVH6w7KdLkKnyNIRRHfMnVMVeuVj/JBiDy3vY8HD\n" +
            "B8z+8eZiKG2YhZDkmOG+lhTjNqt3qPVeseItcvJJPKXdGaABRJlIMHtoZ4dXgiEU4McsmNn6Uy58\n" +
            "RquRoK1lRRw3gZnFCGA="
    val encrypted_register_device = "W0ZquLO90Lp7bJ0pMGzsaAqLhAQtaOpJcSi9OyIybmm1+BRCtxq/okhi8qvvdiEJ3VkkfLtpXhcZ\n" +
            "H+/idyqoAq3d7K53Ejijj9frg0NBhCFGPeIdcd6wV8Dtlxo37h6i+CXrBDU0ZHRpezavAs3yGhVN\n" +
            "Xudhuyi08IZGKPADLWk="
    val encrypted_verify_face = "Dn+aSk3jIvSxgdXwTZuRORIt0EaSSuwssKSIHcTK39dDYalvBtS/cqt++z+/nhrBaTuWQsp9SWB5\n" +
            "zjOFTC+IIKZnmWMYfBKU1WPU0lTXqriQKUNjTg6xvE7ubJqJYjgQBDm8ZVIjrFFUuKhq9ubo8Nnl\n" +
            "mCOUWrovNXYSK9M/r7Y="
    val encrypted_check_live_ness = "lQe8yWK1KDcaZ7mT2go8Oq79RKOLwBBA4IMoVa3zlKl7BtYUoZ1akmQRPB8l66m+612DOYaPhw2q\n" +
            "qJY7uzpK+PQOZFUdx4r6y1tQOqTDUoEMFFE+/pWmXRmJsi2xjYGgC90OU53r8QeCCpUpVpew+ev5\n" +
            "XEnpX9VsfD8Nm/i3yZI="
    val encrypted_deviceId = "cUj1KxIwLt5xQ0RTyQJoaE6M0IlvSsZg5xlEfuTsCaaV/EaH+iGqlXpxUi5N0sLCyvXboyBMe53M\n" +
            "akkpngo6f5Nvik/P8iDddOq9gSEaQR9ubqRUR/EvO9rwr/JW4AI4idVBenbcg3qgJBtejhfNJGy2\n" +
            "Kni8TFI/zZoSW9E2BRQ="
    val encrypted_deviceOS = "jhS2z1eZMeqMagiwBYYXm/3yItmq07cROyGJzq66A1VZHZWDMS/a5PgXdDq9iskwvmoz2YCVMbGA\n" +
            "FZDjd0f9dydoJqqEBklwzLASFavIYTwXG3oyqZCc3x7xLqrg0BnzLTrZFWm+j6X37fyW0cZLeM5l\n" +
            "+HjxuvMAH0UEWtm73y8="
    val encrypted_device_name = "jhRuwePXUX/CDCu4WyDul/u3GHCJvNyJ4i9xVHBez4mlGW27SHgpVO3YMJeD64d2C0f2Cxrz2Umo\n" +
            "oa8eQydZmZm/+lipGhktMEX77GlEG97VBOagJDvodaxmh3xiabRAmRnXVn4CbyY/TDC9pE7blUB1\n" +
            "/tRR7NqjHjRShJ5oDyw="
    val encrypted_period = "D35pwUy+/HfYPh5DC7VYBX4U1qVFuXP+aDS9ao0/dDj/Ry3mBTo5cj+Nbe/yyiIAoicNe77LnMLD\n" +
            "9t0LAz/o3q6CWI6oDTX/Jm1Aezjciw+w5Y0P0ARrlQm+sTg7whgHiNTiBIuKJ5d3JAeHxEGB5PX7\n" +
            "uZujtmtzQwNCRU+Y8LY="
    val encrypted_secret = "goX/K8LnWJqtebGOqVNx5sI2KHWUXXMcljZQWY/ckMOnuY1gLr1DQiPR6llxbzBEy0oKjQ2AsOv9\n" +
            "SfpB424GLppWTVyDq3V0459E6S9v19ZD/vtANX/SN/PgvqL3ZMsdBwIN5mVktp87BaL62Ft/5aWi\n" +
            "inEi/1+rt+Nt1HzUZtI="
    val encrypted_face_image = "HvTO1vKWQcCYHmFFqjWa6avlgeHVl5TbQHiugmSpFW1UOj9Iw5AE5dDNCvp9dE4UjctTyLwy2zIs\n" +
            "LO8nbWNBe66B090VMyL7jiXFyAN4MPzhZNY6CMQQnt9lgJO2KpsvytBPiAMJB+qLrMdveWFxcJBd\n" +
            "43HaSGzCIxv/ou5S7Ag="
    val encrypted_device_id = "gxgmn+yZjzLxPvaG99symKqoo2OJ3c5KrzNNp738BncHJgvrAfOGAi4iI8wU4LPiRYbBZZOXdVXD\n" +
            "3S7d5sov2NwVQ0dPNNycTVKZf88tVxtC5Va7XqdSeYs2gK/0975nhtFf9bchwnQI5tdU96o+GtCz\n" +
            "G9DTyv9TN+W3XTwnS+I="
    val encrypted_totp = "Q1YD9POAkOR1SYKpjHQOsp9Z9SoC40ny07lClxo8NqDArjFyQKwJFNcqx5KeEsb9SBr07lr1RZcg\n" +
            "CrhkeyL5/iyoEBijGTD/n77A0GayF8D1tBBqBIOduuV83MA4KJWFewPkCUZ0vcizGTAurHHXkWut\n" +
            "dxByJ2gox2i/Q9u15d8="
    val encrypted_transaction_id = "gcwYAvSwelH9ygUCT8mI9eoRk17R6hmLLo460XH38CSzC55GgY6LiVVHmmArsU3CutJYyYqJbnUt\n" +
            "FL/l9xPXwT/PWcjdSqp3GxCFpYqMiRrJ7l7TlaJ+2G11DGcPjHZuXd2w80P3Og6WiA1rwT7lHy2N\n" +
            "EvpdU7NYCs/Ts7MDQkQ="
    val encrypted_image_live = "JVh6FqNWd140px3fScP31rBP2TV+1BfTm7Y3bWqTMMeeizjI0vMx9EiXlrC1cOaDwVxthZTQ+P28\n" +
            "pz9A7FfhUvF+76dnscQuEY5ap0zcI5dtSZVkdIX3PdBwd/MuuYyPMG37lLWaQfZCPHz7euuUnhyS\n" +
            "+zefk8ZZ2Y/bZNAATkA="
    val encrypted_color = "IYgZCYPmiZeZNlZKlzHA9+rQLJWU/3HgJPONfD4ITkAs0T9koNy23uHeYknKbefvJxMztzeTwbj5\n" +
            "9QHrAbvbpbdiWk3gJjeZqCfhEioiBrRZVYsIrJGcL63eF8G1AH/bgS0hOk5xOmHcgfmvoFrntv7v\n" +
            "pSpo8rnJIm280ZzqkTE="
    val encrypted_clientTransactionId = "YNPi6PVzZcwus/oGRpVL+1+X34Kfx/cRDlTMSHf0pQPC7KvLiulmYUL5iZatBv7gSGCnguMwhTjM\n" +
            "VUD865yoCz6CIym++gtguPn2fE+hb6tp8DBtHGjSiWo+w7Xy8u8YkU+sBs8e2yoZIj7iSBrfT3Jf\n" +
            "P5aMl0IzU4RG1YrIYso="

    val key_encrypted_register_face = "key_encrypted_a"
    val key_encrypted_init_transaction = "key_encrypted_b"
    val key_encrypted_register_device = "key_encrypted_c"
    val key_encrypted_verify_face = "key_encrypted_d"
    val key_encrypted_deviceId = "key_encrypted_e"
    val key_encrypted_deviceOS = "key_encrypted_f"
    val key_encrypted_device_name = "key_encrypted_g"
    val key_encrypted_period = "key_encrypted_h"
    val key_encrypted_secret = "key_encrypted_i"
    val key_encrypted_face_image = "key_encrypted_k"
    val key_encrypted_device_id = "key_encrypted_l"
    val key_encrypted_totp = "key_encrypted_m"
    val key_encrypted_transaction_id = "key_encrypted_n"
    val key_encrypted_image_live = "key_encrypted_s"
    val key_encrypted_color = "key_encrypted_x"
    val key_encrypted_clientTransactionId ="key_encrypted_z"
}