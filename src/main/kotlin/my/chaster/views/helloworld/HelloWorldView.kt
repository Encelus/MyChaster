package my.chaster.views.helloworld

import com.vaadin.flow.component.ClickEvent
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouteAlias
import my.chaster.gen.chaster.api.ProfileApi
import my.chaster.views.MainLayout

@Route(value = "hello", layout = MainLayout::class)
@RouteAlias(value = "", layout = MainLayout::class)
@PageTitle("Hello World")
class HelloWorldView(
	private val profileApi: ProfileApi,
) : HorizontalLayout() {

	private val name: TextField
	private val sayHello: Button

	init {
		addClassName("hello-world-view")
		name = TextField("Your name")
		sayHello = Button("Say hello")
		add(name, sayHello)
		setVerticalComponentAlignment(FlexComponent.Alignment.END, name, sayHello)
		sayHello.addClickListener { e: ClickEvent<Button?>? -> Notification.show("Hello " + profileApi!!.authMeControllerMe().username) }
	}
}