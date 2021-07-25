package my.chaster.extension.fitness.stepstounlock

import com.vaadin.flow.component.Text
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.progressbar.ProgressBar
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import my.chaster.chaster.ChasterLockId
import my.chaster.extension.fitness.stepstounlock.workaround.config.StepsToUnlockConfigRepository
import my.chaster.fitness.GoogleFitnessService
import my.chaster.views.MainLayout
import my.chaster.views.getChasterLockId
import my.chaster.views.getChasterUserId
import kotlin.math.min

@Route(value = StepsToUnlockView.ROUTE, layout = MainLayout::class)
@PageTitle("Steps Per Period")
class StepsToUnlockView(
	private val googleFitnessService: GoogleFitnessService,
	private val stepsToUnlockConfigRepository: StepsToUnlockConfigRepository,
	private val stepsToUnlockService: StepsToUnlockService,
) : VerticalLayout(), BeforeEnterObserver {

	private val progressBar: ProgressBar
	private val stepsText: Text

	init {
		addClassName("steps-to-unlock-view")
		defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER

		add(H3("Steps To Unlock Extensions"))

		progressBar = ProgressBar()
		progressBar.isIndeterminate = true

		add(progressBar)

		stepsText = Text("- / -")
		add(Div(stepsText))
	}

	override fun beforeEnter(event: BeforeEnterEvent) {
		val chasterUserId = event.ui.session.getChasterUserId()
		if (event.location.queryParameters.parameters.containsKey("code")) {
			val googleCode = event.location.queryParameters.parameters["code"]!![0]
			googleFitnessService.storeAuthorization(ROUTE, googleCode, chasterUserId)
			event.ui.page.history.replaceState(null, event.location.path)
		} else if (!googleFitnessService.isAuthorized(chasterUserId)) {
			event.ui.session.setAttribute("chasterUserId", chasterUserId)
			event.ui.page.open(googleFitnessService.authorize(ROUTE), "_self")
			return
		}

		initProgressBar(event.ui.session.getChasterLockId())
	}

	private fun initProgressBar(chasterLockId: ChasterLockId) {
		val config = stepsToUnlockConfigRepository.findByChasterLockIdOrThrow(chasterLockId)
		progressBar.min = 0.0
		progressBar.max = config.requiredSteps.toDouble()

		val currentSteps = stepsToUnlockService.getCurrentSteps(chasterLockId)
		progressBar.value = min(currentSteps.toDouble(), progressBar.max)
		progressBar.isIndeterminate = false

		stepsText.text = "$currentSteps / ${config.requiredSteps}"
	}

	companion object {
		const val ROUTE = "steps-to-unlock"
	}
}