package my.chaster.jpa

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class AbstractEntityId(
	@Column(name = "id", unique = true, updatable = false, nullable = false)
	var id: Long? = null,
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
		return id?.hashCode() ?: super.hashCode()
	}
}