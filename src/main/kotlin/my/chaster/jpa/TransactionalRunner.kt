package my.chaster.jpa

import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class TransactionalRunner {

	@Transactional(Transactional.TxType.REQUIRES_NEW)
	fun <T> runInNewTransaction(runnable: () -> T): T {
		return runnable.invoke()
	}
}