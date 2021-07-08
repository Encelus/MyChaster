package my.chaster.fitness

import com.vaadin.flow.component.ClickEvent
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import my.chaster.chaster.ChasterUserId
import my.chaster.gen.chaster.api.ProfileApi
import my.chaster.views.MainLayout

@Route(value = "fit", layout = MainLayout::class)
@PageTitle("Fit")
class GoogleFitnessView(
	private val googleFitnessService: GoogleFitnessService,
	private val profileApi: ProfileApi,
) : HorizontalLayout(), BeforeEnterObserver {

	private val name: TextField
	private val sayHello: Button

	init {
		addClassName("google-fit-view")
		name = TextField("Fit name")
		sayHello = Button("Say hello")
		add(name, sayHello)
		setVerticalComponentAlignment(FlexComponent.Alignment.END, name, sayHello)
		sayHello.addClickListener { e: ClickEvent<Button?>? -> Notification.show("Hello " + profileApi!!.authMeControllerMe().username) }
	}

	override fun beforeEnter(event: BeforeEnterEvent) {
		if (event.location.queryParameters.parameters.containsKey("chasterUserId")) {
			val chasterUserId = event.location.queryParameters.parameters["chasterUserId"]!![0].let { ChasterUserId(it) }
			if (!googleFitnessService.isAuthorized(chasterUserId)) {
				event.ui.page.open(googleFitnessService.authorize(), "_self")
			}
		} else if (event.location.queryParameters.parameters.containsKey("code")) {
			val chasterUserId = event.location.queryParameters.parameters["code"]
//			googleFitnessService.storeAuthorization(code)
		}
	}
}