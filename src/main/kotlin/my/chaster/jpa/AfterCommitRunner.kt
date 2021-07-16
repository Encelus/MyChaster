package my.chaster.jpa

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import javax.transaction.Transactional

@Component
@Transactional(Transactional.TxType.MANDATORY)
class AfterCommitRunner {

	fun execute(runnable: () -> Unit) {
		TransactionSynchronizationManager.registerSynchronization(
			object : TransactionSynchronization {
				override fun afterCompletion(status: Int) {
					if (status == TransactionSynchronization.STATUS_COMMITTED) {
						try {
							runnable.invoke()
						} catch (e: Exception) {
							LOGGER.error("Failed to invoke after commit hook", e)
						}
					}
				}
			},
		)
	}

	companion object {
		private val LOGGER = LoggerFactory.getLogger(AfterCommitRunner::class.java)
	}
}