package com.jesusdmedinac.bubble.ui

/**
 * UI texts that name the technology behind Bubble, which differs per platform
 * (Gemini Nano, usage access and AppFunctions on Android; none of those on iOS).
 */
data class PlatformCopy(
    /** Badge under the greeting of an empty chat. */
    val assistantBadge: String,
    /** Engine line on the profile card. */
    val deviceEngine: String,
    /** Status pill next to the Vault title. */
    val vaultStatus: String,
    /** Explanation under the Vault title. */
    val vaultDescription: String,
    /** Vault filter for habits the assistant detected. */
    val habitsFilter: String,
    /** Subtitle of the banner shown while real screen time is not available. */
    val screenTimeAccessHint: String,
    /** Body of that banner. */
    val screenTimeAccessDescription: String,
    /** Button that opens the system setting; null when the platform has none. */
    val screenTimeAccessAction: String?,
    /** Privacy note at the bottom of that banner. */
    val screenTimePrivacyNote: String,
    /** Whether to show the box about the tools exposed to the system assistant (AppFunctions). */
    val showsAssistantToolsBridge: Boolean,
)

expect val platformCopy: PlatformCopy
