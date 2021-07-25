package my.chaster.extension.fitness.stepstounlock.workaround.config

import my.chaster.chaster.ChasterLockId
import my.chaster.chaster.WithChasterLockId
import my.chaster.jpa.AbstractEntity
import my.chaster.jpa.AbstractEntityId
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "steps_to_unlock_config")
class StepsToUnlockConfig(
	@Column(name = "chaster_lock_id", nullable = false, updatable = false, unique = true)
	override val chasterLockId: ChasterLockId,
	@Column(name = "required_steps", nullable = false, updatable = false)
	val requiredSteps: Int,
) : AbstractEntity<StepsToUnlockConfigId>(StepsToUnlockConfigId()), WithChasterLockId {

	@Column(name = "is_frozen", nullable = false)
	var isFrozen = true
}

@Embeddable
class StepsToUnlockConfigId(id: UUID = randomId()) : AbstractEntityId(id)