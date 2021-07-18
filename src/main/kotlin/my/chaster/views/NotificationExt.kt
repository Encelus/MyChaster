package my.chaster.views

import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import java.util.concurrent.TimeUnit

class NotificationExt {

	companion object {
		fun error(text: String): Notification {
			val notification = Notification()
			notification.setText(text)
			notification.duration = TimeUnit.SECONDS.toMillis(3).toInt()
			notification.position = Notification.Position.BOTTOM_CENTER
			notification.themeName = NotificationVariant.LUMO_ERROR.variantName
			notification.open()
			return notification
		}
	}
}
