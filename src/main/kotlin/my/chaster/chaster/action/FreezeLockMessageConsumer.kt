package my.chaster.chaster.action

import my.chaster.gen.chaster.api.LocksApi
import my.chaster.gen.chaster.model.SetFreezeDto
import my.chaster.messaging.MessagingConsumer
import org.springframework.stereotype.Component

@Component
class FreezeLockMessageConsumer(
	private val locksApi: LocksApi,
) : MessagingConsumer<FreezeLock>() {

	override fun handle(message: FreezeLock) {
		val dto = SetFreezeDto()
			.isFrozen(message.freeze)
		locksApi.lockControllerSetFreeze(dto, message.chasterLockId.id)
	}

}