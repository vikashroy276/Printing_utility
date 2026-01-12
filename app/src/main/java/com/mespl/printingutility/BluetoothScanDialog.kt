package com.mespl.printingutility

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.fragment.app.DialogFragment
import com.mespl.printingutility.databinding.DialogBluetoothScanBinding

class BluetoothScanDialog(
    private val onDeviceSelected: (BluetoothDevice) -> Unit
) : DialogFragment() {

    private var _binding: DialogBluetoothScanBinding? = null
    private val binding get() = _binding!!
    private var selectionTime: Long = 0L


    private lateinit var adapter: ArrayAdapter<String>
    private val deviceList = mutableListOf<BluetoothDevice>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogBluetoothScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, mutableListOf())
        binding.deviceList.adapter = adapter

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            Toast.makeText(requireContext(), "Bluetooth not supported", Toast.LENGTH_SHORT).show()
            dismiss()
            return
        }

        val pairedDevices = bluetoothAdapter.bondedDevices
        if (pairedDevices.isEmpty()) {
            Toast.makeText(requireContext(), "No paired devices found", Toast.LENGTH_SHORT).show()
            return
        }

        pairedDevices.forEach { device ->
            deviceList.add(device)
            adapter.add("${device.name ?: "Unknown"} - ${device.address}")
        }

//        binding.deviceList.setOnItemClickListener { _, _, position, _ ->
//            onDeviceSelected(deviceList[position])
//            dismiss()
//        }

        binding.deviceList.setOnItemClickListener { _, _, position, _ ->
            val device = deviceList[position]

            selectionTime = System.currentTimeMillis()

            Log.d(
                "BT_TIME",
                "📌 Device clicked: ${device.name} | ${device.address} | time=$selectionTime"
            )

            onDeviceSelected(device)
            dismiss()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
