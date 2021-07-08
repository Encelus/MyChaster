package my.chaster.jpa

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import java.time.Instant
import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class AbstractEntity<ID : AbstractEntityId> {
	@EmbeddedId
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	var id: ID? = null

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