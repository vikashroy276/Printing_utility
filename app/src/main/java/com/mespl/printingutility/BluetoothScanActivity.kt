package com.mespl.printingutility

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity

/**
 * An [AppCompatActivity] that displays a list of paired Bluetooth devices.
 *
 * This activity retrieves the list of devices already bonded with the Android system
 * and presents them in a [ListView]. When a user selects a device from the list,
 * the activity sends the device's name and address back to the calling activity
 * via an [Intent] result.
 *
 * It does not perform a new Bluetooth scan but relies on the pre-existing list of
 * paired devices provided by the system's [BluetoothAdapter].
 */
class BluetoothScanActivity : AppCompatActivity() {

    private lateinit var adapter: ArrayAdapter<String>
    private val deviceList = mutableListOf<BluetoothDevice>()

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_scan)

        val listView: ListView = findViewById(R.id.device_list)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        listView.adapter = adapter

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
            return
        }

        // Get paired devices instead of scanning
        val pairedDevices = bluetoothAdapter.bondedDevices
        if (pairedDevices.isEmpty()) {
            Toast.makeText(this, "No paired Bluetooth devices found", Toast.LENGTH_SHORT).show()
            return
        }

        pairedDevices.forEach { device ->
            if (!deviceList.contains(device)) {
                deviceList.add(device)
                adapter.add("${device.name ?: "Unknown"} - ${device.address}")
            }
        }
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedDevice = deviceList[position]

            val resultIntent = Intent().apply {
                putExtra("deviceName", selectedDevice.name)
                putExtra("deviceAddress", selectedDevice.address)
                putExtra("connectionStatus", true)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

    }
}


