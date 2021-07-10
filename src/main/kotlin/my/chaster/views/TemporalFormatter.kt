package my.chaster.views

import com.vaadin.flow.server.VaadinSession
import org.apache.commons.lang3.time.DurationFormatUtils
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class TemporalFormatter {

	companion object {
		fun formatDateTime(instant: Instant): String {
			val formatter = DateTimeFormatter
				.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
				.withLocale(VaadinSession.getCurrent().locale)

			return formatter.format(instant.atZone(VaadinSession.getCurrent().getZoneId()))
		}

		fun formatDuration(duration: Duration): String {
			return DurationFormatUtils.formatDurationWords(duration.toMillis(), true, true)
		}
	}
}