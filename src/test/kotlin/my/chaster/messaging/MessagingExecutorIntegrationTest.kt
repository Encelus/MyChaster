package my.chaster.messaging

import my.chaster.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class MessagingExecutorIntegrationTest : AbstractIntegrationTest() {

	@Autowired
	private lateinit var testee: MessagingExecutor

	@Autowired
	private lateinit var applicationMessageTestData: ApplicationMessageTestData

	@Autowired
	private lateinit var testMessagingConsumer: TestMessagingConsumer

	@Autowired
	private lateinit var applicationMessageRepository: ApplicationMessageRepository


	@Test
	fun `fetchAndExecuteMessages should execute all tasks when more than page size`() {
		//given
		val testMessages = IntRange(1, MessagingExecutor.PAGE_SIZE + 1)
			.map { TestMessage.random() }
		val applicationMessages = testMessages.map { applicationMessageTestData.create("testMessagingConsumer", it) }


		//when
		END_TRANSACTION()
		testee.fetchAndExecuteMessages()
		START_TRANSACTION()

		//then
		testMessagingConsumer.awaitMessage(testMessages.last())
		assertThat(testMessagingConsumer.receivedMessages)
			.containsSubsequence(testMessages)
			.containsOnlyOnce(*testMessages.toTypedArray())

		assertThat(applicationMessages).isNotEmpty
			.allSatisfy { assertThat(applicationMessageRepository.findById(it.id)).isNotPresent }
	}

	@Test
	fun `fetchAndExecuteMessages should persist exception and not stop other messages`() {
		//given
		val testMessages = listOf(
			TestMessage.random(consumerShouldThrowException = true),
			TestMessage.random(consumerShouldThrowException = false),
		)
		val applicationMessages = testMessages.map { applicationMessageTestData.create("testMessagingConsumer", it) }

		//when
		END_TRANSACTION()
		testee.fetchAndExecuteMessages()
		START_TRANSACTION()

		//then
		testMessagingConsumer.awaitMessage(testMessages.last())
		assertThat(testMessagingConsumer.receivedMessages)
			.containsSubsequence(testMessages)
			.containsOnlyOnce(*testMessages.toTypedArray())

		assertThat(applicationMessageRepository.findById(applicationMessages[0].id))
			.isPresent
			.satisfies { failedMessage ->
				assertThat(failedMessage.get().failure).isEqualTo("[java.lang.IllegalStateException] I should be thrown...")
			}
		assertThat(applicationMessageRepository.findById(applicationMessages[1].id)).isNotPresent
	}

	@Test
	fun `fetchAndExecuteMessages should not execute failed messages`() {
		//given
		val testMessages = listOf(
			TestMessage.random(),
			TestMessage.random(),
		)
		val applicationMessages = listOf(
			applicationMessageTestData.create("testMessagingConsumer", testMessages[0], "some failure reason"),
			applicationMessageTestData.create("testMessagingConsumer", testMessages[1]),
		)

		//when
		END_TRANSACTION()
		testee.fetchAndExecuteMessages()
		START_TRANSACTION()

		//then
		testMessagingConsumer.awaitMessage(testMessages.last())
		assertThat(testMessagingConsumer.receivedMessages)
			.contains(testMessages[1])
			.doesNotContain(testMessages[0]) //failed one

		assertThat(applicationMessageRepository.findById(applicationMessages[0].id)).isPresent
		assertThat(applicationMessageRepository.findById(applicationMessages[1].id)).isNotPresent
	}
}