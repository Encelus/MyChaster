package my.chaster.chaster

import my.chaster.gen.chaster.api.LocksApi
import my.chaster.gen.chaster.model.SetFreezeDto
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
@Transactional
class ChasterLockService(
	private val locksApi: LocksApi,
) {

	fun freeze(chasterLockId: ChasterLockId) {
		val dto = SetFreezeDto()
		dto.isIsFrozen = true
		locksApi.lockControllerSetFreeze(dto, chasterLockId.id)
	}

	fun unfreeze(chasterLockId: ChasterLockId) {
		val dto = SetFreezeDto()
		dto.isIsFrozen = false
		locksApi.lockControllerSetFreeze(dto, chasterLockId.id)
	}
}