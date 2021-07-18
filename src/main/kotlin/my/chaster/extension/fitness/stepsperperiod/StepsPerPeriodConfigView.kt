package my.chaster.extension.fitness.stepsperperiod

import com.vaadin.flow.component.Text
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.VaadinSession
import my.chaster.extension.fitness.stepsperperiod.workaround.config.StepsPerPeriodConfig
import my.chaster.extension.fitness.stepsperperiod.workaround.config.StepsPerPeriodConfigRepository
import my.chaster.fitness.GoogleFitnessService
import my.chaster.views.MainLayout
import my.chaster.views.NotificationExt
import my.chaster.views.getChasterLockId
import java.time.Duration

@Route(value = StepsPerPeriodConfigView.ROUTE, layout = MainLayout::class)
@PageTitle("Steps Per Period Config")
class StepsPerPeriodConfigView(
	private val googleFitnessService: GoogleFitnessService,
	private val stepsPerPeriodService: StepsPerPeriodService,
	private val stepsPerPeriodConfigRepository: StepsPerPeriodConfigRepository,
) : VerticalLayout(), BeforeEnterObserver {

	private val periodField: IntegerField
	private val requiredStepsField: IntegerField
	private val penaltyField: IntegerField

	init {
		addClassName("steps-per-period-config-view")
		defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER

		periodField = createIntegerField(
			"Period in hours",
			min = 1,
			max = 999,
			default = 24,
		)
		requiredStepsField = createIntegerField(
			"Required Steps per Period",
			min = 100,
			max = 10_000_000,
			step = 100,
			default = 5000,
		)
		penaltyField = createIntegerField(
			"Penalty in hours",
			min = 1,
			max = 999,
			default = 6,
		)
		add(HorizontalLayout(periodField, requiredStepsField, penaltyField))

		val saveButton = Button("Save")
		saveButton.addClickListener { saveConfig() }
		add(saveButton)

		add(Text("You will not be able to change the configuration after you saved it. Saving the configuration will forward you to the overview of your current lock. It may require a google authentication first so that your steps can get tracked."))
	}

	override fun beforeEnter(event: BeforeEnterEvent) {
		val existingConfig = stepsPerPeriodConfigRepository.findByChasterLockId(VaadinSession.getCurrent().getChasterLockId())
		if (existingConfig != null) {
			event.forwardTo(StepsPerPeriodView::class.java)
		}
	}

	private fun createIntegerField(label: String, min: Int, max: Int, default: Int, step: Int = 1): IntegerField {
		val integerField = IntegerField(label)
		integerField.setHasControls(true)
		integerField.min = min
		integerField.max = max
		integerField.step = step
		integerField.value = default
		integerField.width = "10em"
		integerField.addValueChangeListener { if (it.hasValue.isEmpty) it.source.isInvalid = true }

		return integerField
	}

	private fun saveConfig() {
		if (periodField.isInvalid || requiredStepsField.isInvalid || penaltyField.isInvalid) {
			NotificationExt.error("Input is invalid")
			return
		}

		stepsPerPeriodConfigRepository.save(
			StepsPerPeriodConfig(
				VaadinSession.getCurrent().getChasterLockId(),
				Duration.ofHours(periodField.value.toLong()),
				requiredStepsField.value,
				Duration.ofHours(penaltyField.value.toLong()),
			),
		)

		UI.getCurrent().navigate(StepsPerPeriodView::class.java)
	}

	companion object {
		const val ROUTE = "steps-per-period-config"
	}
}