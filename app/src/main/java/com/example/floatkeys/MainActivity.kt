package com.example.floatkeys

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import rikka.shizuku.Shizuku

class MainActivity : Activity() {
    private lateinit var content: LinearLayout
    private lateinit var shizukuStatus: TextView
    private lateinit var overlayStatus: TextView
    private val shortcutButtons = mutableListOf<TextView>()
    private val permissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult -> if (requestCode==4102 && grantResult==PackageManager.PERMISSION_GRANTED) ShizukuBridge.bind(this); refresh() }

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); Shizuku.addRequestPermissionResultListener(permissionListener); buildUi() }
    override fun onResume() { super.onResume(); ShizukuBridge.bind(this); refresh() }
    override fun onDestroy() { Shizuku.removeRequestPermissionResultListener(permissionListener); super.onDestroy() }

    private fun buildUi() {
        val scroll=ScrollView(this); content=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(Ui.dp(this@MainActivity,28),Ui.dp(this@MainActivity,42),Ui.dp(this@MainActivity,28),Ui.dp(this@MainActivity,42)); setBackgroundColor(Color.rgb(247,247,248)) }; scroll.addView(content); setContentView(scroll)
        content.addView(Ui.title(this,"Float Keys")); content.addView(Ui.label(this,"Holdable modifiers + desktop shortcuts for Figma.",16f).apply{setPadding(0,Ui.dp(this@MainActivity,8),0,Ui.dp(this@MainActivity,28))})
        content.addView(sectionTitle("Setup")); overlayStatus=statusCard("Overlay permission"); shizukuStatus=statusCard("Shizuku"); content.addView(overlayStatus); gap(10); content.addView(shizukuStatus); gap(14)
        content.addView(Ui.actionButton(this,"Allow display over other apps").apply{setOnClickListener{startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:$packageName")))}}); gap(10)
        content.addView(Ui.actionButton(this,"Connect Shizuku").apply{setOnClickListener{connectShizuku()}})
        content.addView(sectionTitle("Shortcut keys").apply{setPadding(0,Ui.dp(this@MainActivity,34),0,Ui.dp(this@MainActivity,12))})
        repeat(4){slot-> val b=Ui.actionButton(this,""); b.setOnClickListener{editShortcut(slot)}; shortcutButtons+=b; content.addView(b); gap(10)}
        content.addView(sectionTitle("Floating controls").apply{setPadding(0,Ui.dp(this@MainActivity,34),0,Ui.dp(this@MainActivity,12))})
        content.addView(Ui.actionButton(this,"Start floating keys").apply{setOnClickListener{startOverlay()}}); gap(10)
        content.addView(Ui.actionButton(this,"Stop floating keys").apply{setOnClickListener{stopService(Intent(this@MainActivity,OverlayService::class.java))}})
        content.addView(Ui.label(this,"Hold mode: keep your finger on Alt / Shift / Ctrl while using your S Pen or another finger in Figma.",14f).apply{setPadding(0,Ui.dp(this@MainActivity,22),0,0)})
    }
    private fun statusCard(label:String)=Ui.actionButton(this,label).apply{isClickable=false;isFocusable=false}
    private fun sectionTitle(text:String)=TextView(this).apply{this.text=text;textSize=15f;setTextColor(Color.rgb(60,60,66));setTypeface(typeface,android.graphics.Typeface.BOLD);setPadding(0,0,0,Ui.dp(this@MainActivity,12))}
    private fun gap(dp:Int){content.addView(View(this),LinearLayout.LayoutParams(1,Ui.dp(this,dp)))}
    private fun refresh(){ overlayStatus.text=if(Settings.canDrawOverlays(this))"Overlay permission   ✓" else "Overlay permission   Needed"; shizukuStatus.text=when{!ShizukuBridge.isBinderAlive()->"Shizuku   Start Shizuku first";!ShizukuBridge.hasPermission()->"Shizuku   Permission needed";ShizukuBridge.isReady()->"Shizuku   Connected ✓";else->"Shizuku   Connecting…"}; shortcutButtons.forEachIndexed{i,b->b.text="Key ${i+1}     ${ShortcutStore.get(this,i).label()}"} }
    private fun connectShizuku(){ if(!ShizukuBridge.isBinderAlive()){Toast.makeText(this,"Open Shizuku and start its service first.",Toast.LENGTH_LONG).show();return}; if(!ShizukuBridge.hasPermission()) Shizuku.requestPermission(4102) else {ShizukuBridge.bind(this);Toast.makeText(this,"Connecting…",Toast.LENGTH_SHORT).show()}; refresh() }
    private fun startOverlay(){ if(!Settings.canDrawOverlays(this)){Toast.makeText(this,"Grant overlay permission first.",Toast.LENGTH_LONG).show();return}; if(!ShizukuBridge.isReady()){Toast.makeText(this,"Connect Shizuku first.",Toast.LENGTH_LONG).show();return}; if(Build.VERSION.SDK_INT>=33 && ContextCompat.checkSelfPermission(this,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),9001); ContextCompat.startForegroundService(this,Intent(this,OverlayService::class.java)); moveTaskToBack(true) }

    private fun editShortcut(slot:Int){
        val existing=ShortcutStore.get(this,slot); val wrapper=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(Ui.dp(this@MainActivity,24),Ui.dp(this@MainActivity,6),Ui.dp(this@MainActivity,24),0)}
        wrapper.addView(Ui.label(this,"Action type")); val typeSpinner=Spinner(this); val typeNames=listOf("Tap key / shortcut","Hold modifier"); typeSpinner.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,typeNames); typeSpinner.setSelection(if(existing.actionType==ActionType.HOLD)1 else 0); wrapper.addView(typeSpinner)
        val modifierRow=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}; fun check(t:String,c:Boolean)=CheckBox(this).apply{text=t;isChecked=c}; val ctrl=check("Ctrl",existing.ctrl); val shift=check("Shift",existing.shift); val alt=check("Alt",existing.alt); modifierRow.addView(ctrl);modifierRow.addView(shift);modifierRow.addView(alt);wrapper.addView(modifierRow)
        val keySpinner=Spinner(this);wrapper.addView(keySpinner)
        fun configure(type:ActionType){val choices=if(type==ActionType.HOLD)KeyNames.holdChoices else KeyNames.tapChoices; keySpinner.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,choices.map{it.first}); keySpinner.setSelection(choices.indexOfFirst{it.second==existing.keyCode}.coerceAtLeast(0)); modifierRow.visibility=if(type==ActionType.TAP)View.VISIBLE else View.GONE}
        configure(existing.actionType); typeSpinner.onItemSelectedListener=object:AdapterView.OnItemSelectedListener{override fun onItemSelected(parent:AdapterView<*>?,view:View?,position:Int,id:Long){configure(if(position==1)ActionType.HOLD else ActionType.TAP)};override fun onNothingSelected(parent:AdapterView<*>?){}}
        AlertDialog.Builder(this).setTitle("Assign Key ${slot+1}").setMessage("Tap = automatic press/release. Hold = stays down while your finger remains on the floating button.").setView(wrapper).setNegativeButton("Cancel",null).setPositiveButton("Save"){_,_-> val type=if(typeSpinner.selectedItemPosition==1)ActionType.HOLD else ActionType.TAP; val choices=if(type==ActionType.HOLD)KeyNames.holdChoices else KeyNames.tapChoices; val pair=choices[keySpinner.selectedItemPosition]; ShortcutStore.set(this,slot,Shortcut(type,if(type==ActionType.TAP)ctrl.isChecked else false,if(type==ActionType.TAP)shift.isChecked else false,if(type==ActionType.TAP)alt.isChecked else false,pair.second));refresh()}.show()
    }
}
