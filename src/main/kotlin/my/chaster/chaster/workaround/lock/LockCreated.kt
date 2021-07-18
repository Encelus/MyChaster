package my.chaster.chaster.workaround.lock

data class LockCreated(
	val lockId: LockId,
	val description: String,
)
