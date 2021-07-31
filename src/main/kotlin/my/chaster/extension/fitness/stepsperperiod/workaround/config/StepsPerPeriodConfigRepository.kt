package my.chaster.extension.fitness.stepsperperiod.workaround.config

import my.chaster.chaster.WithChasterLockIdRepository
import my.chaster.jpa.AbstractEntityRepository

interface StepsPerPeriodConfigRepository : AbstractEntityRepository<StepsPerPeriodConfig, StepsPerPeriodConfigId>, WithChasterLockIdRepository<StepsPerPeriodConfig>