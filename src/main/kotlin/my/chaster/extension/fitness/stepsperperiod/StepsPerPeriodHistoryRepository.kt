package my.chaster.extension.fitness.stepsperperiod

import my.chaster.chaster.ChasterLockId
import my.chaster.chaster.WithChasterUserIdRepository
import my.chaster.jpa.AbstractEntityRepository


interface StepsPerPeriodHistoryRepository : AbstractEntityRepository<StepsPerPeriodHistory, StepsPerPeriodHistoryId>, WithChasterUserIdRepository<StepsPerPeriodHistory> {

	fun findAllByChasterLockId(chasterLockId: ChasterLockId): Set<StepsPerPeriodHistory>

}