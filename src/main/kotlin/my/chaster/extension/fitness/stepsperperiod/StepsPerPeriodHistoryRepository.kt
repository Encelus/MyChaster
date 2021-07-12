package my.chaster.extension.fitness.stepsperperiod

import my.chaster.chaster.ChasterLockId
import org.springframework.data.jpa.repository.JpaRepository


interface StepsPerPeriodHistoryRepository : JpaRepository<StepsPerPeriodHistory, StepsPerPeriodHistoryId> {

	fun findAllByChasterLockId(chasterLockId: ChasterLockId): Set<StepsPerPeriodHistory>

}