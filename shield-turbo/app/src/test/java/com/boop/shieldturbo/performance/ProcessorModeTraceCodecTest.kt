package com.boop.shieldturbo.performance

import org.junit.Assert.assertEquals
import org.junit.Test

class ProcessorModeTraceCodecTest {
    @Test fun roundTripsSnapshotWithDelimitersAndNewlines() {
        val snapshot = ProcessorModeTrace.snapshotFromText(
            global = "alpha=value with spaces\npipe=a|b\nequals=a=b=c\n",
            secure = "multiline=first\\nsecond\n",
            system = "unicode=MAX ⚡\n",
            properties = "[persist.vendor.test]: [x:y=z]\n"
        )

        val encoded = ProcessorModeTraceCodec.encode(snapshot)
        val decoded = ProcessorModeTraceCodec.decode(encoded)

        assertEquals(emptyList<ProcessorModeTraceChange>(), ProcessorModeTrace.diff(snapshot, decoded))
    }
}
