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
	val id: ID,
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
		return id.hashCode()
	}

	override fun equals(other: Any?): Boolean {
		if (other !is AbstractEntity<*>) {
			return false // null or other class
		}
		return id == other.id
	}

	override fun toString(): String {
		val result = StringBuilder("${this::class.simpleName}(id=$id")
		toStringInfo().forEach {
			result.append(",${it.first}=${it.second}")
		}
		return "$result)"
	}

	fun toStringInfo(): List<Pair<String, String>> {
		return mutableListOf()
	}
}