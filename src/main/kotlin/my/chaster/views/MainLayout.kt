package my.chaster.views

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.ComponentUtil
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.applayout.DrawerToggle
import com.vaadin.flow.component.avatar.Avatar
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.component.tabs.TabsVariant
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.RouterLink
import my.chaster.chaster.workaround.lock.LockRepository
import my.chaster.views.about.AboutView
import my.chaster.views.error.nouserspecified.NoUserSpecified
import my.chaster.views.helloworld.HelloWorldView
import java.util.Optional
import java.util.UUID

/**
 * The main view is a top-level placeholder for other views.
 */
class MainLayout(
	private val lockRepository: LockRepository,
) : AppLayout(), BeforeEnterObserver {

	private val menu: Tabs
	private var viewTitle: H1? = null

	init {
		primarySection = Section.DRAWER
		addToNavbar(true, createHeaderContent())
		menu = createMenu()
		addToDrawer(createDrawerContent(menu))
	}

	private fun createHeaderContent(): Component {
		val layout = HorizontalLayout()
		layout.className = "sidemenu-header"
		layout.themeList["dark"] = true
		layout.setWidthFull()
		layout.isSpacing = false
		layout.alignItems = FlexComponent.Alignment.CENTER
		layout.add(DrawerToggle())
		viewTitle = H1()
		layout.add(viewTitle)
		val avatar = Avatar()
		avatar.addClassNames("ms-auto", "me-m")
		layout.add(avatar)
		return layout
	}

	private fun createDrawerContent(menu: Tabs): Component {
		val layout = VerticalLayout()
		layout.className = "sidemenu-menu"
		layout.setSizeFull()
		layout.isPadding = false
		layout.isSpacing = false
		layout.themeList["spacing-s"] = true
		layout.alignItems = FlexComponent.Alignment.STRETCH
		val logoLayout = HorizontalLayout()
		logoLayout.setId("logo")
		logoLayout.alignItems = FlexComponent.Alignment.CENTER
		logoLayout.add(Image("images/logo.png", "MyChaster logo"))
		logoLayout.add(H1("MyChaster"))
		layout.add(logoLayout, menu)
		return layout
	}

	private fun createMenu(): Tabs {
		val tabs = Tabs()
		tabs.orientation = Tabs.Orientation.VERTICAL
		tabs.addThemeVariants(TabsVariant.LUMO_MINIMAL)
		tabs.setId("tabs")
		tabs.add(*createMenuItems())
		return tabs
	}

	private fun createMenuItems(): Array<Component> {
		return arrayOf(
			createTab("Hello World", HelloWorldView::class.java),
			createTab("About", AboutView::class.java),
		)
	}

	override fun afterNavigation() {
		super.afterNavigation()
		getTabForComponent(content).ifPresent { selectedTab: Tab? -> menu.selectedTab = selectedTab }
		viewTitle!!.text = currentPageTitle
	}

	private fun getTabForComponent(component: Component): Optional<Tab> {
		return menu.children.filter { tab: Component? -> ComponentUtil.getData(tab, Class::class.java) == component.javaClass }
			.findFirst().map { obj: Component? -> Tab::class.java.cast(obj) }
	}

	private val currentPageTitle: String
		private get() {
			val title = content.javaClass.getAnnotation(PageTitle::class.java)
			return title?.value ?: ""
		}

	override fun beforeEnter(event: BeforeEnterEvent) {
		verifyChasterUserId(event)
	}

	private fun verifyChasterUserId(event: BeforeEnterEvent) {
		if (event.location.queryParameters.parameters.containsKey(API_KEY_NAME)) {
			val apiKey = event.location.queryParameters.parameters[API_KEY_NAME]!![0]
			val lock = lockRepository.findByFakeApiKey(UUID.fromString(apiKey))
			if (lock == null) {
				event.rerouteToError(NoUserSpecified(), "")
			} else {
				event.ui.session.setChasterUserId(lock.chasterUserId)
				event.ui.session.setChasterLockId(lock.chasterLockId)
			}
		} else if (!event.ui.session.hasChasterUserId()) {
			event.rerouteToError(NoUserSpecified(), "")
		}
	}

	companion object {
		private fun createTab(text: String, navigationTarget: Class<out Component>): Tab {
			val tab = Tab()
			tab.add(RouterLink(text, navigationTarget))
			ComponentUtil.setData(tab, Class::class.java, navigationTarget)
			return tab
		}

		const val API_KEY_NAME = "api-key"
	}
}