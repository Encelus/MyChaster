package my.chaster.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.annotations.VisibleForTesting
import my.chaster.jpa.TransactionalRunner
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.core.GenericTypeResolver
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
class MessagingExecutor(
	private val applicationContext: ApplicationContext,
	private val applicationMessageRepository: ApplicationMessageRepository,
	private val transactionalRunner: TransactionalRunner,
	private val objectMapper: ObjectMapper,
) {

	private val cachedMessagingConsumers: MutableMap<String, CachedMessagingConsumer> = mutableMapOf()
	private val executor = Executors.newSingleThreadExecutor()


	@PostConstruct
	fun fetchAndExecuteMessages() {
		executor.submit {
			do {
				val page = applicationMessageRepository.findAllByFailureIsNull(PageRequest.of(0, PAGE_SIZE))
				page.forEach { processInNewTransaction(it.id) }
			} while (page.hasNext())
		}
	}

	private fun processInNewTransaction(messageId: ApplicationMessageId) {
		try {
			transactionalRunner.runInNewTransaction {
				val message = applicationMessageRepository.findOrThrow(messageId)
				val cachedMessagingConsumer = getOrCreateCachedConsumer(message)
				LOGGER.info("Handling message for consumer ${message.consumer}: ${message.payload}")
				cachedMessagingConsumer.handle(message)

				applicationMessageRepository.delete(message)
			}
		} catch (e: Exception) {
			LOGGER.error("Failed to handle $messageId", e)
			transactionalRunner.runInNewTransaction {
				val message = applicationMessageRepository.findOrThrow(messageId)
				message.failure = "[${e::class.qualifiedName}] ${e.message}"
			}
		}
	}

	private fun getOrCreateCachedConsumer(message: ApplicationMessage): CachedMessagingConsumer {
		return cachedMessagingConsumers.getOrPut(message.consumer) {
			val consumer = getMessagingConsumer(message)
			val payloadType = getPayloadType(consumer)
			CachedMessagingConsumer(consumer, payloadType, objectMapper)
		}
	}

	private fun getMessagingConsumer(message: ApplicationMessage): MessagingConsumer<Any> {
		return applicationContext.getBean(message.consumer, MessagingConsumer::class.java) as MessagingConsumer<Any>
	}

	private fun getPayloadType(consumer: MessagingConsumer<*>): Class<*> {
		return GenericTypeResolver.resolveTypeArgument(consumer::class.java, MessagingConsumer::class.java)!!
	}

	@PreDestroy
	fun shutdown() {
		executor.shutdown()
		executor.awaitTermination(5, TimeUnit.SECONDS)
	}

	companion object {
		private val LOGGER = LoggerFactory.getLogger(MessagingExecutor::class.java)

		@VisibleForTesting
		val PAGE_SIZE = 5
	}

	private class CachedMessagingConsumer(
		private val consumer: MessagingConsumer<Any>,
		private val payloadType: Class<*>,
		private val objectMapper: ObjectMapper,
	) {

		fun handle(message: ApplicationMessage) {
			val payload = objectMapper.readValue(message.payload, payloadType)
			consumer.handle(payload)
		}
	}
}