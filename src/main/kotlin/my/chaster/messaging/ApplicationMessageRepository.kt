package my.chaster.messaging

import my.chaster.jpa.AbstractEntityRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ApplicationMessageRepository : AbstractEntityRepository<ApplicationMessage, ApplicationMessageId> {

	fun findAllByFailureIsNull(pageable: Pageable): Page<ApplicationMessage>

}