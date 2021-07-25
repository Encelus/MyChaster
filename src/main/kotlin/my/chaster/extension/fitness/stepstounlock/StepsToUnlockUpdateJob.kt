package my.chaster.extension.fitness.stepstounlock

import my.chaster.extension.fitness.ExtensionProperties
import my.chaster.extension.fitness.stepstounlock.workaround.config.StepsToUnlockConfigRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@ConditionalOnProperty(ExtensionProperties.STEPS_TO_UNLOCK_ENABLED, havingValue = "true", matchIfMissing = false)
@Component
class StepsToUnlockUpdateJob(
	private val stepsToUnlockService: StepsToUnlockService,
	private val stepsToUnlockConfigRepository: StepsToUnlockConfigRepository,
) {

	@Scheduled(initialDelayString = "PT30S", fixedDelayString = "PT45M")
	@Transactional(Transactional.TxType.NEVER)
	fun updateAllOngoingLocks() {
		val activeLockIds = stepsToUnlockConfigRepository.findAllActiveChasterLockIds()
		for (activeLockId in activeLockIds) {
			stepsToUnlockService.getCurrentSteps(activeLockId)
			LOGGER.info("Updated steps for $activeLockId")
		}
	}

	companion object {
		private val LOGGER = LoggerFactory.getLogger(StepsToUnlockUpdateJob::class.java)
	}
}