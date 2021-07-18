package my.chaster.extension.fitness.stepsperperiod

import my.chaster.chaster.ChasterLockId
import my.chaster.chaster.WithChasterUserIdRepository
import my.chaster.jpa.AbstractEntityRepository
import org.springframework.data.jpa.repository.Query


interface StepsPerPeriodHistoryRepository : AbstractEntityRepository<StepsPerPeriodHistory, StepsPerPeriodHistoryId>, WithChasterUserIdRepository<StepsPerPeriodHistory> {

	fun findAllByChasterLockId(chasterLockId: ChasterLockId): Set<StepsPerPeriodHistory>

	@Query("SELECT DISTINCT chasterLockId FROM StepsPerPeriodHistory WHERE isFinal IS FALSE")
	fun findAllActiveChasterLockIds(): Set<ChasterLockId>

}