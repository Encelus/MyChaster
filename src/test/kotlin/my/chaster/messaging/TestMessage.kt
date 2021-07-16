package my.chaster.messaging

import java.util.UUID

data class TestMessage(
	val someValue: String,
	val consumerShouldThrowException: Boolean,
) {

	companion object {
		fun random(consumerShouldThrowException: Boolean = false): TestMessage {
			return TestMessage(UUID.randomUUID().toString(), consumerShouldThrowException)
		}
	}
}