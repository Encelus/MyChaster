package my.chaster.extension.fitness.stepsperperiod

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
		activeLockIds.forEach { stepsPerPeriodService.loadHistory(it) }
	}
}