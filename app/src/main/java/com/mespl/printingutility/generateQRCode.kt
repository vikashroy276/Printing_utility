package com.mespl.printingutility

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

/**
 * Generates a QR code bitmap from a given string.
 *
 * This function takes a serial number (or any string), width, and height,
 * and encodes it into a QR code. The resulting QR code is returned as a Bitmap.
 *
 * @param serialNumber The string data to be encoded in the QR code.
 * @param width The desired width of the output QR code bitmap in pixels.
 * @param height The desired height of the output QR code bitmap in pixels.
 * @return A [Bitmap] object representing the QR code, or `null` if an error occurs during generation.
 */
fun generateQRCode(serialNumber: String, width: Int, height: Int): Bitmap? {
        try {
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                serialNumber,
                BarcodeFormat.QR_CODE,
                width,
                height
            )
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(
                        x,
                        y,
                        if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
                    )
                }
            }
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }