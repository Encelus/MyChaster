package my.chaster.messaging

import my.chaster.jpa.AbstractEntity
import my.chaster.jpa.AbstractEntityId
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "application_message")
class ApplicationMessage(
	@Column(name = "consumer", updatable = false, nullable = false)
	val consumer: String,
	@Column(name = "payload", updatable = false, nullable = false)
	val payload: String,
) : AbstractEntity<ApplicationMessageId>(ApplicationMessageId()) {

	@Column(name = "failure")
	var failure: String? = null
}

@Embeddable
class ApplicationMessageId(id: UUID = randomId()) : AbstractEntityId(id)