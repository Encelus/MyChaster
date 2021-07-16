package my.chaster.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Repository

@Repository
class ApplicationMessageTestData(
	private val applicationMessageRepository: ApplicationMessageRepository,
	private val objectMapper: ObjectMapper,
) {

	fun create(consumer: String, payload: Any, failure: String? = null): ApplicationMessage {
		val payloadStr = objectMapper.writeValueAsString(payload)

		val applicationMessage = applicationMessageRepository.save(
			ApplicationMessage(consumer, payloadStr),
		)
		applicationMessage.failure = failure
		return applicationMessage
	}
}