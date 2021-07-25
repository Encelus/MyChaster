package my.chaster.extension.fitness.stepsperperiod

import my.chaster.extension.fitness.ExtensionProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@ConditionalOnProperty(ExtensionProperties.STEPS_PER_PERIOD_ENABLED, havingValue = "true", matchIfMissing = false)
@Component
class StepsPerPeriodUpdateJob(
	private val stepsPerPeriodService: StepsPerPeriodService,
	private val stepsPerPeriodHistoryRepository: StepsPerPeriodHistoryRepository,
) {

	@Scheduled(initialDelayString = "PT30S", fixedDelayString = "PT45M")
	@Transactional(Transactional.TxType.NEVER)
	fun updateAllOngoingLocks() {
		val activeLockIds = stepsPerPeriodHistoryRepository.findAllActiveChasterLockIds()
		for (activeLockId in activeLockIds) {
			stepsPerPeriodService.loadHistory(activeLockId)
			LOGGER.info("Updated history for $activeLockId")
		}
	}

	companion object {
		private val LOGGER = LoggerFactory.getLogger(StepsPerPeriodUpdateJob::class.java)
	}
}