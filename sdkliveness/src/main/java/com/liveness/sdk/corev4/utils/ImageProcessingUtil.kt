package com.liveness.sdk.corev4.utils

import android.graphics.Bitmap
import android.graphics.Color
import java.nio.FloatBuffer

fun processImage(bitmap: Bitmap, buffer: FloatBuffer, offset: Int) {
    val width: Int = bitmap.getWidth()
    val height: Int = bitmap.getHeight()
    val stride: Int = width * height

    for (x in 0 until width) {
        for (y in 0 until height) {
            val color: Int = bitmap.getPixel(y, x)
            val index = offset + (x * height + y)
            buffer.put(index + stride * 0, ((Color.red(color).toFloat() / 255f) - 0.485f) / 0.229f)
            buffer.put(index + stride * 1, ((Color.green(color).toFloat() / 255f) - 0.456f) / 0.224f)
            buffer.put(index + stride * 2, ((Color.blue(color).toFloat() / 255f) - 0.406f) / 0.225f)
        }
    }
}

fun processBitmap(bitmap: Bitmap) : Bitmap {
    // This function processes the given bitmap by
    //   - cropping along the longer dimension to get a square bitmap
    //     If the width is larger than the height
    //     ___+_________________+___
    //     |  +                 +  |
    //     |  +                 +  |
    //     |  +        +        +  |
    //     |  +                 +  |
    //     |__+_________________+__|
    //     <-------- width -------->
    //        <----- height ---->
    //     <-->      cropped    <-->
    //
    //     If the height is larger than the width
    //     _________________________   ʌ            ʌ
    //     |                       |   |         cropped
    //     |+++++++++++++++++++++++|   |      ʌ     v
    //     |                       |   |      |
    //     |                       |   |      |
    //     |           +           | height width
    //     |                       |   |      |
    //     |                       |   |      |
    //     |+++++++++++++++++++++++|   |      v     ʌ
    //     |                       |   |         cropped
    //     |_______________________|   v            v
    //
    //
    //
    //   - resizing the cropped square image to be of size (3 x 224 x 224) as needed by the
    //     mobilenetv2 model.
    lateinit var bitmapCropped: Bitmap
    if (bitmap.getWidth() >= bitmap.getHeight()) {
        // Since height is smaller than the width, we crop a square whose length is the height
        // So cropping happens along the width dimesion
        val width: Int = bitmap.getHeight()
        val height: Int = bitmap.getHeight()

        // left side of the cropped image must begin at (bitmap.getWidth() / 2 - bitmap.getHeight() / 2)
        // so that the cropped width contains equal portion of the width on either side of center
        // top side of the cropped image must begin at 0 since we are not cropping along the height
        // dimension
        val x: Int = bitmap.getWidth() / 2 - bitmap.getHeight() / 2
        val y: Int = 0
        bitmapCropped = Bitmap.createBitmap(bitmap, x, y, width, height)
    } else {
        // Since width is smaller than the height, we crop a square whose length is the width
        // So cropping happens along the height dimesion
        val width: Int = bitmap.getWidth()
        val height: Int = bitmap.getWidth()

        // left side of the cropped image must begin at 0 since we are not cropping along the width
        // dimension
        // top side of the cropped image must begin at (bitmap.getHeight() / 2 - bitmap.getWidth() / 2)
        // so that the cropped height contains equal portion of the height on either side of center
        val x: Int = 0
        val y: Int = bitmap.getHeight() / 2 - bitmap.getWidth() / 2
        bitmapCropped = Bitmap.createBitmap(bitmap, x, y, width, height)
    }

    // Resize the image to be channels x width x height as needed by the mobilenetv2 model
    val width: Int = 224
    val height: Int = 224
    val bitmapResized: Bitmap = Bitmap.createScaledBitmap(bitmapCropped, width, height, false)

    return bitmapResized
}