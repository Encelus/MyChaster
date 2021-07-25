package my.chaster.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import my.chaster.jpa.AfterCommitRunner
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.core.GenericTypeResolver
import org.springframework.stereotype.Component

@Component
class MessagingPublisher(
	private val applicationContext: ApplicationContext,
	private val applicationMessageRepository: ApplicationMessageRepository,
	private val messagingExecutor: MessagingExecutor,
	private val afterCommitRunner: AfterCommitRunner,
	private val objectMapper: ObjectMapper,
) {

	private val consumersByType: MutableMap<Class<*>, MutableSet<String>> = mutableMapOf()

	@Volatile
	private var publishedSomeMessage = false


	fun register(messageConsumer: MessagingConsumer<*>) {
		if (publishedSomeMessage) {
			throw IllegalStateException("Can't register $messageConsumer because some message was already published!")
		}

		val beanName = applicationContext.getBeanNamesForType(messageConsumer::class.java)[0]
		consumersByType.getOrPut(getPayloadType(messageConsumer)) { mutableSetOf() }.add(beanName)
	}

	private fun getPayloadType(consumer: MessagingConsumer<*>): Class<*> {
		return GenericTypeResolver.resolveTypeArgument(consumer::class.java, MessagingConsumer::class.java)!!
	}

	fun publish(message: Any) {
		publishedSomeMessage = true

		val messageType = message::class.java
		val payload = objectMapper.writeValueAsString(message)
		val consumers = consumersByType[messageType] ?: throw IllegalStateException("No Consumer for $messageType")
		consumers.forEach { saveMessage(it, payload) }

		afterCommitRunner.execute {
			messagingExecutor.fetchAndExecuteMessages()
		}
	}

	private fun saveMessage(consumer: String, payload: String) {
		LOGGER.info("Saved message for consumer $consumer: $payload")
		applicationMessageRepository.save(ApplicationMessage(consumer, payload))
	}

	companion object {
		private val LOGGER = LoggerFactory.getLogger(MessagingPublisher::class.java)
	}
}