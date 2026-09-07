package com.boop.shieldturbo

import android.app.*
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.*
import android.widget.*
import com.boop.shieldturbo.analysis.ShieldAnalyzer
import com.boop.shieldturbo.model.*
import com.boop.shieldturbo.probe.*
import com.boop.shieldturbo.privilege.PrivilegeDetector

class MainActivity:Activity(){
    private lateinit var results:LinearLayout
    override fun onCreate(state:Bundle?){super.onCreate(state); window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); setContentView(buildUi())}
    private fun text(s:String,size:Float,color:Int=Color.WHITE)=TextView(this).apply{text=s;textSize=size;setTextColor(color);setPadding(8,8,8,8)}
    private fun buildUi():View{
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(64,36,64,36);setBackgroundColor(Color.BLACK)}
        root.addView(text("SHIELD TURBO",34f,Color.CYAN).apply{setTypeface(typeface,Typeface.BOLD)})
        root.addView(text("Measure first. Turbo only what earns it.",18f,Color.LTGRAY))
        val button=Button(this).apply{id=1001;text="ANALYSE SHIELD";textSize=22f;isFocusable=true;isFocusableInTouchMode=true;setOnClickListener{analyse()}}
        root.addView(button,LinearLayout.LayoutParams(-1,72))
        val scroll=ScrollView(this); results=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;id=1002};scroll.addView(results);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f));button.requestFocus();return root
    }
    private fun analyse(){
        results.removeAllViews()
        val tier=PrivilegeDetector.android(this).detect()
        add(ProbeResult("privilege","Capability tier",ProbeStatus.AVAILABLE,tier.name.replace('_',' '),if(tier.name=="STANDARD")"Optional ADB Turbo permission not granted" else "Detected locally"))
        ShieldAnalyzer(listOf(DeviceProbe(),MemoryProbe(this),StorageProbe(),CpuProbe(),ThermalProbe(),NetworkProbe(this))).analyze().results.forEach(::add)
        results.addView(text("No optimisation has been applied. This build measures the Shield so future Turbo actions can be evidence-based.",16f,Color.LTGRAY))
    }
    private fun add(r:ProbeResult){ val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(18,12,18,12);background=getDrawable(com.boop.shieldturbo.R.drawable.focus_panel)};card.addView(text("${r.label}  •  ${r.status}",16f,Color.CYAN));card.addView(text(r.value,24f));card.addView(text(r.evidence,14f,Color.LTGRAY));results.addView(card,LinearLayout.LayoutParams(-1,-2).apply{setMargins(0,8,0,8)}) }
}
