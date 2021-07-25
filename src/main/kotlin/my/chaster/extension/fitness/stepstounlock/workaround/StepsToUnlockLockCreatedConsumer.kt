package my.chaster.extension.fitness.stepstounlock.workaround

import my.chaster.chaster.ChasterLockId
import my.chaster.chaster.ChasterLockService
import my.chaster.chaster.workaround.lock.Lock
import my.chaster.chaster.workaround.lock.LockCreated
import my.chaster.chaster.workaround.lock.LockRepository
import my.chaster.extension.fitness.ExtensionProperties
import my.chaster.extension.fitness.stepstounlock.StepsToUnlockView
import my.chaster.extension.fitness.stepstounlock.workaround.config.StepsToUnlockConfig
import my.chaster.extension.fitness.stepstounlock.workaround.config.StepsToUnlockConfigRepository
import my.chaster.gen.chaster.api.MessagingApi
import my.chaster.gen.chaster.model.ConversationForPublic
import my.chaster.gen.chaster.model.CreateConversationDto
import my.chaster.messaging.MessagingConsumer
import my.chaster.views.UrlBuilder
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import javax.transaction.Transactional


@ConditionalOnProperty(ExtensionProperties.STEPS_TO_UNLOCK_ENABLED, havingValue = "true", matchIfMissing = false)
@Component
@Transactional
class StepsToUnlockLockCreatedConsumer(
	private val stepsToUnlockConfigRepository: StepsToUnlockConfigRepository,
	private val lockRepository: LockRepository,
	private val chasterLockService: ChasterLockService,
	private val messagingApi: MessagingApi,
	private val urlBuilder: UrlBuilder,
) : MessagingConsumer<LockCreated>() {

	override fun handle(message: LockCreated): StepsToUnlockConfig? {
		if (!message.description.contains("StepsToUnlock-Extension")) {
			return null
		}

		val maybeRequiredSteps = REQUIRED_STEPS_REGEX.find(message.description)?.groupValues?.get(1)?.toIntOrNull()
		if (maybeRequiredSteps == null) {
			LOGGER.warn("Found StepsToUnlock extension without required steps configured. " + message.description)
			return null
		}

		val lock = lockRepository.findOrThrow(message.lockId)
		val config = persistConfig(lock.chasterLockId, maybeRequiredSteps)
		sendMessageWithViewUrl(lock, config)
		chasterLockService.freeze(lock.chasterLockId)

		return config
	}

	private fun persistConfig(chasterLockId: ChasterLockId, requiredSteps: Int): StepsToUnlockConfig {
		return stepsToUnlockConfigRepository.save(
			StepsToUnlockConfig(chasterLockId, requiredSteps),
		)
	}

	private fun sendMessageWithViewUrl(lock: Lock, stepsToUnlockConfig: StepsToUnlockConfig): ConversationForPublic {
		val createConversationDto = CreateConversationDto()
			.addUsersItem(lock.chasterUserId.id)
			.type(CreateConversationDto.TypeEnum.PRIVATE)
			.message(
				"""Welcome to a lock using the StepsToUnlock-Extension.
				Your look will be frozen until you fulfilled your required ${stepsToUnlockConfig.requiredSteps} steps.
				Please visit ${urlBuilder.build(StepsToUnlockView.ROUTE, lock.fakeApiKey)} and authenticate with your GoogleFit user
				to allow this extension to track your steps and unfreeze your lock when your are done.
				You can also visit this website to check your progress.
				""".trimIndent(),
			)
		return messagingApi.messagingControllerCreateConversation(createConversationDto)
	}

	companion object {
		private val LOGGER = LoggerFactory.getLogger(StepsToUnlockLockCreatedConsumer::class.java)

		val REQUIRED_STEPS_REGEX = Regex("Required steps to unlock: (\\d+)", RegexOption.IGNORE_CASE)
	}
}