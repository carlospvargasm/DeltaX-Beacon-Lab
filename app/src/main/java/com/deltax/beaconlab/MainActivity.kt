package com.deltax.beaconlab
import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.pm.PackageManager
import android.os.*
import android.os.ParcelUuid
import android.widget.*
class MainActivity:Activity(){
 lateinit var out:TextView; var scanning=false; val seen=linkedMapOf<String,ScanResult>()
 val scanner get()=(getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter.bluetoothLeScanner
 override fun onCreate(b:Bundle?){super.onCreate(b);val l=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(36,48,36,36)}
  val title=TextView(this).apply{text="DeltaX · Estimote Lab";textSize=24f};val scan=Button(this).apply{text="BUSCAR ESTIMOTE / EDDYSTONE"};val stop=Button(this).apply{text="DETENER"}
  out=TextView(this).apply{text="Listo para escanear.";textSize=13f;setTextIsSelectable(true)}
  l.addView(title);l.addView(scan);l.addView(stop);l.addView(ScrollView(this).apply{addView(out)},LinearLayout.LayoutParams(-1,0,1f));setContentView(l)
  scan.setOnClickListener{permission()};stop.setOnClickListener{stop();render()}
 }
 fun permission(){if(Build.VERSION.SDK_INT>=31&&checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)!=PackageManager.PERMISSION_GRANTED)requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT),7) else start()}
 override fun onRequestPermissionsResult(r:Int,p:Array<out String>,g:IntArray){super.onRequestPermissionsResult(r,p,g);if(r==7&&g.isNotEmpty()&&g[0]==PackageManager.PERMISSION_GRANTED)start()}
 fun start(){seen.clear();out.text="Escaneando… acerca el Estimote al teléfono.";scanner.startScan(null,ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(),cb);scanning=true}
 fun stop(){if(scanning){try{scanner.stopScan(cb)}catch(_:SecurityException){};scanning=false}}
 val cb=object:ScanCallback(){override fun onScanResult(t:Int,r:ScanResult){seen[r.device.address]=r;runOnUiThread{render()}};override fun onScanFailed(e:Int){runOnUiThread{out.text="Error BLE: "+e}}}
 fun render(){if(seen.isEmpty()){out.text="Sin dispositivos BLE detectados.";return};out.text=seen.values.sortedByDescending{it.rssi}.take(30).joinToString("\n\n"){describe(it)}}
 fun describe(r:ScanResult):String{val rec=r.scanRecord;val ed=rec?.getServiceData(ParcelUuid.fromString("0000feaa-0000-1000-8000-00805f9b34fb"));val ap=rec?.getManufacturerSpecificData(0x004c);val ib=ap!=null&&ap.size>=23&&(ap[0].toInt()and 255)==2&&(ap[1].toInt()and 255)==21;val likely=ib||ed!=null||(rec?.deviceName?.contains("estimote",true)==true)
  return (if(likely)"★ POSIBLE ESTIMOTE\n" else "BLE\n")+"Nombre: "+(rec?.deviceName?:"(sin nombre)")+"\nDirección: "+r.device.address+"\nRSSI: "+r.rssi+" dBm"+(if(ib)"\niBeacon: sí" else "")+(if(ed!=null)"\n"+eddystone(ed) else "")}
 fun eddystone(d:ByteArray):String{if(d.isEmpty())return "Eddystone";return when(d[0].toInt()and 255){0->"Eddystone-UID";16->"Eddystone-URL: "+url(d);32->"Eddystone-TLM";else->"Eddystone frame 0x"+"%02X".format(d[0])}}
 fun url(d:ByteArray):String{if(d.size<3)return "(incompleta)";val pre=arrayOf("http://www.","https://www.","http://","https://");val suf=arrayOf(".com/",".org/",".edu/",".net/",".info/",".biz/",".gov/",".com",".org",".edu",".net",".info",".biz",".gov");val s=StringBuilder(pre.getOrElse(d[2].toInt()and 255){""});for(i in 3 until d.size){val v=d[i].toInt()and 255;if(v<=13)s.append(suf[v])else s.append(v.toChar())};return s.toString()}
 override fun onDestroy(){stop();super.onDestroy()}
}