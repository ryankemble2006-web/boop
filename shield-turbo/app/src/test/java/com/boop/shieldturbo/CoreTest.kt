package com.boop.shieldturbo

import com.boop.shieldturbo.analysis.*
import com.boop.shieldturbo.model.*
import com.boop.shieldturbo.probe.*
import com.boop.shieldturbo.privilege.*
import org.junit.Assert.*
import org.junit.Test

class CoreTest {
 @Test fun resultRetainsEvidence(){ val r=ProbeResult("x","X",ProbeStatus.RESTRICTED,"Restricted","denied"); assertEquals("denied",r.evidence) }
 @Test fun analyzerPreservesOrder(){ val a=Probe{ProbeResult("a","A",ProbeStatus.AVAILABLE,"1","ok")};val b=Probe{ProbeResult("b","B",ProbeStatus.UNSUPPORTED,"n/a","missing")};val s=ShieldAnalyzer(listOf(a,b)){123}.analyze();assertEquals(123,s.capturedAtMillis);assertEquals(listOf("a","b"),s.results.map{it.key}) }
 @Test fun privilegePrecedence(){ assertEquals(PrivilegeTier.STANDARD,PrivilegeDetector({false},{false}).detect());assertEquals(PrivilegeTier.ADB_TURBO,PrivilegeDetector({true},{false}).detect());assertEquals(PrivilegeTier.ROOT,PrivilegeDetector({true},{true}).detect()) }
 @Test fun formatsGiB(){ assertEquals("1.00 GiB",gib(1073741824)) }
}
