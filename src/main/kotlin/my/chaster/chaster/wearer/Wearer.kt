package my.chaster.chaster.wearer

import my.chaster.chaster.ChasterUserId
import my.chaster.jpa.AbstractEntity
import my.chaster.jpa.AbstractEntityId
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Embeddable

//@Entity
//@Table(name = "wearer")
class Wearer(
	@Column(name = "chaster_user_id", nullable = false, updatable = false, unique = true)
	val chasterUserId: ChasterUserId,
) : AbstractEntity<WearerId>(WearerId()) {

}

@Embeddable
class WearerId(id: UUID = randomId()) : AbstractEntityId(id)