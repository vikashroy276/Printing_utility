package com.mespl.printingutility

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    private val REQUEST_BLUETOOTH_PERMISSIONS = 1

    private lateinit var qrImageView: ImageView
    private lateinit var printButton: Button
    private lateinit var serialNumberEditText: EditText
    private lateinit var checkBtn: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var version: TextView

    private lateinit var connectionManager: ConnectionManager

    private var printerAddress: String? = null
    private var serialNumber = ""
    private lateinit var qrBitmap: Bitmap

    private var receiverRegistered = false

    // -------------------- LIFECYCLE --------------------

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        setupToolbar()
        requestBluetoothPermissions()

        connectionManager = ConnectionManager(
            scope = lifecycleScope
        ) { state ->
            updatePrinterState(state)
        }

        checkBtn.setOnClickListener { openBluetoothScanner() }
        printButton.setOnClickListener { printing() }

        setupSerialWatcher()

        val autoReconnect = AppSharedPreference.getBoolean(
            this,
            AppSharedPreference.PREF_KEY.AUTO_RECONNECT
        )

        if (autoReconnect) {
            val mac = AppSharedPreference.getString(
                this,
                AppSharedPreference.PREF_KEY.MAC
            )

            mac?.let {
                connectionManager.start(it)
            }
        }

    }

//    @SuppressLint("MissingPermission")
//    override fun onStart() {
//        super.onStart()
//        printerAddress = AppSharedPreference.getString(
//            this,
//            AppSharedPreference.PREF_KEY.MAC
//        )
//
//        printerAddress?.let {
//            connectionManager.start(it)
//        }
//    }

//    override fun onResume() {
//        super.onResume()
//        updatePrinterState(PrinterState.DISCONNECTED)
//    }


//    override fun onStop() {
//        super.onStop()
//        connectionManager.stop()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        connectionManager.stop()
//    }

    // -------------------- UI SETUP --------------------

    private fun bindViews() {
        qrImageView = findViewById(R.id.qrImageView)
        printButton = findViewById(R.id.printButton)
        checkBtn = findViewById(R.id.connectBtn)
        progressBar = findViewById(R.id.progressBar)
        version = findViewById(R.id.version)
        serialNumberEditText = findViewById(R.id.serialNumberEditText)

        version.text = "v${BuildConfig.VERSION_NAME}"
        serialNumberEditText.requestFocus()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarTv)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    // -------------------- BLUETOOTH --------------------

    @SuppressLint("MissingPermission")
    private fun openBluetoothScanner() {
        BluetoothScanDialog { device ->
            printerAddress = device.address
            AppSharedPreference.putString(this, AppSharedPreference.PREF_KEY.MAC, device.address)
            AppSharedPreference.putBoolean(this, AppSharedPreference.PREF_KEY.AUTO_RECONNECT, true)

            Toast.makeText(this, "Selected: ${device.name}", Toast.LENGTH_SHORT).show()
            connectionManager.start(device.address)

        }.show(supportFragmentManager, "BT_SCAN")
    }

    // -------------------- PRINTING --------------------

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun printing() {
        lifecycleScope.launch {
            if (serialNumber.isEmpty()) {
                toast("Serial number is empty")
                return@launch
            }

            if (!connectionManager.isConnected()) {
                toast("Please connect printer.")
                return@launch
            }

            val printerConnection = connectionManager.printerConnection ?: return@launch

            printQRCodeWithText(
                printerAddress,
                qrBitmap,
                serialNumber,
                printerConnection,
                this@MainActivity
            ).collectLatest { state ->
                if (state is PrintState.Completed && state.result.isSuccess) {
                    clearData()
                    serialNumberEditText.requestFocus()
                }
            }
        }
    }

    private fun showLoader() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideLoader() {
        progressBar.visibility = View.GONE
    }

    // -------------------- SERIAL INPUT --------------------

    private fun setupSerialWatcher() {
        serialNumberEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().uppercase()
                if (s.toString() != input) {
                    serialNumberEditText.setText(input)
                    serialNumberEditText.setSelection(input.length)
                    return
                }

                serialNumber = input
                printButton.isEnabled = input.isNotEmpty()

                if (input.length >= 3) {
                    generateQr(input)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun generateQr(serial: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            qrBitmap = generateQRCode(serial, 150, 150)!!
            withContext(Dispatchers.Main) {
                qrImageView.setImageBitmap(qrBitmap)
            }
        }
    }

    // -------------------- UI STATE --------------------

    private fun updatePrinterState(state: PrinterState) {
        when (state) {
            PrinterState.CONNECTING -> {
                showLoader()
                checkBtn.setColorFilter(
                    ContextCompat.getColor(this, R.color.red),
                    PorterDuff.Mode.SRC_IN
                )
                checkBtn.isClickable = false
            }

            PrinterState.CONNECTED -> {
                hideLoader()
                checkBtn.setColorFilter(
                    ContextCompat.getColor(this, R.color.green),
                    PorterDuff.Mode.SRC_IN
                )
                checkBtn.isClickable = false
            }

            PrinterState.DISCONNECTED -> {
                showLoader()
                checkBtn.setColorFilter(
                    ContextCompat.getColor(this, R.color.red),
                    PorterDuff.Mode.SRC_IN
                )
                checkBtn.isClickable = true
            }
        }
    }

    // -------------------- HELPERS --------------------

    private fun clearData() {
        serialNumberEditText.setText("")
        qrImageView.setImageBitmap(null)
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun requestBluetoothPermissions() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )

        val pending = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (pending.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                pending.toTypedArray(),
                REQUEST_BLUETOOTH_PERMISSIONS
            )
        }
    }
}

