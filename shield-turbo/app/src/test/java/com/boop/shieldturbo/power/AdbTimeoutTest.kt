package com.boop.shieldturbo.power

import org.junit.Assert.fail
import org.junit.Test
import java.net.InetAddress
import java.net.ServerSocket
import java.net.SocketTimeoutException
import java.security.KeyPairGenerator
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class AdbTimeoutTest {
    @Test(timeout = 10000L) fun silentDaemonCannotWaitForever() {
        val generator = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }
        val key = generator.generateKeyPair()
        val worker = Executors.newSingleThreadExecutor()
        try {
            ServerSocket(0, 1, InetAddress.getLoopbackAddress()).use { server ->
                server.soTimeout = 3000
                val peer = worker.submit {
                    server.accept().use { socket ->
                        socket.soTimeout = 3000
                        AdbWire.read(socket.getInputStream())
                        Thread.sleep(700)
                    }
                }
                AdbWire().use { client ->
                    try {
                        client.connect(server.localPort, key, 100, Runnable {})
                        fail("Silent daemon was accepted")
                    } catch (_: SocketTimeoutException) { }
                }
                peer.get(4, TimeUnit.SECONDS)
            }
        } finally { worker.shutdownNow() }
    }
}
