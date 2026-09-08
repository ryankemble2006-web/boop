package com.boop.shieldturbo.performance

import android.content.Context

class ProcessorModeTraceStore(context: Context) {
    private val prefs = context.getSharedPreferences("processor_mode_trace", Context.MODE_PRIVATE)

    fun saveOptimized(snapshot: ProcessorModeTraceSnapshot): Boolean = prefs.edit()
        .putString("optimized_snapshot", ProcessorModeTraceCodec.encode(snapshot))
        .commit()

    fun loadOptimized(): ProcessorModeTraceSnapshot? = prefs
        .getString("optimized_snapshot", null)
        ?.let { encoded -> runCatching { ProcessorModeTraceCodec.decode(encoded) }.getOrNull() }

    fun clearOptimized(): Boolean = prefs.edit().remove("optimized_snapshot").commit()
}
