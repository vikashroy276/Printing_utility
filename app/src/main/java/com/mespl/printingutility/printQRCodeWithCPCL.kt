package com.mespl.printingutility

import android.content.Context
import android.graphics.Bitmap
import com.zebra.sdk.comm.BluetoothConnection
import com.zebra.sdk.printer.PrinterLanguage
import com.zebra.sdk.printer.ZebraPrinterFactory
import java.io.File
import java.io.FileOutputStream

fun printQRCodeWithCPCL(printerMacAddress: String, serialNumber: String, qrBitmap: Bitmap, context: Context) {
    try {
        // Establish Bluetooth connection
        val connection = BluetoothConnection(printerMacAddress)
        connection.open()

        // Get Zebra printer instance
        val printer = ZebraPrinterFactory.getInstance(connection)
        // Save bitmap to a temporary PNG file
        val file = File(context.cacheDir, "qr_code.png")
        FileOutputStream(file).use { out ->
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        printer.printImage(file.absolutePath, 0, 0, qrBitmap.width, qrBitmap.height, false)

        Thread.sleep(500) // Wait for data to be sent
        connection.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}