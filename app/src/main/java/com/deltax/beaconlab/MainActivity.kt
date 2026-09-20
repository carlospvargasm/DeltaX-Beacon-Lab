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
    private lateinit var status: TextView
    private lateinit var start: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(48,80,48,48)}
        status=TextView(this).apply{text="DeltaX Beacon Lab\nListo";textSize=20f}
        start=Button(this).apply{text="INICIAR EMISIÓN"}
        val stop=Button(this).apply{text="DETENER"}
        layout.addView(status);layout.addView(start);layout.addView(stop);setContentView(layout)
        if(Build.VERSION.SDK_INT>=31 && (!has(Manifest.permission.BLUETOOTH_ADVERTISE)||!has(Manifest.permission.BLUETOOTH_CONNECT)))
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_ADVERTISE,Manifest.permission.BLUETOOTH_CONNECT),7)
        start.setOnClickListener { begin() }
        stop.setOnClickListener { end("Emisión detenida") }
    }

    private fun has(p:String)=Build.VERSION.SDK_INT<31 || checkSelfPermission(p)==PackageManager.PERMISSION_GRANTED

    private fun begin(){
        if(advertising){ status.text="EMITIENDO\nFast Pair UUID: 0xFE2C\nModel ID laboratorio: 000000"; return }
        if(!has(Manifest.permission.BLUETOOTH_CONNECT)||!has(Manifest.permission.BLUETOOTH_ADVERTISE)){
            status.text="Autoriza Bluetooth y vuelve a pulsar INICIAR"; return
        }
        try{
            val bm=getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            advertiser=bm.adapter.bluetoothLeAdvertiser
            if(advertiser==null){status.text="Este teléfono no permite BLE advertising";return}
            callback?.let { try{advertiser?.stopAdvertising(it)}catch(_:Exception){} }
            callback=null
            val uuid=ParcelUuid.fromString("0000FE2C-0000-1000-8000-00805F9B34FB")
            val data=AdvertiseData.Builder().addServiceUuid(uuid).addServiceData(uuid,byteArrayOf(0,0,0)).setIncludeDeviceName(false).build()
            val settings=AdvertiseSettings.Builder().setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY).setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH).setConnectable(true).build()
            callback=object:AdvertiseCallback(){
                override fun onStartSuccess(x:AdvertiseSettings?){
                    advertising=true;start.isEnabled=false
                    status.text="EMITIENDO\nFast Pair UUID: 0xFE2C\nModel ID laboratorio: 000000"
                }
                override fun onStartFailure(e:Int){
                    advertising=false;start.isEnabled=true;callback=null
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

    private fun end(message:String){
        if(has(Manifest.permission.BLUETOOTH_ADVERTISE)) callback?.let{try{advertiser?.stopAdvertising(it)}catch(_:Exception){}}
        callback=null;advertising=false;start.isEnabled=true;status.text=message
    }
    override fun onDestroy(){end("Emisión detenida");super.onDestroy()}
}