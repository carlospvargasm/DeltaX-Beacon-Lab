package com.deltax.beaconlab
import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.*
import android.widget.*
import java.util.Locale
class MainActivity:Activity(){
 var advertiser:BluetoothLeAdvertiser?=null; var ac:AdvertiseCallback?=null; var scanner:BluetoothLeScanner?=null; var scanning=false
 lateinit var status:TextView; lateinit var log:TextView
 val uuid=ParcelUuid.fromString("0000FE2C-0000-1000-8000-00805F9B34FB")
 override fun onCreate(b:Bundle?){super.onCreate(b);val l=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(42,60,42,42)}
 status=TextView(this).apply{text="DeltaX Beacon Lab\nDiagnóstico BLE";textSize=20f};val e=Button(this).apply{text="EMITIR BEACON"};val x=Button(this).apply{text="DETENER EMISIÓN"};val s=Button(this).apply{text="DETECTAR BEACON"};val q=Button(this).apply{text="DETENER DETECCIÓN"};log=TextView(this).apply{text="Sin detecciones";textSize=16f};listOf(status,e,x,s,q,log).forEach{l.addView(it)};setContentView(l);permissions();e.setOnClickListener{emit()};x.setOnClickListener{stopEmit()};s.setOnClickListener{scan()};q.setOnClickListener{stopScan()}}
 fun ok(p:String)=Build.VERSION.SDK_INT<31||checkSelfPermission(p)==PackageManager.PERMISSION_GRANTED
 fun permissions(){if(Build.VERSION.SDK_INT>=31)requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_ADVERTISE,Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.BLUETOOTH_SCAN),10)}
 fun emit(){stopEmit(); if(!ok(Manifest.permission.BLUETOOTH_ADVERTISE)){permissions();status.text="Autoriza Bluetooth y vuelve a pulsar EMITIR";return};val bm=getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager;if(!bm.adapter.isEnabled){status.text="Bluetooth está apagado";return};advertiser=bm.adapter.bluetoothLeAdvertiser;if(advertiser==null){status.text="Este teléfono no admite BLE advertising";return};val d=AdvertiseData.Builder().addServiceUuid(uuid).addServiceData(uuid,byteArrayOf(0,0,0)).build();val s=AdvertiseSettings.Builder().setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY).setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH).setConnectable(true).build();ac=object:AdvertiseCallback(){override fun onStartSuccess(x:AdvertiseSettings?){status.text="EMITIENDO ✓\nUUID 0xFE2C\nModel ID 000000"};override fun onStartFailure(e:Int){status.text="ERROR EMISIÓN BLE: $e"}};advertiser?.startAdvertising(s,d,ac)}
 fun stopEmit(){ac?.let{advertiser?.stopAdvertising(it)};ac=null;status.text="Emisión detenida"}
 val sc=object:ScanCallback(){override fun onScanResult(t:Int,r:ScanResult){show(r)};override fun onScanFailed(e:Int){log.text="ERROR ESCÁNER: $e"}}
 fun show(r:ScanResult){val d=r.scanRecord?.getServiceData(uuid)?:return;val m=d.take(3).joinToString(""){String.format(Locale.US,"%02X",it.toInt() and 255)};val z=when{r.rssi>=-55->"muy cerca";r.rssi>=-70->"cerca";r.rssi>=-85->"media";else->"lejos"};runOnUiThread{log.text="BEACON DETECTADO ✓\nRSSI: ${r.rssi} dBm ($z)\nUUID: 0xFE2C\nModel ID: $m\nHora: ${java.text.SimpleDateFormat("HH:mm:ss",Locale.getDefault()).format(java.util.Date())}"}}
 fun scan(){stopScan(); if(!ok(Manifest.permission.BLUETOOTH_SCAN)){permissions();log.text="Autoriza Bluetooth y pulsa DETECTAR nuevamente";return};val bm=getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager;if(!bm.adapter.isEnabled){log.text="Bluetooth está apagado";return};scanner=bm.adapter.bluetoothLeScanner;if(scanner==null){log.text="Escáner BLE no disponible";return};val f=ScanFilter.Builder().setServiceUuid(uuid).build();val s=ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();scanner?.startScan(listOf(f),s,sc);scanning=true;log.text="BUSCANDO beacon 0xFE2C…"}
 fun stopScan(){if(scanning&&ok(Manifest.permission.BLUETOOTH_SCAN))scanner?.stopScan(sc);scanning=false}
 override fun onDestroy(){stopScan();stopEmit();super.onDestroy()}
}