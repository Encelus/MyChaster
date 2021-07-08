package my.chaster.fitness

import my.chaster.chaster.ChasterUserId
import my.chaster.jpa.AbstractEntity
import my.chaster.jpa.AbstractEntityId
import java.time.Instant
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "google_fitness_token")
class GoogleFitnessToken(
	@Column(name = "chaster_user_id", nullable = false, updatable = false, unique = true)
	val chasterUserId: ChasterUserId,
	@Column(name = "token", nullable = false)
	var token: String,
	@Column(name = "valid_until", nullable = false)
	var validUntil: Instant,
	@Column(name = "refresh_token")
	var refreshToken: String?,
) : AbstractEntity<GoogleFitnessTokenId>(GoogleFitnessTokenId()) {


}

@Embeddable
class GoogleFitnessTokenId(id: UUID = randomId()) : AbstractEntityId(id)