package my.chaster.messaging

import org.springframework.beans.factory.annotation.Autowired
import javax.annotation.PostConstruct

abstract class MessagingConsumer<T> {

	@Autowired
	private lateinit var messagingPublisher: MessagingPublisher


	abstract fun handle(message: T): Any?

	@PostConstruct
	fun register() {
		messagingPublisher.register(this)
	}
}