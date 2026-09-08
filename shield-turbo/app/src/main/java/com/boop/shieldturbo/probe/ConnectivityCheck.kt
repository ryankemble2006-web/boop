package com.boop.shieldturbo.probe

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

enum class Reachability { LOCAL_ONLY, INTERNET_REACHABLE, OFFLINE, UNKNOWN }

object ConnectivityCheck {
    fun map(activeNetwork: Boolean, capabilitiesKnown: Boolean, validated: Boolean): Reachability = when {
        !activeNetwork -> Reachability.OFFLINE
        !capabilitiesKnown -> Reachability.UNKNOWN
        validated -> Reachability.INTERNET_REACHABLE
        else -> Reachability.LOCAL_ONLY
    }

    fun current(context: Context): Reachability = try {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return Reachability.OFFLINE
        val capabilities = cm.getNetworkCapabilities(network) ?: return Reachability.UNKNOWN
        map(true, true, capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
    } catch (_: Exception) {
        Reachability.UNKNOWN
    }
}
