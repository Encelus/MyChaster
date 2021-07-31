package my.chaster.fitness

import com.google.api.client.auth.oauth2.TokenResponse
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.googleapis.util.Utils
import com.google.api.services.fitness.Fitness
import com.google.api.services.fitness.FitnessScopes
import com.google.api.services.fitness.model.AggregateBucket
import com.google.api.services.fitness.model.AggregateBy
import com.google.api.services.fitness.model.AggregateRequest
import com.google.api.services.fitness.model.BucketByTime
import com.google.api.services.fitness.model.DataPoint
import com.google.api.services.fitness.model.Dataset
import my.chaster.chaster.ChasterLockId
import my.chaster.chaster.ChasterUserId
import my.chaster.chaster.workaround.lock.LockRepository
import my.chaster.views.UrlBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.transaction.Transactional

@Service
@Transactional
class GoogleFitnessService(
	@Value("\${google.client-id}") clientId: String,
	@Value("\${google.client-secret}") clientSecret: String,
	private val googleFitnessTokenDataStore: GoogleFitnessTokenDataStore,
	private val lockRepository: LockRepository,
	private val urlBuilder: UrlBuilder,
) {

	private val flow: GoogleAuthorizationCodeFlow

	init {
		val web = GoogleClientSecrets.Details()
		web.clientId = clientId
		web.clientSecret = clientSecret
		val clientSecrets = GoogleClientSecrets().setWeb(web)
		flow = GoogleAuthorizationCodeFlow.Builder(
			Utils.getDefaultTransport(),
			Utils.getDefaultJsonFactory(),
			clientSecrets,
			setOf(FitnessScopes.FITNESS_ACTIVITY_READ),
		)
			.setCredentialDataStore(googleFitnessTokenDataStore)
			.build()
	}

	fun isAuthorized(chasterLockId: ChasterLockId): Boolean {
		val lock = lockRepository.findByChasterLockId(chasterLockId) ?: return false
		return isAuthorized(lock.chasterUserId)
	}

	fun isAuthorized(chasterUserId: ChasterUserId): Boolean {
		val storedCredential = googleFitnessTokenDataStore.get(chasterUserId.id)
		if (storedCredential == null) {
			return false
		}

		if (storedCredential.refreshToken == null && storedCredential.expirationTimeMilliseconds <= 0) {
			return false
		}
		return true
	}

	fun authorize(redirectPage: String): String {
		val authorizationUrl = flow.newAuthorizationUrl()
			.setAccessType("offline")
			.setRedirectUri(urlBuilder.build(redirectPage).toString())
		return authorizationUrl.build()
	}

	fun storeAuthorization(page: String, code: String, chasterUserId: ChasterUserId) {
		val response: TokenResponse = flow.newTokenRequest(code).setRedirectUri(urlBuilder.build(page).toString()).execute()
		flow.createAndStoreCredential(response, chasterUserId.id)
	}

	fun getSteps(chasterUserId: ChasterUserId, start: Instant, end: Instant): Int {
		val fitness = createClient(chasterUserId)
		val steps = getAggregatedStepsForDataSource(chasterUserId, fitness, start, end, "derived:com.google.step_count.delta:com.google.android.gms:estimated_steps")
		val manualSteps = getAggregatedStepsForDataSource(chasterUserId, fitness, start, end, "raw:com.google.step_count.delta:com.google.android.apps.fitness:user_input")
		return steps - manualSteps
	}

	private fun getAggregatedStepsForDataSource(chasterUserId: ChasterUserId, fitness: Fitness, start: Instant, end: Instant, dataSourceId: String): Int {
		try {
			val aggregateRequest = AggregateRequest()
				.setAggregateBy(
					listOf(
						AggregateBy()
							.setDataSourceId(dataSourceId),
					),
				)
				.setBucketByTime(BucketByTime().setDurationMillis(TimeUnit.DAYS.toMillis(1)))
				.setStartTimeMillis(start.toEpochMilli())
				.setEndTimeMillis(end.toEpochMilli())
			val datasets = fitness.users().dataset().aggregate("me", aggregateRequest).execute()
			return datasets.bucket.asSequence()
				.flatMap { bucket: AggregateBucket -> bucket.dataset }
				.flatMap { dataset: Dataset -> dataset.point }
				.flatMap { dataPoint: DataPoint -> dataPoint.value }
				.map { it.intVal }
				.sum()
		} catch (e: GoogleJsonResponseException) {
			LOGGER.warn("Failed to load steps for $chasterUserId and $dataSourceId. ${e.message}")
			return 0
		}
	}

	private fun createClient(chasterUserId: ChasterUserId): Fitness {
		val credential = flow.loadCredential(chasterUserId.id)!!
		return Fitness.Builder(flow.transport, flow.jsonFactory, credential)
			.setApplicationName("MyChaster").build()
	}

	companion object {
		private val LOGGER = LoggerFactory.getLogger(GoogleFitnessService::class.java)
	}
}