package com.boop.shieldturbo.system

enum class Level { OK, WARNING }

data class QuickCheckFinding(val level: Level, val message: String)

object QuickCheck {
    fun storage(freeBytes: Long, totalBytes: Long): QuickCheckFinding {
        if (totalBytes <= 0L) return QuickCheckFinding(Level.OK, "Storage capacity not exposed")
        val percent = ((freeBytes.coerceAtLeast(0L) * 100L) / totalBytes).coerceIn(0L, 100L)
        return if (percent < 5L) {
            QuickCheckFinding(Level.WARNING, "Internal storage is low: $percent% free")
        } else {
            QuickCheckFinding(Level.OK, "Internal storage has $percent% free")
        }
    }
}
