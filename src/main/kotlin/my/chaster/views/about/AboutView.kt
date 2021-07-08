package my.chaster.views.about

import com.vaadin.flow.component.Text
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import my.chaster.views.MainLayout

@Route(value = "about", layout = MainLayout::class)
@PageTitle("About")
class AboutView : Div() {

	init {
		addClassName("about-view")
		add(Text("Content placeholder"))
	}
}