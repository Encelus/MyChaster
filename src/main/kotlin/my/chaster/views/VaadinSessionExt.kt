package my.chaster.views

import com.vaadin.flow.server.VaadinSession
import my.chaster.chaster.ChasterLockId
import my.chaster.chaster.ChasterUserId
import java.time.ZoneId

class VaadinSessionExt

fun VaadinSession.hasChasterUserId(): Boolean {
	return getAttribute("chasterUserId") != null
}

fun VaadinSession.getChasterUserId(): ChasterUserId {
	return getAttribute("chasterUserId") as ChasterUserId
}

fun VaadinSession.setChasterUserId(chasterUserId: ChasterUserId) {
	setAttribute("chasterUserId", chasterUserId)
}


fun VaadinSession.getChasterLockId(): ChasterLockId {
	return getAttribute("chasterLockId") as ChasterLockId
}

fun VaadinSession.setChasterLockId(chasterLockId: ChasterLockId) {
	setAttribute("chasterLockId", chasterLockId)
}


fun VaadinSession.hasZoneId(): Boolean {
	return getAttribute("zoneId") != null
}

fun VaadinSession.getZoneId(): ZoneId {
	return getAttribute("zoneId") as ZoneId
}

fun VaadinSession.setZoneId(zoneId: ZoneId) {
	return setAttribute("zoneId", zoneId)
}