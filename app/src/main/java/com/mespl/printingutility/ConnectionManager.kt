package com.mespl.printingutility

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.*
import com.zebra.sdk.comm.BluetoothConnection

class ConnectionManager(
    private val scope: CoroutineScope,
    private val uiCallback: (PrinterState) -> Unit
) {

    companion object {
        private const val RECONNECT_INTERVAL = 2500L
    }

    @Volatile
    private var lastConnectAttempt = 0L
    private val STABLE_CONNECTION_GRACE_MS = 5000L

    var printerConnection: BluetoothConnection? = null
        private set

    private var connectionJob: Job? = null
    private var macAddress: String? = null


    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun start(mac: String) {
        if (connectionJob?.isActive == true) return

        macAddress = mac

        val device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mac)
        if (device.bondState != BluetoothDevice.BOND_BONDED) {
            uiCallback(PrinterState.DISCONNECTED)
            return
        }

        connectionJob = scope.launch(Dispatchers.IO) {
            BluetoothAdapter.getDefaultAdapter()?.cancelDiscovery()

            var lastAliveTime = System.currentTimeMillis()

            while (isActive) {
                try {
                    //  fast disconnect detection
                    val alive = isPrinterAlive(printerConnection)
                    if (!alive) {
                        val now = System.currentTimeMillis()
                        Log.d(
                            "BT_MANAGER",
                            " Disconnected in ${(now - lastAliveTime) / 1000.0}s"
                        )
                        forceDisconnect()
                        withContext(Dispatchers.Main) {
                            uiCallback(PrinterState.DISCONNECTED)
                        }
                    } else {
                        lastAliveTime = System.currentTimeMillis()
                    }

                    //  reconnect
                    if (printerConnection == null) {
                        val now = System.currentTimeMillis()
                        if (now - lastConnectAttempt < RECONNECT_INTERVAL) continue

                        lastConnectAttempt = now

                        withContext(Dispatchers.Main) {
                            uiCallback(PrinterState.CONNECTING)
                        }

                        val conn = BluetoothConnection(mac)
                        conn.open() // blocking

                        printerConnection = conn

                        withContext(Dispatchers.Main) {
                            uiCallback(PrinterState.CONNECTED)
                        }
                    }

                } catch (e: Exception) {
                    Log.e("BT_MANAGER", " Error: ${e.message}")
                    forceDisconnect()
                    withContext(Dispatchers.Main) {
                        uiCallback(PrinterState.DISCONNECTED)
                    }
                }
            }
        }
    }

    fun stop() {
        connectionJob?.cancel()
        connectionJob = null
        forceDisconnect()
    }

    fun isConnected(): Boolean {
        return printerConnection?.isConnected == true
    }



    private fun isPrinterAlive(conn: BluetoothConnection?): Boolean {
        return try {
            conn?.write(byteArrayOf(0x00))
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun forceDisconnect() {
        try {
            printerConnection?.close()
        } catch (_: Exception) {
        }
        printerConnection = null
    }
}
