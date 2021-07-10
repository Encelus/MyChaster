package my.chaster.extension.fitness.stepsperperiod

import my.chaster.chaster.ChasterUserId
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant


interface StepsPerPeriodHistoryRepository : JpaRepository<StepsPerPeriodHistory, StepsPerPeriodHistoryId> {

	fun findAllByChasterUserIdAndPeriodStartAfterOrderByPeriodStartDesc(chasterUserId: ChasterUserId, lockStart: Instant): List<StepsPerPeriodHistory>

}