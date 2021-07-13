package my.chaster.views.error.nouserspecified

import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.ErrorParameter
import com.vaadin.flow.router.HasErrorParameter
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import javax.servlet.http.HttpServletResponse

@Route(value = "no-user-specified")
@PageTitle("No User Specified")
class NoUserSpecifiedView : HorizontalLayout(), HasErrorParameter<NoUserSpecified> {

	init {
		defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
		justifyContentMode = FlexComponent.JustifyContentMode.CENTER
		setSizeFull()
		add(H3("Error: No User Specified"))
	}

	override fun setErrorParameter(event: BeforeEnterEvent, parameter: ErrorParameter<NoUserSpecified>): Int {
		return HttpServletResponse.SC_UNAUTHORIZED
	}
}