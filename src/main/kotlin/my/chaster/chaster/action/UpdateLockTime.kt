package my.chaster.chaster.action

import my.chaster.chaster.ChasterLockId
import java.time.Duration

data class UpdateLockTime(
	val chasterLockId: ChasterLockId,
	val duration: Duration,
)
