package my.chaster.extension.fitness.stepsperperiod

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.VaadinSession
import my.chaster.fitness.GoogleFitnessService
import my.chaster.views.MainLayout
import my.chaster.views.ensureZoneId
import my.chaster.views.getChasterUserId
import my.chaster.views.getZoneId
import org.apache.commons.lang3.time.DurationFormatUtils
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

@Route(value = "fit", layout = MainLayout::class)
@PageTitle("Steps Per Period")
class StepsPerPeriodView(
	private val googleFitnessService: GoogleFitnessService,
	private val stepsPerPeriodHistoryRepository: StepsPerPeriodHistoryRepository,
) : HorizontalLayout(), BeforeEnterObserver {

	private val grid: Grid<StepsPerPeriodHistory>

	init {
		addClassName("steps-per-period-view")
		grid = Grid(StepsPerPeriodHistory::class.java, false)
		grid.addColumn(this::renderPeriod).setHeader("Period").setComparator(Comparator.comparing(StepsPerPeriodHistory::periodStart))
		grid.addColumn(StepsPerPeriodHistory::steps).setHeader("Steps")
		grid.addComponentColumn(this::renderAppliedPunishment).setHeader("Result")
		add(grid)
	}

	override fun beforeEnter(event: BeforeEnterEvent) {
		val chasterUserId = event.ui.session.getChasterUserId()
		if (event.location.queryParameters.parameters.containsKey("code")) {
			val googleCode = event.location.queryParameters.parameters["code"]!![0]
			googleFitnessService.storeAuthorization(googleCode, chasterUserId)
		} else if (!googleFitnessService.isAuthorized(chasterUserId)) {
			event.ui.session.setAttribute("chasterUserId", chasterUserId)
			event.ui.page.open(googleFitnessService.authorize(), "_self")
		} else {
			event.ui.ensureZoneId { initGridData() }
		}
	}

	private fun renderPeriod(stepsPerPeriodHistory: StepsPerPeriodHistory): String {
		val formatter = DateTimeFormatter
			.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
			.withLocale(VaadinSession.getCurrent().locale)
		val periodStart = stepsPerPeriodHistory.periodStart.atZone(VaadinSession.getCurrent().getZoneId())
		val periodEnd = stepsPerPeriodHistory.periodEnd.atZone(VaadinSession.getCurrent().getZoneId())
		return "${formatter.format(periodStart)} to ${formatter.format(periodEnd)}"
	}

	private fun renderAppliedPunishment(stepsPerPeriodHistory: StepsPerPeriodHistory): Component {
		return if (stepsPerPeriodHistory.isActive(Instant.now())) {
			Text("Ongoing")
		} else if (stepsPerPeriodHistory.appliedPunishment == null) {
			val check = Icon(VaadinIcon.CHECK_CIRCLE)
			check.color = "green"
			check
		} else {
			val check = Icon(VaadinIcon.CLOSE_CIRCLE)
			check.color = "red"
			val description = Text("Added ${DurationFormatUtils.formatDurationWords(stepsPerPeriodHistory.appliedPunishment!!.toMillis(), true, true)}")
			HorizontalLayout(check, description)
		}
	}

	fun initGridData() {
		val chasterUserId = VaadinSession.getCurrent().getChasterUserId()
		val lockStart = Instant.now().minus(30, ChronoUnit.DAYS)
		grid.setItems(stepsPerPeriodHistoryRepository.findAllByChasterUserIdAndPeriodStartAfterOrderByPeriodStartDesc(chasterUserId, lockStart))
	}

}