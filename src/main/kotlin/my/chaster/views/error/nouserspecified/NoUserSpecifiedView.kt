package my.chaster.views.error.nouserspecified

import com.vaadin.flow.component.Text
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.ErrorParameter
import com.vaadin.flow.router.HasErrorParameter
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import my.chaster.views.MainLayout
import javax.servlet.http.HttpServletResponse

@Route(value = "no-user-specified")
@PageTitle("No User Specified")
class NoUserSpecifiedView : VerticalLayout(), HasErrorParameter<NoUserSpecified> {

	init {
		defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
		justifyContentMode = FlexComponent.JustifyContentMode.CENTER
		setSizeFull()
		add(H3("Error: No User Specified"))
		add(Div(Text("Use the original link containing an ${MainLayout.API_KEY_NAME}")))
	}

	override fun setErrorParameter(event: BeforeEnterEvent, parameter: ErrorParameter<NoUserSpecified>): Int {
		return HttpServletResponse.SC_UNAUTHORIZED
	}
}