package my.chaster.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import my.chaster.jpa.AfterCommitRunner
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.core.GenericTypeResolver
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class MessagingPublisher(
	private val applicationContext: ApplicationContext,
	private val applicationMessageRepository: ApplicationMessageRepository,
	private val messagingExecutor: MessagingExecutor,
	private val afterCommitRunner: AfterCommitRunner,
	private val objectMapper: ObjectMapper,
) {

	private lateinit var consumersByType: Map<Class<*>, Set<String>>

	@PostConstruct
	fun initConsumers() {
		val consumersByType = mutableMapOf<Class<*>, MutableSet<String>>()
		val consumerBeans = applicationContext.getBeansOfType(MessagingConsumer::class.java)
		consumerBeans.forEach { (name, bean) ->
			consumersByType.getOrPut(getPayloadType(bean)) { mutableSetOf() }.add(name)
		}

		this.consumersByType = consumersByType
	}

	private fun getPayloadType(consumer: MessagingConsumer<*>): Class<*> {
		return GenericTypeResolver.resolveTypeArgument(consumer::class.java, MessagingConsumer::class.java)!!
	}

	fun publish(message: Any) {
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