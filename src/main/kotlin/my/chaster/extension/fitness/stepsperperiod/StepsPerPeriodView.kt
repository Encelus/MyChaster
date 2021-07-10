package my.chaster.extension.fitness.stepsperperiod

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.VaadinSession
import my.chaster.fitness.GoogleFitnessService
import my.chaster.views.MainLayout
import my.chaster.views.TemporalFormatter
import my.chaster.views.ensureZoneId
import my.chaster.views.getChasterUserId
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@Route(value = "fit", layout = MainLayout::class)
@PageTitle("Steps Per Period")
class StepsPerPeriodView(
	private val googleFitnessService: GoogleFitnessService,
	private val stepsPerPeriodHistoryRepository: StepsPerPeriodHistoryRepository,
) : VerticalLayout(), BeforeEnterObserver {

	private val grid: Grid<StepsPerPeriodHistory>
	private val period: Text
	private val requiredSteps: Text
	private val penalty: Text

	init {
		addClassName("steps-per-period-view")
		grid = Grid(StepsPerPeriodHistory::class.java, false)
		grid.addColumn(this::renderPeriod).setHeader("Period").setComparator(Comparator.comparing(StepsPerPeriodHistory::periodStart))
		grid.addColumn(StepsPerPeriodHistory::steps).setHeader("Steps")
		grid.addComponentColumn(this::renderAppliedPunishment).setHeader("Result")
		add(grid)

		period = Text("")
		requiredSteps = Text("")
		penalty = Text("")
		add(Div(period), Div(requiredSteps), Div(penalty))
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
			event.ui.ensureZoneId { initUi() }
		}
	}

	private fun renderPeriod(stepsPerPeriodHistory: StepsPerPeriodHistory): String {
		return "${TemporalFormatter.formatDateTime(stepsPerPeriodHistory.periodStart)} to ${TemporalFormatter.formatDateTime(stepsPerPeriodHistory.periodEnd)}"
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
			val description = Text("Added ${TemporalFormatter.formatDuration(stepsPerPeriodHistory.appliedPunishment!!)}")
			HorizontalLayout(check, description)
		}
	}

	private fun initUi() {
		val periodDuration = Duration.ofDays(1)
		val requiredStepsCount = 7500
		val penaltyDuration = Duration.ofHours(12)

		initGridData()
		period.text = "Period: ${TemporalFormatter.formatDuration(periodDuration)}"
		requiredSteps.text = "Required Steps: $requiredStepsCount"
		penalty.text = "Penalty: ${TemporalFormatter.formatDuration(penaltyDuration)}"
	}

	fun initGridData() {
		val chasterUserId = VaadinSession.getCurrent().getChasterUserId()
		val lockStart = Instant.now().minus(30, ChronoUnit.DAYS)
		grid.setItems(stepsPerPeriodHistoryRepository.findAllByChasterUserIdAndPeriodStartAfterOrderByPeriodStartDesc(chasterUserId, lockStart))
	}


}