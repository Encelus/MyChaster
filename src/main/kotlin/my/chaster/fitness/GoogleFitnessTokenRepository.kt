package my.chaster.fitness

import my.chaster.chaster.ChasterUserId
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.EntityNotFoundException


interface GoogleFitnessTokenRepository : JpaRepository<GoogleFitnessToken, GoogleFitnessTokenId> {

	fun findByChasterUserId(chasterUserId: ChasterUserId): GoogleFitnessToken?

	fun findByChasterUserIdOrThrow(chasterUserId: ChasterUserId): GoogleFitnessToken {
		return findByChasterUserId(chasterUserId) ?: throw EntityNotFoundException("No entity found for $chasterUserId")
	}
}