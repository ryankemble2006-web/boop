package com.boop.shieldturbo.performance

import android.content.Context
import com.boop.shieldturbo.power.LocalBridge

class NvidiaTurboPort(context: Context) : TurboControlPort {
    private val bridge = LocalBridge(context)

    override fun readState(): ProcessorModeActuatorState = bridge.withTrustedAdb { adb ->
        ProcessorModeActuatorPolicy.parseState(
            ProcessorModeActuatorPolicy.readCommands.map { command ->
                bridge.checked(adb, command)
            }
        )
    }

    override fun writeMode(mode: Int) {
        bridge.withTrustedAdb { adb ->
            bridge.checked(adb, ProcessorModeActuatorPolicy.writeModeCommand(mode))
        }
    }

    override fun waitForState(expected: ProcessorModeActuatorState): ProcessorModeActuatorState {
        var last = readState()
        repeat(2) {
            if (last == expected) return last
            Thread.sleep(250L)
            last = readState()
        }
        return last
    }
}
