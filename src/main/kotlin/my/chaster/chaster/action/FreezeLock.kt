package my.chaster.chaster.action

import my.chaster.chaster.ChasterLockId

data class FreezeLock(
	val chasterLockId: ChasterLockId,
	val freeze: Boolean,
)