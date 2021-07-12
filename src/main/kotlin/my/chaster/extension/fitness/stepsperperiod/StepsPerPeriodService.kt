package my.chaster.extension.fitness.stepsperperiod

import my.chaster.chaster.getChasterId
import my.chaster.fitness.GoogleFitnessService
import my.chaster.gen.chaster.model.LockForPublic
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import javax.transaction.Transactional

@Service
@Transactional
class StepsPerPeriodService(
	private val stepsPerPeriodHistoryRepository: StepsPerPeriodHistoryRepository,
	private val googleFitnessService: GoogleFitnessService,
) {

	fun loadHistory(currentLock: LockForPublic): Set<StepsPerPeriodHistory> {
		val existingHistory = stepsPerPeriodHistoryRepository.findAllByChasterLockId(currentLock.getChasterId())
		val fullHistory = tryCreatingMissingHistoryEntries(currentLock, existingHistory)

		updateNonFinalHistory(currentLock, fullHistory)
		return fullHistory
	}

	private fun tryCreatingMissingHistoryEntries(currentLock: LockForPublic, existingHistory: Set<StepsPerPeriodHistory>): Set<StepsPerPeriodHistory> {
		val createHistoryFrom = existingHistory.maxOfOrNull { it.periodEnd } ?: currentLock.startDate.toInstant()
		val createHistoryUntil = if (currentLock.endDate != null) {
			currentLock.endDate.toInstant()
		} else {
			Instant.now()
		}

		val createdHistory = createHistory(createHistoryFrom, createHistoryUntil, currentLock)
		return existingHistory + createdHistory
	}

	private fun createHistory(start: Instant, end: Instant, currentLock: LockForPublic): Set<StepsPerPeriodHistory> {
		val createdHistoryEntries = mutableSetOf<StepsPerPeriodHistory>()
		val period = Duration.ofDays(1)

		var periodStart = start
		while (periodStart.isBefore(end)) {
			val periodEnd = periodStart.plus(period)
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

	private fun updateNonFinalHistory(currentLock: LockForPublic, history: Set<StepsPerPeriodHistory>) {
		for (entry in history) {
			if (entry.isFinal) {
				continue
			}

			val steps = googleFitnessService.getSteps(entry.chasterUserId, entry.periodStart, entry.periodEnd)
			entry.steps = steps

			if (!entry.isActive(Instant.now())) {
				finalizeHistory(currentLock, entry)
			}
		}
	}

	private fun finalizeHistory(currentLock: LockForPublic, history: StepsPerPeriodHistory) {
		val requiredSteps = 2500

		val punishment = if (history.steps < requiredSteps) {
			Duration.ofHours(12)
		} else {
			null
		}
		history.setFinal(punishment)
	}
}