package my.chaster.fitness

import com.google.api.client.auth.oauth2.TokenResponse
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.util.Utils
import com.google.api.services.fitness.Fitness
import com.google.api.services.fitness.FitnessScopes
import com.google.api.services.fitness.model.AggregateBucket
import com.google.api.services.fitness.model.AggregateBy
import com.google.api.services.fitness.model.AggregateRequest
import com.google.api.services.fitness.model.BucketByTime
import com.google.api.services.fitness.model.DataPoint
import com.google.api.services.fitness.model.Dataset
import my.chaster.chaster.ChasterUserId
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit
import javax.transaction.Transactional

@Service
@Transactional
class GoogleFitnessService(
	@Value("\${google.client-id}") clientId: String,
	@Value("\${google.client-secret}") clientSecret: String,
	private val googleFitnessTokenDataStore: GoogleFitnessTokenDataStore,
) {

	private val flow: GoogleAuthorizationCodeFlow

	init {
		val web = GoogleClientSecrets.Details()
		web.clientId = clientId
		web.clientSecret = clientSecret
		val clientSecrets = GoogleClientSecrets().setWeb(web)
		//			httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		flow = GoogleAuthorizationCodeFlow.Builder(
			Utils.getDefaultTransport(),
			Utils.getDefaultJsonFactory(),
			clientSecrets,
			setOf(FitnessScopes.FITNESS_ACTIVITY_READ, FitnessScopes.FITNESS_ACTIVITY_WRITE),
		)
			.setCredentialDataStore(googleFitnessTokenDataStore)
			.build()
	}

	fun isAuthorized(chasterUserId: ChasterUserId): Boolean {
		return googleFitnessTokenDataStore.get(chasterUserId.id) != null
	}

	fun authorize(): String {
		val authorizationUrl = flow.newAuthorizationUrl().setRedirectUri("http://localhost:8080/fit")
		return authorizationUrl.build()
	}

	fun storeAuthorization(code: String, chasterUserId: ChasterUserId) {
		val response: TokenResponse = flow.newTokenRequest(code).setRedirectUri("http://localhost:8080/fit").execute()
		flow.createAndStoreCredential(response, chasterUserId.id)
	}

	fun getStepsOfDay(chasterUserId: ChasterUserId, day: LocalDate): Int {
		val fitness = createClient(chasterUserId)
		val aggregateRequest = AggregateRequest()
			.setAggregateBy(
				listOf(
					AggregateBy()
						.setDataTypeName("com.google.step_count.delta")
						.setDataSourceId("derived:com.google.step_count.delta:com.google.android.gms:estimated_steps"),
				),
			)
			.setBucketByTime(BucketByTime().setDurationMillis(TimeUnit.DAYS.toMillis(1)))
			.setStartTimeMillis(day.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
			.setEndTimeMillis(day.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
		val datasetsOfDay = fitness.users().dataset().aggregate("me", aggregateRequest).execute()
		return datasetsOfDay.bucket.asSequence()
			.flatMap { bucket: AggregateBucket -> bucket.dataset }
			.flatMap { dataset: Dataset -> dataset.point }
			.flatMap { dataPoint: DataPoint -> dataPoint.value }
			.map { it.intVal }
			.sum()
	}

	private fun createClient(chasterUserId: ChasterUserId): Fitness {
		val credential = flow.loadCredential(chasterUserId.id)!!
		return Fitness.Builder(flow.transport, flow.jsonFactory, credential)
			.setApplicationName("MyChaster").build()
	}
}