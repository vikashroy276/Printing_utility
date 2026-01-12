# Printing_utility

# Zebra Bluetooth Printer Integration

This project demonstrates how to connect and use a Zebra Bluetooth printer for printing barcodes and QR codes in an Android application.

## Features

- Print barcode by entering text
- Print QR code using scanner input
- Pair Zebra printer via Bluetooth
- Connect to Zebra printer using Bluetooth Manager
- Automatically connects when the device is in range
- Automatically disconnects when the device goes out of range

## Requirements

- Zebra Bluetooth Printer
- Android device with Bluetooth enabled
- Bluetooth permissions granted in the app
- Zebra SDK (if used)

## How It Works

1. User pairs the Zebra printer from Bluetooth settings.
2. The app scans for nearby paired devices.
3. When the printer is in range, the app connects automatically.
4. User can:
   - Enter text to print barcode
   - Scan and print QR code
5. If the printer goes out of range, it disconnects automatically.

## Permissions Used

```xml
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />

