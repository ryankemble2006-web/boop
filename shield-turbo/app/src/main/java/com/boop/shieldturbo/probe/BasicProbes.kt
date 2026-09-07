package com.boop.shieldturbo.probe

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.boop.shieldturbo.model.*

class DeviceProbe : Probe { override fun read() = ProbeResult("device", "Device", ProbeStatus.AVAILABLE, "${Build.MANUFACTURER} ${Build.MODEL}", "${Build.DEVICE} • Android ${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}") }
class MemoryProbe(private val context: Context) : Probe { override fun read(): ProbeResult = try { val m=ActivityManager.MemoryInfo(); (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(m); ProbeResult("memory","Memory",ProbeStatus.AVAILABLE,"${gib(m.availMem)} free","${gib(m.totalMem)} total") } catch(e:Exception){ ProbeResult("memory","Memory",ProbeStatus.ERROR,"Unavailable",e.javaClass.simpleName) } }
class StorageProbe : Probe { override fun read(): ProbeResult = try { val s=StatFs(Environment.getDataDirectory().absolutePath); ProbeResult("storage","Internal storage",ProbeStatus.AVAILABLE,"${gib(s.availableBytes)} free","${gib(s.totalBytes)} total") } catch(e:Exception){ ProbeResult("storage","Internal storage",ProbeStatus.ERROR,"Unavailable",e.javaClass.simpleName) } }
