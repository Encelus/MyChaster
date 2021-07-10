package my.chaster.extension.fitness.stepsperperiod

import my.chaster.chaster.ChasterUserId
import my.chaster.jpa.AbstractEntity
import my.chaster.jpa.AbstractEntityId
import my.chaster.util.isAfterOrEqual
import my.chaster.util.isBeforeOrEqual
import java.time.Duration
import java.time.Instant
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "steps_per_period_history")
class StepsPerPeriodHistory(
	@Column(name = "chaster_user_id", nullable = false, updatable = false)
	val chasterUserId: ChasterUserId,
	@Column(name = "period_start", nullable = false, updatable = false)
	val periodStart: Instant,
	@Column(name = "period_end", nullable = false, updatable = false)
	val periodEnd: Instant,
	@Column(name = "steps", nullable = false, updatable = false)
	val steps: Int,
	@Column(name = "applied_punishment", updatable = false)
	val appliedPunishment: Duration?,
) : AbstractEntity<StepsPerPeriodHistoryId>(StepsPerPeriodHistoryId()) {

	fun isActive(now: Instant): Boolean {
		return periodStart.isBeforeOrEqual(now)
				&& periodEnd.isAfterOrEqual(now)
	}
}

@Embeddable
class StepsPerPeriodHistoryId(id: UUID = randomId()) : AbstractEntityId(id)