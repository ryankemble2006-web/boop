package com.boop.shieldturbo.performance

import android.content.Context
import com.boop.shieldturbo.power.LocalBridge

class ProcessorModeActuatorProbe(context: Context) {
    private val bridge = LocalBridge(context)

    fun runProof(): ProcessorModeActuatorProofResult = bridge.withTrustedAdb { adb ->
        ProcessorModeActuatorProof.run(
            readState = {
                ProcessorModeActuatorPolicy.parseState(
                    ProcessorModeActuatorPolicy.readCommands.map { command ->
                        bridge.checked(adb, command)
                    }
                )
            },
            writeMode = { mode ->
                bridge.checked(adb, ProcessorModeActuatorPolicy.writeModeCommand(mode))
            },
            pause = { millis -> Thread.sleep(millis) }
        )
    }
}
