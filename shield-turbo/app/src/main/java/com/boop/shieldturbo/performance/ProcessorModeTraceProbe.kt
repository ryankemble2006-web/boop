package com.boop.shieldturbo.performance

import android.content.Context
import com.boop.shieldturbo.power.LocalBridge

class ProcessorModeTraceProbe(context: Context) {
    private val bridge = LocalBridge(context)

    fun capture(): ProcessorModeTraceSnapshot = bridge.withTrustedAdb { adb ->
        ProcessorModeTraceCapture.capture { command -> bridge.checked(adb, command) }
    }
}
