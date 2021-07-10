package my.chaster.util

import java.time.Instant

class InstantExt

fun Instant.isBeforeOrEqual(other: Instant): Boolean {
	return !isAfter(other)
}

fun Instant.isAfterOrEqual(other: Instant): Boolean {
	return !isBefore(other)
}