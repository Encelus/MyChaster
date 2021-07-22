package my.chaster.extension.fitness.stepsperperiod

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import javax.transaction.Transactional

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