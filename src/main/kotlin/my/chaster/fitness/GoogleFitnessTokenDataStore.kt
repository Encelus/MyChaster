package my.chaster.fitness

import com.google.api.client.auth.oauth2.StoredCredential
import com.google.api.client.util.store.DataStore
import com.google.api.client.util.store.DataStoreFactory
import my.chaster.chaster.ChasterUserId
import org.springframework.stereotype.Component
import java.time.Instant
import javax.transaction.Transactional

@Component
@Transactional
class GoogleFitnessTokenDataStore(
	private val googleFitnessTokenRepository: GoogleFitnessTokenRepository,
) : DataStore<StoredCredential> {

	override fun getDataStoreFactory(): DataStoreFactory {
		TODO("Not yet implemented")
	}

	override fun getId(): String {
		TODO("Not yet implemented")
	}

	override fun size(): Int {
		TODO("Not yet implemented")
	}

	override fun isEmpty(): Boolean {
		TODO("Not yet implemented")
	}

	override fun containsKey(key: String?): Boolean {
		TODO("Not yet implemented")
	}

	override fun containsValue(value: StoredCredential?): Boolean {
		TODO("Not yet implemented")
	}

	override fun keySet(): MutableSet<String> {
		TODO("Not yet implemented")
	}

	override fun values(): MutableCollection<StoredCredential> {
		TODO("Not yet implemented")
	}

	override fun get(key: String): StoredCredential? {
		val maybeExistingToken = googleFitnessTokenRepository.findByChasterUserId(ChasterUserId(key))
		if (maybeExistingToken == null) {
			return null
		}
		return StoredCredential()
			.setAccessToken(maybeExistingToken.token)
			.setExpirationTimeMilliseconds(maybeExistingToken.validUntil.toEpochMilli() - Instant.now().toEpochMilli())
			.setRefreshToken(maybeExistingToken.refreshToken)
	}

	override fun set(key: String, value: StoredCredential): DataStore<StoredCredential> {
		val chasterUserId = ChasterUserId(key)
		val maybeExistingToken = googleFitnessTokenRepository.findByChasterUserId(chasterUserId)
		if (maybeExistingToken != null) {
			maybeExistingToken.token = value.accessToken
			maybeExistingToken.validUntil = Instant.now().plusMillis(value.expirationTimeMilliseconds)
			maybeExistingToken.refreshToken = value.refreshToken
		} else {
			googleFitnessTokenRepository.save(
				GoogleFitnessToken(
					chasterUserId,
					value.accessToken,
					Instant.now().plusMillis(value.expirationTimeMilliseconds),
					value.refreshToken,
				),
			)
		}
		
		return this
	}

	override fun clear(): DataStore<StoredCredential> {
		TODO("Not yet implemented")
	}

	override fun delete(key: String?): DataStore<StoredCredential> {
		TODO("Not yet implemented")
	}
}