package my.chaster.extension.fitness.stepstounlock.workaround.config

import my.chaster.chaster.ChasterLockId
import my.chaster.chaster.WithChasterLockIdRepository
import my.chaster.jpa.AbstractEntityRepository
import org.springframework.data.jpa.repository.Query

interface StepsToUnlockConfigRepository : AbstractEntityRepository<StepsToUnlockConfig, StepsToUnlockConfigId>, WithChasterLockIdRepository<StepsToUnlockConfig> {

	@Query("SELECT DISTINCT chasterLockId FROM StepsToUnlockConfig WHERE isFrozen IS TRUE")
	fun findAllActiveChasterLockIds(): Set<ChasterLockId>
}