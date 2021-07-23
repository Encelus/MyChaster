package my.chaster.views

import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import my.chaster.chaster.workaround.lock.LockRepository
import my.chaster.views.error.nouserspecified.NoUserSpecified
import java.util.UUID

class MainLayout(
	private val lockRepository: LockRepository,
) : AppLayout(), BeforeEnterObserver {

	override fun beforeEnter(event: BeforeEnterEvent) {
		verifyChasterUserId(event)
	}

	private fun verifyChasterUserId(event: BeforeEnterEvent) {
		if (event.location.queryParameters.parameters.containsKey(API_KEY_NAME)) {
			val apiKey = event.location.queryParameters.parameters[API_KEY_NAME]!![0]
			val lock = lockRepository.findByFakeApiKey(UUID.fromString(apiKey))
			if (lock == null) {
				event.rerouteToError(NoUserSpecified(), "")
			} else {
				event.ui.session.setChasterUserId(lock.chasterUserId)
				event.ui.session.setChasterLockId(lock.chasterLockId)
			}
		} else if (!event.ui.session.hasChasterUserId()) {
			event.rerouteToError(NoUserSpecified(), "")
		}
	}

	companion object {
		const val API_KEY_NAME = "api-key"
	}
}