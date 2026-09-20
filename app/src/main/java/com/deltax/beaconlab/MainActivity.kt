package com.deltax.beaconlab
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
class MainActivity:Activity(){
 override fun onCreate(b:Bundle?){super.onCreate(b)
  val l=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(48,70,48,48)}
  val title=TextView(this).apply{text="DeltaX Nearby Share Lab";textSize=22f}
  val msg=EditText(this).apply{hint="Mensaje o enlace";setText("Conoce DeltaX")}
  val send=Button(this).apply{text="COMPARTIR CON TELÉFONO CERCANO"}
  val info=TextView(this).apply{text="Se abrirá el selector de compartir de Android. Elige Quick Share y luego el teléfono cercano.";textSize=15f}
  listOf(title,msg,send,info).forEach{l.addView(it)};setContentView(l)
  send.setOnClickListener{
   val text=msg.text.toString().trim()
   if(text.isEmpty()){Toast.makeText(this,"Escribe un mensaje o enlace",Toast.LENGTH_SHORT).show();return@setOnClickListener}
   val i=Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_TEXT,text)}
   startActivity(Intent.createChooser(i,"Compartir cerca"))
  }
 }
}