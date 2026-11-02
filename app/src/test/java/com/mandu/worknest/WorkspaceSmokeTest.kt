package com.mandu.worknest

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspaceSmokeTest {
    @Test fun workspacePackageIsStable() { assertEquals("com.mandu.worknest", MainActivity::class.java.packageName) }
    @Test fun taggedAppVersionIsInitialRelease() { assertTrue("1.0.0".matches(Regex("\\d+\\.\\d+\\.\\d+"))) }
}
