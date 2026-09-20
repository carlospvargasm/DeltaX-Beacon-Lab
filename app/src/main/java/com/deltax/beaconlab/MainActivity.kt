package com.deltax.beaconlab
import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private var advertiser: BluetoothLeAdvertiser? = null
    private var callback: AdvertiseCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48,80,48,48) }
        val status = TextView(this).apply { text = "DeltaX Beacon Lab\nListo para probar BLE 0xFE2C"; textSize = 20f }
        val start = Button(this).apply { text = "INICIAR EMISIÓN" }
        val stop = Button(this).apply { text = "DETENER" }
        layout.addView(status); layout.addView(start); layout.addView(stop); setContentView(layout)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            (checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED ||
             checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_CONNECT), 7)
        }

        start.setOnClickListener {
            try {
                val bm = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    status.text = "Autoriza Bluetooth y vuelve a pulsar INICIAR"
                    return@setOnClickListener
                }
                advertiser = bm.adapter.bluetoothLeAdvertiser
                if (advertiser == null) { status.text = "Este teléfono no permite BLE advertising"; return@setOnClickListener }
                val uuid = ParcelUuid.fromString("0000FE2C-0000-1000-8000-00805F9B34FB")
                val data = AdvertiseData.Builder().addServiceUuid(uuid).addServiceData(uuid, byteArrayOf(0,0,0)).setIncludeDeviceName(false).build()
                val settings = AdvertiseSettings.Builder().setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY).setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH).setConnectable(true).build()
                callback = object : AdvertiseCallback() {
                    override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) { status.text = "EMITIENDO\nFast Pair UUID: 0xFE2C\nModel ID laboratorio: 000000" }
                    override fun onStartFailure(errorCode: Int) { status.text = "Error BLE: $errorCode" }
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                    checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED) {
                    advertiser?.startAdvertising(settings, data, callback)
                }
            } catch (e: Exception) { status.text = "Error: " + e.message }
        }
        stop.setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED) {
                callback?.let { advertiser?.stopAdvertising(it) }
            }
            status.text = "Emisión detenida"
        }
    }
}