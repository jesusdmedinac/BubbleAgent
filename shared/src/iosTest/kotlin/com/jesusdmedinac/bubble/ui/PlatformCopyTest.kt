package com.jesusdmedinac.bubble.ui

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlatformCopyTest {
    private val androidOnlyNames = listOf("Android", "Gemini", "Snapdragon", "MCP", "NPU")

    @Test
    fun iosCopy_namesNoAndroidTechnology() {
        val texts = listOf(
            platformCopy.assistantBadge,
            platformCopy.deviceEngine,
            platformCopy.vaultStatus,
            platformCopy.vaultDescription,
            platformCopy.habitsFilter,
            platformCopy.screenTimeAccessHint,
            platformCopy.screenTimeAccessDescription,
            platformCopy.screenTimePrivacyNote,
        )
        for (text in texts) {
            for (name in androidOnlyNames) {
                assertFalse(text.contains(name, ignoreCase = true), "\"$text\" mentions $name")
            }
        }
    }

    @Test
    fun iosCopy_hidesAndroidOnlyControls() {
        assertNull(platformCopy.screenTimeAccessAction)
        assertTrue(!platformCopy.showsAssistantToolsBridge)
    }
}
