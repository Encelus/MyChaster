package my.chaster.chaster.workaround.lock

import my.chaster.chaster.WithChasterLockId
import my.chaster.chaster.WithChasterUserId
import my.chaster.jpa.AbstractEntity
import my.chaster.jpa.AbstractEntityId
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "lock")
class Lock(
	@Column(name = "chaster_lock_id", updatable = false, nullable = false, unique = true)
	override val chasterLockId: my.chaster.chaster.ChasterLockId,
	@Column(name = "chaster_user_id", updatable = false, nullable = false)
	override val chasterUserId: my.chaster.chaster.ChasterUserId,
) : AbstractEntity<LockId>(LockId()), WithChasterLockId, WithChasterUserId {

	@Column(name = "fake_api_key", updatable = false, nullable = false, unique = true)
	val fakeApiKey: UUID = UUID.randomUUID()
}

@Embeddable
class LockId(id: UUID = randomId()) : AbstractEntityId(id)