package my.chaster.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import javax.persistence.EntityNotFoundException

interface AbstractEntityRepository<T : AbstractEntity<ID>, ID : AbstractEntityId> : JpaRepository<T, ID> {

	fun findOrThrow(id: ID): T {
		return findByIdOrNull(id) ?: throw EntityNotFoundException("No entity found for $id")
	}
}