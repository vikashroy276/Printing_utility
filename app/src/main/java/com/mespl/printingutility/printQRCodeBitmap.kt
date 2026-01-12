package com.mespl.printingutility

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.zebra.sdk.comm.BluetoothConnection
import com.zebra.sdk.comm.Connection
import com.zebra.sdk.printer.PrinterLanguage
import com.zebra.sdk.printer.ZebraPrinter
import com.zebra.sdk.printer.ZebraPrinterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("MissingPermission")
fun printQRCodeWithText(
    printerMacAddress: String?,
    qrBitmap: Bitmap,
    text: String,
    printerConnection: BluetoothConnection?,
    context: Context
): StateFlow<PrintState> {

    var connection: Connection? = null
    val stateFlow = MutableStateFlow<PrintState>(PrintState.Idle)
    // Start printing in background coroutine
    CoroutineScope(Dispatchers.IO).launch @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN) {
        try {
            // Emit loading state immediately
//            stateFlow.emit(PrintState.Loading)
            ToastController.showToast(context, "Printing...", false)
            Log.d("PrintQRCode", "Start printQRCodeWithText")

            // Open Bluetooth connection
//            connection = BluetoothConnection(printerMacAddress)
//            connection.open()
            val printer = ZebraPrinterFactory.getInstance(printerConnection)



            //  Check printer status
            val status = printer.currentStatus

            if (!status.isReadyToPrint) {
                val result = when {
                    status.isPaused -> PrintResult(false, "Printer is paused")
                    status.isHeadOpen -> PrintResult(false, "Printer head is open")
                    status.isPaperOut -> PrintResult(false, "Printer is out of paper")
                    status.isRibbonOut -> PrintResult(false, "Printer ribbon is out")
                    status.isHeadTooHot -> PrintResult(false, "Printer head too hot")
                    else -> PrintResult(
                        false,
                        "Printer not ready: ${status.numberOfFormatsInReceiveBuffer} jobs pending"
                    )
                }

                // Emit immediately on the current thread
                stateFlow.value = PrintState.Completed(result)

                // Launch UI updates asynchronously
                CoroutineScope(Dispatchers.Main).launch {
                    ToastController.showToast(context, result.message, true)
                    Handler(Looper.getMainLooper()).postDelayed({
                        ToastController.cancel()
                    }, 1500)
                    Log.d("test ", "1 ")

                }

                return@launch
            }

            // Build ZPL
            val zplData = buildZplInlineBitmap(qrBitmap, text, printer)

            //Write to printer
            try {
                printerConnection?.write(zplData.toByteArray())
                withContext(Dispatchers.Main) {
                    val successResult = PrintResult(true, "Print Successful")
                    stateFlow.emit(PrintState.Completed(successResult))
                    ToastController.showToast(context, successResult.message, true)
                    Handler(Looper.getMainLooper()).postDelayed({
                        ToastController.cancel()
                    }, 1500)


                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.d("test ", "2 ")
                    val failResult = PrintResult(false, "Print Failed: ${e.message ?: "Error"}")
                    stateFlow.emit(PrintState.Completed(failResult))
                    ToastController.showToast(context, failResult.message, true)
                    ToastController.cancel()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Log.d("test ", "3 ")
                val errorResult = PrintResult(false, e.message ?: "Error")
                stateFlow.emit(PrintState.Completed(errorResult))
                ToastController.showToast(context, errorResult.message, true)
                ToastController.cancel()

            }
        } finally {
            try {
                connection?.close()
            } catch (_: Exception) {
                Log.d("test ", "4 ")
            }
        }
    }

    // Return StateFlow immediately
    return stateFlow
}


fun buildZplInlineBitmap(
    qrBitmap: Bitmap,
    text: String,
    printer: ZebraPrinter
): String {
    val printerLanguage = printer.printerControlLanguage
    val maxWidth = when (printerLanguage) {
        PrinterLanguage.ZPL -> 832
        PrinterLanguage.CPCL -> 576
        else -> 600
    }

    val qrWidth = qrBitmap.width
    val qrX = (maxWidth - qrWidth) / 2
    val qrY = 4
    val textMargin = 50
    val verticalSpacing = -12
    val textY = qrY + qrBitmap.height + verticalSpacing
    val fieldBlockWidth = maxWidth - 2 * textMargin

    // Convert bitmap to ZPL ^GF inline
    val gfData = bitmapToZplGF(qrBitmap)

    return buildString {
        append("^XA") // Start label
        append("^FO$qrX,$qrY^GFA,$gfData^FS") // QR code
        append("^FO$textMargin,$textY") // text below QR
        append("^FB$fieldBlockWidth,2,0,C") // width, 2 lines, centered
        append("^A0N,20,20") // font size
        append("^FD$text^FS") // text content
        append("^XZ") // End label
    }
}

















