package com.mespl.printingutility

import android.graphics.Bitmap
import com.zebra.sdk.graphics.internal.ZebraImageAndroid

fun bitmapToZplGF(bitmap: Bitmap): String {
    val width = bitmap.width
    val height = bitmap.height
    val bytesPerRow = (width + 7) / 8
    val totalBytes = bytesPerRow * height
    val sb = StringBuilder()
    sb.append("$totalBytes,$totalBytes,$bytesPerRow,")

    for (y in 0 until height) {
        var byte = 0
        var bitCount = 0
        for (x in 0 until width) {
            val pixel = bitmap.getPixel(x, y)
            val black = if ((pixel and 0xFF) < 128) 1 else 0
            byte = (byte shl 1) or black
            bitCount++
            if (bitCount == 8) {
                sb.append(String.format("%02X", byte))
                byte = 0
                bitCount = 0
            }
        }
        if (bitCount > 0) { // last partial byte
            byte = byte shl (8 - bitCount)
            sb.append(String.format("%02X", byte))
        }
    }

    return sb.toString()
}

