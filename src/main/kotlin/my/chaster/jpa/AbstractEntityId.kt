package my.chaster.jpa

import com.github.f4b6a3.ulid.UlidCreator
import java.io.Serializable
import java.util.UUID
import javax.persistence.Column
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class AbstractEntityId(
	@Column(name = "id", unique = true, updatable = false, nullable = false)
	var id: UUID,
) : Serializable {

	override fun toString(): String {
		return "${this.javaClass.simpleName}($id)"
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as AbstractEntityId

		if (id != other.id) return false

		return true
	}

	override fun hashCode(): Int {
		return id.hashCode() ?: super.hashCode()
	}

	companion object {
		fun randomId(): UUID = UlidCreator.getMonotonicUlid().toUuid()
	}
}