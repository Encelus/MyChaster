package my.chaster.fitness

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.auth.oauth2.TokenResponse
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.util.Utils
import com.google.api.services.fitness.Fitness
import com.google.api.services.fitness.FitnessScopes
import com.google.api.services.fitness.model.AggregateBucket
import com.google.api.services.fitness.model.AggregateBy
import com.google.api.services.fitness.model.AggregateRequest
import com.google.api.services.fitness.model.Application
import com.google.api.services.fitness.model.BucketByTime
import com.google.api.services.fitness.model.DataPoint
import com.google.api.services.fitness.model.DataSource
import com.google.api.services.fitness.model.DataType
import com.google.api.services.fitness.model.DataTypeField
import com.google.api.services.fitness.model.Dataset
import com.google.api.services.fitness.model.Device
import com.google.api.services.fitness.model.Value
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.view.RedirectView
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.List
import java.util.Set
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import javax.servlet.http.HttpServletRequest

@Controller
class GoogleController {
	var clientSecrets: GoogleClientSecrets? = null
	var flow: GoogleAuthorizationCodeFlow? = null
	var credential: Credential? = null
	private val clientId = ""
	private val clientSecret = ""
	private val redirectURI = "http://localhost:8080/login/google"

	//	private Set<Event> events = new HashSet<>();
	//
	//	final DateTime date1 = new DateTime("2017-05-05T16:30:00.000+05:30");
	//	final DateTime date2 = new DateTime(new Date());
	//
	//	public void setEvents(Set<Event> events) {
	//		this.events = events;
	//	}
	@RequestMapping(value = ["/login/google"], method = [RequestMethod.GET])
	@Throws(Exception::class)
	fun googleConnectionStatus(request: HttpServletRequest?): Any {
		return if (client == null) {
			RedirectView(authorize())
		} else {
			oauth2Callback("")
		}
	}

	@RequestMapping(value = ["/login/google"], method = [RequestMethod.GET], params = ["code"])
	fun oauth2Callback(@RequestParam(value = "code") code: String?): ResponseEntity<String> {
		var message: String
		try {
			if (client == null) {
				val response: TokenResponse = flow!!.newTokenRequest(code).setRedirectUri(redirectURI).execute()
				credential = flow!!.createAndStoreCredential(response, "userID")
				client = Fitness.Builder(Utils.getDefaultTransport(), Utils.getDefaultJsonFactory(), credential)
					.setApplicationName("MyChaster").build()
			}
			val aggregateRequest = AggregateRequest()
				.setAggregateBy(
					List.of(
						AggregateBy()
							.setDataTypeName("com.google.step_count.delta")
							.setDataSourceId("derived:com.google.step_count.delta:com.google.android.gms:estimated_steps"),
					),
				)
				.setBucketByTime(BucketByTime().setDurationMillis(TimeUnit.DAYS.toMillis(1)))
				.setStartTimeMillis(LocalDate.now().minusDays(0).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
				.setEndTimeMillis(LocalDate.now().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
			val result = client!!.users().dataset().aggregate("me", aggregateRequest).execute()
			val totalSteps = result.bucket.stream()
				.flatMap { bucket: AggregateBucket -> bucket.dataset.stream() }
				.flatMap { dataset: Dataset -> dataset.point.stream() }
				.flatMap { dataPoint: DataPoint -> dataPoint.value.stream() }
				.mapToInt { obj: Value -> obj.intVal }
				.sum()
			message = "Total Steps = $totalSteps $result"
			val dataSource = DataSource()
				.setDataStreamName("MyDatSource")
				.setType("derived")
				.setApplication(
					Application()
						.setDetailsUrl("http://example.com")
						.setName("Foo Example App")
						.setVersion("1"),
				)
				.setDataType(
					DataType()
						.setField(
							List.of(
								DataTypeField()
									.setName("steps")
									.setFormat("integer"),
							),
						)
						.setName("com.google.step_count.delta"),
				)
				.setDevice(
					Device()
						.setManufacturer("Example Manufacturer")
						.setModel("ExampleTablet")
						.setType("tablet")
						.setUid("1000001")
						.setVersion("1.0"),
				)
			//			var r1 = client.users().dataSources().create("me", dataSource).execute();
			val dataSources = client!!.users().dataSources().list("me").execute().dataSource.stream()
				.filter { ds: DataSource -> ds.dataStreamId.contains("derived:com.google.step_count.delta:854559895614:Example Manufacturer:ExampleTablet:1000001:MyDatSource") }
				.collect(Collectors.toUnmodifiableList())
			//			derived:com.google.step_count.delta:com.google.android.gms:samsung:SM-G950F:e98cd146:derive_step_deltas<-raw:com.google.step_count.cumulative:samsung:SM-G950F:e98cd146:SAMSUNG Step Counter Sensor
//			var r2 = client.users().dataSources().get("me", dataSources.get(0).getDataStreamId()).execute();
			val dataset = Dataset()
				.setDataSourceId(dataSources[0].dataStreamId)
				.setPoint(
					List.of(
						DataPoint()
							.setDataTypeName("com.google.step_count.delta")
							.setStartTimeNanos(LocalDate.now().atStartOfDay(ZoneOffset.UTC).plusSeconds(1).toInstant().toEpochMilli() * 1000000L)
							.setEndTimeNanos(LocalDate.now().atStartOfDay(ZoneOffset.UTC).plusHours(1).toInstant().toEpochMilli() * 1000000L)
							.setOriginDataSourceId("")
							.setValue(List.of(Value().setIntVal(1357))),
					),
				)
			dataset.minStartTimeNs = dataset.point[0].startTimeNanos
			dataset.maxEndTimeNs = dataset.point[0].endTimeNanos
			val datasetId = dataset.minStartTimeNs.toString() + "-" + dataset.maxEndTimeNs
			//			var r3 = client.users().dataSources().datasets().patch("me", dataSources.get(0).getDataStreamId(), datasetId, dataset).execute();
			LOGGER.info("My:" + client!!.users())
		} catch (e: Exception) {
			LOGGER.warn(
				"Exception while handling OAuth2 callback (" + e.message + ")."
						+ " Redirecting to google connection status page.",
			)
			message = ("Exception while handling OAuth2 callback (" + e.message + ")."
					+ " Redirecting to google connection status page.")
		}
		LOGGER.info("cal message:$message")
		return ResponseEntity(message, HttpStatus.OK)
	}

	private fun authorize(): String {
		val authorizationUrl: AuthorizationCodeRequestUrl
		if (flow == null) {
			val web = GoogleClientSecrets.Details()
			web.clientId = clientId
			web.clientSecret = clientSecret
			clientSecrets = GoogleClientSecrets().setWeb(web)
			//			httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			flow = GoogleAuthorizationCodeFlow.Builder(
				Utils.getDefaultTransport(), Utils.getDefaultJsonFactory(), clientSecrets,
				Set.of(FitnessScopes.FITNESS_ACTIVITY_READ, FitnessScopes.FITNESS_ACTIVITY_WRITE),
			).build()
		}
		authorizationUrl = flow!!.newAuthorizationUrl().setRedirectUri(redirectURI)
		LOGGER.info("cal authorizationUrl->$authorizationUrl")
		return authorizationUrl.build()
	}

	companion object {
		private val LOGGER = LoggerFactory.getLogger(GoogleController::class.java)

		//	private static final String APPLICATION_NAME = "";
		//	private static HttpTransport httpTransport;
		//	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
		private var client: Fitness? = null
	}
}