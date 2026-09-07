package com.boop.shieldturbo.probe

import com.boop.shieldturbo.model.*
import java.io.File

sealed class SourceRead { data class Value(val path:String,val text:String):SourceRead(); data class Restricted(val path:String):SourceRead(); object Missing:SourceRead() }
open class FileSourceReader { open fun readFirst(paths: List<String>): SourceRead { var restricted:String?=null; for(p in paths){ val f=File(p); if(!f.exists()) continue; try { return SourceRead.Value(p,f.readText().trim()) } catch(_:SecurityException){ restricted=p } catch(_:Exception){ restricted=p } }; return restricted?.let{SourceRead.Restricted(it)} ?: SourceRead.Missing } }
private fun mapped(key:String,label:String,r:SourceRead, parser:(String)->String):ProbeResult = when(r){ is SourceRead.Value -> try{ ProbeResult(key,label,ProbeStatus.AVAILABLE,parser(r.text),r.path) }catch(_:Exception){ ProbeResult(key,label,ProbeStatus.ERROR,"Unreadable value",r.path) }; is SourceRead.Restricted -> ProbeResult(key,label,ProbeStatus.RESTRICTED,"Restricted",r.path); SourceRead.Missing -> ProbeResult(key,label,ProbeStatus.UNSUPPORTED,"Not exposed","No readable Shield source") }
class CpuProbe(private val reader:FileSourceReader=FileSourceReader()):Probe { override fun read()=mapped("cpu","CPU frequency",reader.readFirst(listOf("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq","/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_cur_freq"))){ "%.0f MHz".format(java.util.Locale.US,it.toLong()/1000.0) } }
class ThermalProbe(private val reader:FileSourceReader=FileSourceReader()):Probe { override fun read()=mapped("thermal","Thermal",reader.readFirst(listOf("/sys/class/thermal/thermal_zone0/temp","/sys/class/thermal/thermal_zone1/temp"))){ val n=it.toDouble(); val c=if(n>1000)n/1000 else n; "%.1f °C".format(java.util.Locale.US,c) } }
