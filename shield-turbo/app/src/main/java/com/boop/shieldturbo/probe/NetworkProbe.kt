package com.boop.shieldturbo.probe

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.boop.shieldturbo.model.*
class NetworkProbe(private val context:Context):Probe { override fun read():ProbeResult = try { val cm=context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager; val n=cm.activeNetwork ?: return ProbeResult("network","Network",ProbeStatus.UNSUPPORTED,"Disconnected","No active network"); val c=cm.getNetworkCapabilities(n) ?: return ProbeResult("network","Network",ProbeStatus.ERROR,"Unknown","Capabilities unavailable"); val t=when{c.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)->"Ethernet";c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)->"Wi-Fi";c.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)->"Cellular";else->"Other"}; ProbeResult("network","Network",ProbeStatus.AVAILABLE,t,if(c.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))"Internet validated" else "Local/unvalidated") } catch(e:Exception){ ProbeResult("network","Network",ProbeStatus.ERROR,"Unavailable",e.javaClass.simpleName) } }
