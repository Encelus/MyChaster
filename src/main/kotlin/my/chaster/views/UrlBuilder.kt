package my.chaster.views

import my.chaster.AppProperties
import org.springframework.stereotype.Component
import java.net.URL
import java.util.UUID

@Component
class UrlBuilder(
	private val appProperties: AppProperties,
) {

	fun build(page: String, fakeApiKey: UUID? = null): URL {
		return if (fakeApiKey != null) {
			URL("${appProperties.baseUrl}/$page?api-key=${fakeApiKey}")
		} else {
			URL("${appProperties.baseUrl}/$page")
		}
	}
}