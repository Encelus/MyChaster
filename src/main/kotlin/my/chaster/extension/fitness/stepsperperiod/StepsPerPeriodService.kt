package my.chaster.extension.fitness.stepsperperiod

import my.chaster.chaster.ChasterLockId
import my.chaster.chaster.ChasterLockService
import my.chaster.chaster.getChasterId
import my.chaster.extension.fitness.stepsperperiod.workaround.config.StepsPerPeriodConfig
import my.chaster.extension.fitness.stepsperperiod.workaround.config.StepsPerPeriodConfigRepository
import my.chaster.fitness.GoogleFitnessService
import my.chaster.gen.chaster.api.LocksApi
import my.chaster.gen.chaster.model.LockForPublic
import org.springframework.stereotype.Service
import java.time.Instant
import javax.transaction.Transactional

@Service
@Transactional
class StepsPerPeriodService(
	private val stepsPerPeriodHistoryRepository: StepsPerPeriodHistoryRepository,
	private val stepsPerPeriodConfigRepository: StepsPerPeriodConfigRepository,
	private val googleFitnessService: GoogleFitnessService,
	private val locksApi: LocksApi,
	private val chasterLockService: ChasterLockService,
) {

	fun loadHistory(chasterLockId: ChasterLockId): Set<StepsPerPeriodHistory> {
		val lock = locksApi.lockControllerFindOne(chasterLockId.id)
		val config = stepsPerPeriodConfigRepository.findByChasterLockIdOrThrow(chasterLockId)

		val existingHistory = stepsPerPeriodHistoryRepository.findAllByChasterLockId(chasterLockId)
		val fullHistory = tryCreatingMissingHistoryEntries(lock, config, existingHistory)

		updateNonFinalHistory(config, fullHistory)
		return fullHistory
	}

	private fun tryCreatingMissingHistoryEntries(lock: LockForPublic, config: StepsPerPeriodConfig, existingHistory: Set<StepsPerPeriodHistory>): Set<StepsPerPeriodHistory> {
		val createHistoryFrom = existingHistory.maxOfOrNull { it.periodEnd } ?: lock.startDate.toInstant()
		val createHistoryUntil = if (lock.endDate != null) {
			if (lock.endDate.toInstant().isAfter(Instant.now())) Instant.now() else lock.endDate.toInstant()
		} else {
			Instant.now()
		}

		val createdHistory = createHistory(createHistoryFrom, createHistoryUntil, lock, config)
		return existingHistory + createdHistory
	}

	private fun createHistory(start: Instant, end: Instant, currentLock: LockForPublic, config: StepsPerPeriodConfig): Set<StepsPerPeriodHistory> {
		val createdHistoryEntries = mutableSetOf<StepsPerPeriodHistory>()

		var periodStart = start
		while (periodStart.isBefore(end)) {
			val periodEnd = periodStart.plus(config.period)
			val historyEntry = stepsPerPeriodHistoryRepository.save(
				StepsPerPeriodHistory(
					currentLock.user.getChasterId(),
					currentLock.getChasterId(),
					periodStart,
					periodEnd,
					0,
				),
			)
			createdHistoryEntries.add(historyEntry)
			periodStart = periodEnd
		}

		return createdHistoryEntries
	}

	private fun updateNonFinalHistory(config: StepsPerPeriodConfig, history: Set<StepsPerPeriodHistory>) {
		for (entry in history) {
			if (entry.isFinal) {
				continue
			}

			val steps = googleFitnessService.getSteps(entry.chasterUserId, entry.periodStart, entry.periodEnd)
			entry.steps = steps

			if (!entry.isActive(Instant.now())) {
				finalizeHistory(config, entry)
			}
		}
	}

	private fun finalizeHistory(config: StepsPerPeriodConfig, history: StepsPerPeriodHistory) {
		val punishment = if (history.steps < config.requiredSteps) {
			chasterLockService.addTime(config.chasterLockId, config.penalty)
			config.penalty
		} else {
			null
		}
		history.setFinal(punishment)
	}
}