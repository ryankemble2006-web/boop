package com.boop.shieldturbo.model

enum class ProbeStatus { AVAILABLE, RESTRICTED, UNSUPPORTED, ERROR }
data class ProbeResult(val key: String, val label: String, val status: ProbeStatus, val value: String, val evidence: String)
