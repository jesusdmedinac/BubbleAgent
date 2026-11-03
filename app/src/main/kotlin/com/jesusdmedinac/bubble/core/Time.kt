package com.jesusdmedinac.bubble.core

import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Random identifier in the standard UUID text form, usable from common code. */
@OptIn(ExperimentalUuidApi::class)
fun randomId(): String = Uuid.random().toString()

/** Current wall-clock time in epoch milliseconds, usable from common code. */
fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
