package my.chaster.messaging

import my.chaster.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class MessagingPublisherIntegrationTest : AbstractIntegrationTest() {

	@Autowired
	private lateinit var testee: MessagingPublisher

	@Autowired
	private lateinit var applicationMessageRepository: ApplicationMessageRepository

	@Autowired
	private lateinit var testMessagingConsumer: TestMessagingConsumer


	@Test
	fun `publish should persist message`() {
		//given
		val testMessage = TestMessage.random()

		//when
		testee.publish(testMessage)

		//then
		assertThat(applicationMessageRepository.findAll()).anySatisfy { assertThat(it.payload).contains(testMessage.someValue) }
	}

	@Test
	fun `publish should trigger execution of message after commit`() {
		//given
		val testMessage = TestMessage.random()

		//when
		testee.publish(testMessage)
		COMMIT()

		//then
		testMessagingConsumer.awaitMessage(testMessage)
		assertThat(testMessagingConsumer.receivedMessages).contains(testMessage)

		assertThat(applicationMessageRepository.findAll()).noneSatisfy { assertThat(it.payload).contains(testMessage.someValue) }
	}

	@Test
	fun `publish should throw if trying to publish message that has no consumer`() {
		//given
		val messageWithoutConsumer = "Some string message"

		//when / then
		assertThatThrownBy { testee.publish(messageWithoutConsumer) }
			.isInstanceOf(IllegalStateException::class.java)
			.hasMessage("No Consumer for class java.lang.String")
	}
}