package com.deltax.beaconlab

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.*
import android.widget.*

class MainActivity : Activity() {
    private var advertiser: BluetoothLeAdvertiser? = null
    private var callback: AdvertiseCallback? = null
    private var advertising = false
    private var startedAt = 0L
    private lateinit var status: TextView
    private lateinit var message: EditText
    private lateinit var start: Button
    private val handler = Handler(Looper.getMainLooper())

    private val ticker = object : Runnable {
        override fun run() {
            if (advertising) {
                val s = (SystemClock.elapsedRealtime() - startedAt) / 1000
                status.text = "EMITIENDO BLE ✓\nTiempo: ${s}s\nUUID: 0xFE2C\nModel ID: 000000\nMensaje local: ${message.text}"
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(48,70,48,48)}
        status=TextView(this).apply{text="DeltaX Beacon Lab\nListo para emitir";textSize=20f}
        message=EditText(this).apply{hint="Mensaje";setText("Conoce DeltaX")}
        start=Button(this).apply{text="INICIAR EMISIÓN"}
        val change=Button(this).apply{text="CAMBIAR MENSAJE / REINICIAR"}
        val stop=Button(this).apply{text="DETENER"}
        layout.addView(status);layout.addView(message);layout.addView(start);layout.addView(change);layout.addView(stop);setContentView(layout)
        if(Build.VERSION.SDK_INT>=31 && (!has(Manifest.permission.BLUETOOTH_ADVERTISE)||!has(Manifest.permission.BLUETOOTH_CONNECT)))
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_ADVERTISE,Manifest.permission.BLUETOOTH_CONNECT),7)
        start.setOnClickListener { begin() }
        change.setOnClickListener { end("Actualizando…"); begin() }
        stop.setOnClickListener { end("Emisión detenida") }
    }

    private fun has(p:String)=Build.VERSION.SDK_INT<31 || checkSelfPermission(p)==PackageManager.PERMISSION_GRANTED

    private fun begin(){
        if(advertising){return}
        if(!has(Manifest.permission.BLUETOOTH_CONNECT)||!has(Manifest.permission.BLUETOOTH_ADVERTISE)){status.text="Autoriza Bluetooth y vuelve a pulsar INICIAR";return}
        try{
            val bm=getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            advertiser=bm.adapter.bluetoothLeAdvertiser
            if(advertiser==null){status.text="Este teléfono no permite BLE advertising";return}
            val uuid=ParcelUuid.fromString("0000FE2C-0000-1000-8000-00805F9B34FB")
            // Fast Pair service data: 3-byte laboratory Model ID.
            // The editable text is displayed locally for diagnostics; Fast Pair notification text
            // is controlled by Google's registered Model ID metadata, not arbitrary BLE text.
            val data=AdvertiseData.Builder().addServiceUuid(uuid).addServiceData(uuid,byteArrayOf(0,0,0)).setIncludeDeviceName(false).build()
            val settings=AdvertiseSettings.Builder().setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY).setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH).setConnectable(true).build()
            callback=object:AdvertiseCallback(){
                override fun onStartSuccess(x:AdvertiseSettings?){
                    advertising=true;start.isEnabled=false;message.isEnabled=true
                    startedAt=SystemClock.elapsedRealtime()
                    handler.removeCallbacks(ticker);handler.post(ticker)
                }
                override fun onStartFailure(e:Int){
                    advertising=false;start.isEnabled=true;handler.removeCallbacks(ticker);callback=null
                    status.text=when(e){
                        ADVERTISE_FAILED_TOO_MANY_ADVERTISERS->"BLE ocupado. Apaga y enciende Bluetooth y pulsa INICIAR."
                        ADVERTISE_FAILED_DATA_TOO_LARGE->"Error BLE: datos demasiado grandes"
                        ADVERTISE_FAILED_FEATURE_UNSUPPORTED->"Error BLE: advertising no soportado"
                        ADVERTISE_FAILED_INTERNAL_ERROR->"Error BLE interno"
                        ADVERTISE_FAILED_ALREADY_STARTED->"La emisión BLE ya está activa"
                        else->"Error BLE: $e"
                    }
                }
            }
            advertiser?.startAdvertising(settings,data,callback)
        }catch(e:Exception){advertising=false;start.isEnabled=true;status.text="Error: "+e.message}
    }

    private fun end(msg:String){
        handler.removeCallbacks(ticker)
        if(has(Manifest.permission.BLUETOOTH_ADVERTISE)) callback?.let{try{advertiser?.stopAdvertising(it)}catch(_:Exception){}}
        callback=null;advertising=false;start.isEnabled=true;status.text=msg
    }
    override fun onDestroy(){end("Emisión detenida");super.onDestroy()}
}
