package my.chaster.messaging

import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Component
class TestMessagingConsumer : MessagingConsumer<TestMessage> {

	val receivedMessages = mutableListOf<TestMessage>()
	private val lock = ReentrantLock()
	private val condition = lock.newCondition()

	override fun handle(message: TestMessage) {
		try {
			if (message.consumerShouldThrowException) {
				throw IllegalStateException("I should be thrown...")
			}
		} finally {
			lock.withLock {
				receivedMessages.add(message)
				condition.signalAll()
			}
		}
	}

	fun awaitMessage(expectedMessage: TestMessage): Boolean {
		while (true) {
			lock.withLock {
				if (receivedMessages.contains(expectedMessage)) {
					return true
				}

				if (!condition.await(3, TimeUnit.SECONDS)) {
					return false
				}
			}
		}
	}

}