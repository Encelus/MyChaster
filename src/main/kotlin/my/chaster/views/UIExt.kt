package my.chaster.views

import com.vaadin.flow.component.UI
import java.time.ZoneId

class UIExt

fun UI.ensureZoneId(asyncConsumer: () -> Unit) {
	if (!session.hasZoneId()) {
		UI.getCurrent().page.retrieveExtendedClientDetails { details ->
			val zoneId = details.timeZoneId
				?.let { ZoneId.of(it) }
				?: ZoneId.systemDefault()
			session.setZoneId(zoneId)
			asyncConsumer.invoke()
		}
	} else {
		asyncConsumer.invoke()
	}
}