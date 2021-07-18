package my.chaster.chaster.action

import my.chaster.gen.chaster.api.LocksApi
import my.chaster.gen.chaster.model.UpdateTimeDto
import my.chaster.messaging.MessagingConsumer
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class UpdateLockTimeMessageConsumer(
	private val locksApi: LocksApi,
) : MessagingConsumer<UpdateLockTime> {

	override fun handle(message: UpdateLockTime) {
		val dto = UpdateTimeDto()
			.duration(BigDecimal(message.duration.toSeconds()))
		locksApi.lockControllerUpdateTime(dto, message.chasterLockId.id)
	}

}