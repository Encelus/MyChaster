package my.chaster.chaster

import my.chaster.chaster.action.FreezeLock
import my.chaster.chaster.action.UpdateLockTime
import my.chaster.messaging.MessagingPublisher
import org.springframework.stereotype.Service
import java.time.Duration
import javax.transaction.Transactional

@Service
@Transactional
class ChasterLockService(
	private val messagingPublisher: MessagingPublisher,
) {

	fun freeze(chasterLockId: ChasterLockId) {
		messagingPublisher.publish(FreezeLock(chasterLockId, true))
	}

	fun unfreeze(chasterLockId: ChasterLockId) {
		messagingPublisher.publish(FreezeLock(chasterLockId, false))
	}

	fun addTime(chasterLockId: ChasterLockId, duration: Duration) {
		check(!duration.isNegative)
		messagingPublisher.publish(UpdateLockTime(chasterLockId, duration))
	}

	fun removeTime(chasterLockId: ChasterLockId, duration: Duration) {
		check(!duration.isNegative)
		messagingPublisher.publish(UpdateLockTime(chasterLockId, duration.negated()))
	}
}