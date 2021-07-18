package my.chaster.extension.fitness.stepsperperiod.workaround.config

import my.chaster.chaster.ChasterLockId
import my.chaster.chaster.WithChasterLockId
import my.chaster.jpa.AbstractEntity
import my.chaster.jpa.AbstractEntityId
import java.time.Duration
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "steps_per_period_config")
class StepsPerPeriodConfig(
	@Column(name = "chaster_lock_id", nullable = false, updatable = false, unique = true)
	override val chasterLockId: ChasterLockId,
	@Column(name = "period", nullable = false, updatable = false)
	val period: Duration,
	@Column(name = "required_steps", nullable = false, updatable = false)
	val requiredSteps: Int,
	@Column(name = "penalty", nullable = false, updatable = false)
	val penalty: Duration,
) : AbstractEntity<StepsPerPeriodConfigId>(StepsPerPeriodConfigId()), WithChasterLockId

@Embeddable
class StepsPerPeriodConfigId(id: UUID = randomId()) : AbstractEntityId(id)