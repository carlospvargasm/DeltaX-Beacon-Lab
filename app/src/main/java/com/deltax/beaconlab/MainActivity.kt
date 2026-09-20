package com.deltax.beaconlab
import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.*
import android.os.ParcelUuid
import android.widget.*
class MainActivity: Activity() {
 private var adv: BluetoothLeAdvertiser?=null
 private var cb: AdvertiseCallback?=null
 override fun onCreate(b: Bundle?){super.onCreate(b)
  val l=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(48,80,48,48)}
  val s=TextView(this).apply{text="DeltaX Beacon Lab\nListo para probar BLE 0xFE2C";textSize=20f}
  val start=Button(this).apply{text="INICIAR EMISIÓN"}; val stop=Button(this).apply{text="DETENER"}
  l.addView(s);l.addView(start);l.addView(stop);setContentView(l)
  if(Build.VERSION.SDK_INT>=31 && (checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE)!=PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED)) requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_ADVERTISE,Manifest.permission.BLUETOOTH_CONNECT),7)
  start.setOnClickListener{try{
   val bm=getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager; adv=bm.adapter.bluetoothLeAdvertiser
   if(adv==null){s.text="Este teléfono no permite BLE advertising";return@setOnClickListener}
   val uuid=ParcelUuid.fromString("0000FE2C-0000-1000-8000-00805F9B34FB")
   val data=AdvertiseData.Builder().addServiceUuid(uuid).addServiceData(uuid, byteArrayOf(0,0,0)).setIncludeDeviceName(false).build()
   val settings=AdvertiseSettings.Builder().setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY).setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH).setConnectable(true).build()
   cb=object:AdvertiseCallback(){override fun onStartSuccess(x:AdvertiseSettingsInEffect?){s.text="EMITIENDO\nFast Pair UUID: 0xFE2C\nModel ID laboratorio: 000000"};override fun onStartFailure(e:Int){s.text="Error BLE: $e"}}
   adv?.startAdvertising(settings,data,cb)
  }catch(e:Exception){s.text="Error: "+e.message}}
  stop.setOnClickListener{cb?.let{adv?.stopAdvertising(it)};s.text="Emisión detenida"}
 }
}