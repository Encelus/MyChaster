package my.chaster.extension.fitness.stepstounlock

import my.chaster.chaster.ChasterLockId
import my.chaster.chaster.ChasterLockService
import my.chaster.chaster.ChasterUserId
import my.chaster.extension.fitness.stepstounlock.workaround.config.StepsToUnlockConfigRepository
import my.chaster.fitness.GoogleFitnessService
import my.chaster.gen.chaster.api.LocksApi
import org.springframework.stereotype.Service
import java.time.Instant
import javax.transaction.Transactional

@Service
@Transactional
class StepsToUnlockService(
	private val stepsToUnlockConfigRepository: StepsToUnlockConfigRepository,
	private val chasterLockService: ChasterLockService,
	private val googleFitnessService: GoogleFitnessService,
	private val locksApi: LocksApi,
) {

	fun getCurrentSteps(chasterLockId: ChasterLockId): Int {
		val lock = locksApi.lockControllerFindOne(chasterLockId.id)
		val currentSteps = googleFitnessService.getSteps(ChasterUserId(lock.user.id), lock.startDate.toInstant(), Instant.now())

		unfreezeIfNecessary(chasterLockId, currentSteps)
		return currentSteps
	}

	private fun unfreezeIfNecessary(chasterLockId: ChasterLockId, currentSteps: Int): Boolean {
		val config = stepsToUnlockConfigRepository.findByChasterLockIdOrThrow(chasterLockId)
		if (config.isFrozen && config.requiredSteps <= currentSteps) {
			chasterLockService.unfreeze(chasterLockId)
			return true
		}
		return false
	}

}