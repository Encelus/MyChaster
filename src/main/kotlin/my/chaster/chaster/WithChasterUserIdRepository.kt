package my.chaster.chaster

import my.chaster.jpa.AbstractEntity
import my.chaster.jpa.AbstractEntityId
import javax.persistence.EntityNotFoundException


interface WithChasterUserIdRepository<T> where T : WithChasterUserId, T : AbstractEntity<out AbstractEntityId> {

	fun findByChasterUserId(chasterUserId: ChasterUserId): T?
	
	fun findByChasterUserIdOrThrow(chasterUserId: ChasterUserId): T {
		return findByChasterUserId(chasterUserId) ?: throw EntityNotFoundException("No entity found for $chasterUserId")
	}
}