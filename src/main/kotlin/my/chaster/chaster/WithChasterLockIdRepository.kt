package my.chaster.chaster

import my.chaster.jpa.AbstractEntity
import my.chaster.jpa.AbstractEntityId
import javax.persistence.EntityNotFoundException


interface WithChasterLockIdRepository<T> where T : WithChasterLockId, T : AbstractEntity<out AbstractEntityId> {

	fun findByChasterLockId(chasterLockId: ChasterLockId): T?

	fun findByChasterLockIdOrThrow(chasterLockId: ChasterLockId): T {
		return findByChasterLockId(chasterLockId) ?: throw EntityNotFoundException("No entity found for $chasterLockId")
	}
}