package my.chaster.extension.fitness.stepsperperiod.workaround

import my.chaster.chaster.workaround.lock.LockCreated
import my.chaster.chaster.workaround.lock.LockRepository
import my.chaster.extension.fitness.ExtensionProperties
import my.chaster.extension.fitness.stepsperperiod.StepsPerPeriodConfigView
import my.chaster.gen.chaster.api.MessagingApi
import my.chaster.gen.chaster.model.ConversationForPublic
import my.chaster.gen.chaster.model.CreateConversationDto
import my.chaster.messaging.MessagingConsumer
import my.chaster.views.UrlBuilder
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@ConditionalOnProperty(ExtensionProperties.STEPS_PER_PERIOD_ENABLED, havingValue = "true", matchIfMissing = false)
@Component
@Transactional
class StepsPerPeriodLockCreatedConsumer(
	private val lockRepository: LockRepository,
	private val messagingApi: MessagingApi,
	private val urlBuilder: UrlBuilder,
) : MessagingConsumer<LockCreated>() {

	override fun handle(message: LockCreated): ConversationForPublic? {
		if (!message.description.contains("StepsPerPeriod-Extension")) {
			return null
		}

		return sendMessageWithConfigUrl(message)
	}

	private fun sendMessageWithConfigUrl(lockCreated: LockCreated): ConversationForPublic {
		val lock = lockRepository.findOrThrow(lockCreated.lockId)

		val createConversationDto = CreateConversationDto()
			.addUsersItem(lock.chasterUserId.id)
			.type(CreateConversationDto.TypeEnum.PRIVATE)
			.message(
				"""Welcome to a lock using the StepsPerPeriod-Extension.
				To configure and activate the extension please visit ${urlBuilder.build(StepsPerPeriodConfigView.ROUTE, lock.fakeApiKey)}
				""".trimIndent(),
			)
		return messagingApi.messagingControllerCreateConversation(createConversationDto)
	}
}