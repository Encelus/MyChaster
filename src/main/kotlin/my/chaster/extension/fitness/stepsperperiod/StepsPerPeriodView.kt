package my.chaster.extension.fitness.stepsperperiod

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridSortOrderBuilder
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
import my.chaster.extension.fitness.stepsperperiod.workaround.config.StepsPerPeriodConfigRepository
import my.chaster.fitness.GoogleFitnessService
import my.chaster.views.MainLayout
import my.chaster.views.TemporalFormatter
import my.chaster.views.ensureZoneId
import my.chaster.views.getChasterLockId
import my.chaster.views.getChasterUserId
import java.time.Instant

@Route(value = StepsPerPeriodView.ROUTE, layout = MainLayout::class)
@PageTitle("Steps Per Period")
class StepsPerPeriodView(
	private val googleFitnessService: GoogleFitnessService,
	private val stepsPerPeriodService: StepsPerPeriodService,
	private val stepsPerPeriodConfigRepository: StepsPerPeriodConfigRepository,
) : VerticalLayout(), BeforeEnterObserver {

	private val grid: Grid<StepsPerPeriodHistory>
	private val period: Text
	private val requiredSteps: Text
	private val penalty: Text

	init {
		addClassName("steps-per-period-view")
		grid = Grid(StepsPerPeriodHistory::class.java, false)
		val periodColumn = grid.addColumn(this::renderPeriod).setHeader("Period").setComparator(Comparator.comparing(StepsPerPeriodHistory::periodStart))
		grid.addColumn(StepsPerPeriodHistory::steps).setHeader("Steps")
		grid.addComponentColumn(this::renderAppliedPunishment).setHeader("Result")
		grid.sort(GridSortOrderBuilder<StepsPerPeriodHistory>().thenDesc(periodColumn).build())
		add(grid)

		period = Text("")
		requiredSteps = Text("")
		penalty = Text("")
		add(Div(period), Div(requiredSteps), Div(penalty))
	}

	override fun beforeEnter(event: BeforeEnterEvent) {
		val chasterUserId = event.ui.session.getChasterUserId()
		val existingConfig = stepsPerPeriodConfigRepository.findByChasterLockId(VaadinSession.getCurrent().getChasterLockId())
		if (existingConfig == null) {
			event.forwardTo(StepsPerPeriodConfigView::class.java)
			return
		} else if (event.location.queryParameters.parameters.containsKey("code")) {
			val googleCode = event.location.queryParameters.parameters["code"]!![0]
			googleFitnessService.storeAuthorization(ROUTE, googleCode, chasterUserId)
			event.ui.page.history.replaceState(null, event.location.path)
		} else if (!googleFitnessService.isAuthorized(chasterUserId)) {
			event.ui.session.setAttribute("chasterUserId", chasterUserId)
			event.ui.page.open(googleFitnessService.authorize(ROUTE), "_self")
			return
		}

		event.ui.ensureZoneId { initUi() }
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
		initGridData()

		val config = stepsPerPeriodConfigRepository.findByChasterLockIdOrThrow(VaadinSession.getCurrent().getChasterLockId())
		period.text = "Period: ${TemporalFormatter.formatDuration(config.period)}"
		requiredSteps.text = "Required Steps: ${config.requiredSteps}"
		penalty.text = "Penalty: ${TemporalFormatter.formatDuration(config.penalty)}"
	}

	private fun initGridData() {
		val chasterLockId = VaadinSession.getCurrent().getChasterLockId()
		grid.setItems(stepsPerPeriodService.loadHistory(chasterLockId))
	}

	companion object {
		const val ROUTE = "steps-per-period"
	}
}