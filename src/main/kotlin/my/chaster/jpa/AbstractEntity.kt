package my.chaster.jpa

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.EntityListeners
import javax.persistence.MappedSuperclass

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class AbstractEntity<ID : AbstractEntityId>(
	@EmbeddedId
	var id: ID,
) {

	@CreatedDate
	@Column(name = "created_on", nullable = false, updatable = false)
	var createdOn: Instant? = null

	@LastModifiedDate
	@Column(name = "modified_on", nullable = false)
	var modifiedOn: Instant? = null

	@Version
	@Column(name = "optlock", nullable = false)
	private val optlock = 0L


	override fun hashCode(): Int {
		return if (id != null) {
			id.hashCode()
		} else {
			super.hashCode()
		}
	}

	override fun equals(other: Any?): Boolean {
		if (other !is AbstractEntity<*>) {
			return false // null or other class
		}
		return if (id != null) {
			id == other.id
		} else {
			super.equals(other)
		}
	}
}