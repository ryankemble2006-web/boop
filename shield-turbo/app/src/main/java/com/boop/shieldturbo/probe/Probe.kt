package com.boop.shieldturbo.probe

import com.boop.shieldturbo.model.ProbeResult
fun interface Probe { fun read(): ProbeResult }
fun gib(bytes: Long): String = "%.2f GiB".format(java.util.Locale.US, bytes / 1073741824.0)
