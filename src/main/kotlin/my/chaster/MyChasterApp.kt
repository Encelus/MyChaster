package my.chaster

import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.server.PWA
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.Lumo
import my.chaster.gen.chaster.ApiClient
import my.chaster.gen.chaster.auth.OAuth
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.vaadin.artur.helpers.LaunchUtil
import java.time.Duration

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
@EnableJpaRepositories
@ComponentScan(excludeFilters = [ComponentScan.Filter(type = FilterType.ASPECTJ, pattern = ["my.chaster.gen.chaster.ApiClient"])])
@Theme(value = "mychaster", variant = Lumo.DARK)
@PWA(name = "MyChaster", shortName = "MyChaster", offlineResources = ["images/logo.png"])
class MyChasterApp : SpringBootServletInitializer(), AppShellConfigurator {

	@Bean
	fun apiClient(restTemplateBuilder: RestTemplateBuilder, @Value("\${chaster.developer-token}") chasterDeveloperToken: String): ApiClient {
		val restTemplate = restTemplateBuilder
			.setConnectTimeout(Duration.ofSeconds(3))
			.setReadTimeout(Duration.ofSeconds(3))
			.build()

		val apiClient = ApiClient(restTemplate)
		(apiClient.getAuthentication("bearer") as OAuth).accessToken = chasterDeveloperToken
		return apiClient
	}
}

fun main(args: Array<String>) {
	LaunchUtil.launchBrowserInDevelopmentMode(SpringApplication.run(MyChasterApp::class.java, *args))
}