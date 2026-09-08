package com.boop.shieldturbo.power

import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.security.KeyPairGenerator
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class AdbWireStreamLifecycleTest {
    @Test
    fun latePacketForClosedStreamDoesNotBreakNextCommand() {
        val key = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        val worker = Executors.newSingleThreadExecutor()
        try {
            ServerSocket(0, 1, InetAddress.getLoopbackAddress()).use { server ->
                val fixture = worker.submit {
                    server.accept().use { socket ->
                        socket.soTimeout = 5000
                        val input = socket.getInputStream()
                        val output = socket.getOutputStream()

                        check(AdbWire.read(input).command == AdbWire.CNXN)
                        output.write(AdbWire.encode(AdbWire.CNXN, 0x01000000, 4096, "device::\u0000".toByteArray(StandardCharsets.UTF_8)))
                        output.flush()

                        val firstOpen = AdbWire.read(input)
                        check(firstOpen.command == AdbWire.OPEN)
                        val firstMarker = marker(firstOpen)
                        val firstLocal = firstOpen.arg0
                        val firstRemote = 41
                        output.write(AdbWire.encode(AdbWire.OKAY, firstRemote, firstLocal, byteArrayOf()))
                        output.write(AdbWire.encode(AdbWire.WRTE, firstRemote, firstLocal, "\n${firstMarker}0\n".toByteArray(StandardCharsets.UTF_8)))
                        output.flush()
                        check(AdbWire.read(input).command == AdbWire.OKAY)
                        output.write(AdbWire.encode(AdbWire.CLSE, firstRemote, firstLocal, byteArrayOf()))
                        output.flush()

                        var secondOpen = AdbWire.read(input)
                        while (secondOpen.command == AdbWire.CLSE) secondOpen = AdbWire.read(input)
                        check(secondOpen.command == AdbWire.OPEN)
                        val secondMarker = marker(secondOpen)
                        val secondLocal = secondOpen.arg0
                        val secondRemote = 42

                        // Android's ADB protocol permits already-in-flight traffic from a just-closed
                        // stream to arrive late. A single-stream client must ignore that old traffic.
                        output.write(AdbWire.encode(AdbWire.CLSE, firstRemote, firstLocal, byteArrayOf()))
                        output.write(AdbWire.encode(AdbWire.OKAY, secondRemote, secondLocal, byteArrayOf()))
                        output.write(AdbWire.encode(AdbWire.WRTE, secondRemote, secondLocal, "\n${secondMarker}0\n".toByteArray(StandardCharsets.UTF_8)))
                        output.flush()
                        check(AdbWire.read(input).command == AdbWire.OKAY)
                        output.write(AdbWire.encode(AdbWire.CLSE, secondRemote, secondLocal, byteArrayOf()))
                        output.flush()
                    }
                }

                AdbWire().use { client ->
                    client.connect(server.localPort, key, 5000, {})
                    assertEquals(0, client.execute("true", 5000).exitCode)
                    assertEquals(0, client.execute("true", 5000).exitCode)
                }
                fixture.get(6, TimeUnit.SECONDS)
            }
        } finally {
            worker.shutdownNow()
        }
    }

    private fun marker(open: AdbWire.Packet): String {
        val command = String(open.payload, StandardCharsets.UTF_8)
        val match = Pattern.compile("__TURBO_RC_[0-9a-f]+__").matcher(command)
        check(match.find())
        return match.group()
    }
}
