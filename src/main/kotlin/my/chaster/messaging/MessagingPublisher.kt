package my.chaster.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import my.chaster.jpa.AfterCommitRunner
import org.springframework.context.ApplicationContext
import org.springframework.core.GenericTypeResolver
import org.springframework.stereotype.Component

@Component
class MessagingPublisher(
	applicationContext: ApplicationContext,
	private val applicationMessageRepository: ApplicationMessageRepository,
	private val messagingExecutor: MessagingExecutor,
	private val afterCommitRunner: AfterCommitRunner,
	private val objectMapper: ObjectMapper,
) {

	private val consumersByType: Map<Class<*>, Set<String>>

	init {
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
		applicationMessageRepository.save(ApplicationMessage(consumer, payload))
	}
}