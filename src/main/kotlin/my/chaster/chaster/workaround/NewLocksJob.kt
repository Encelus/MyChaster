package my.chaster.chaster.workaround

import my.chaster.chaster.getChasterId
import my.chaster.chaster.workaround.lock.Lock
import my.chaster.chaster.workaround.lock.LockCreated
import my.chaster.chaster.workaround.lock.LockRepository
import my.chaster.gen.chaster.api.KeyholderApi
import my.chaster.gen.chaster.model.LockForKeyholder
import my.chaster.messaging.MessagingPublisher
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
@Transactional
class NewLocksJob(
	private val keyholderApi: KeyholderApi,
	private val lockRepository: LockRepository,
	private val messagingPublisher: MessagingPublisher,
) {

	@Scheduled(initialDelayString = "PT30S", fixedDelayString = "PT5M")
	fun lookForNewLocks(): List<Lock> {
		val activeChasterLocks = keyholderApi.keyholderControllerGetKeyholderWearers(LockForKeyholder.StatusEnum.LOCKED.value)

		val newLocks = activeChasterLocks.asSequence()
			.filter { isNew(it) }
			.map { createLock(it) }
			.toList()
		LOGGER.info("Found new locks: $newLocks")
		return newLocks
	}

	private fun isNew(chasterLock: LockForKeyholder): Boolean {
		return lockRepository.findByChasterLockId(chasterLock.getChasterId()) == null
	}

	private fun createLock(chasterLock: LockForKeyholder): Lock {
		val lock = lockRepository.save(
			Lock(chasterLock.getChasterId(), chasterLock.user.getChasterId()),
		)

		messagingPublisher.publish(
			LockCreated(
				lock.id,
				chasterLock.sharedLock.description,
			),
		)
		return lock
	}

	companion object {
		private val LOGGER = LoggerFactory.getLogger(NewLocksJob::class.java)
	}
}