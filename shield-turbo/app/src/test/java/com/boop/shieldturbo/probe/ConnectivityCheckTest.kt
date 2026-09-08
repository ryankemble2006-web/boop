package com.boop.shieldturbo.probe

import org.junit.Assert.assertEquals
import org.junit.Test

class ConnectivityCheckTest {
    @Test fun validatedInternetIsReachable() {
        assertEquals(Reachability.INTERNET_REACHABLE, ConnectivityCheck.map(true, true, true))
    }

    @Test fun connectedButUnvalidatedIsLocalOnly() {
        assertEquals(Reachability.LOCAL_ONLY, ConnectivityCheck.map(true, true, false))
    }

    @Test fun noActiveNetworkIsOffline() {
        assertEquals(Reachability.OFFLINE, ConnectivityCheck.map(false, false, false))
    }

    @Test fun activeNetworkWithoutCapabilitiesIsUnknown() {
        assertEquals(Reachability.UNKNOWN, ConnectivityCheck.map(true, false, false))
    }
}
