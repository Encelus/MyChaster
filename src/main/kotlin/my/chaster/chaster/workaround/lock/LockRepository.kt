package my.chaster.chaster.workaround.lock

import my.chaster.chaster.WithChasterLockIdRepository
import my.chaster.jpa.AbstractEntityRepository
import java.util.UUID

interface LockRepository : AbstractEntityRepository<Lock, LockId>, WithChasterLockIdRepository<Lock> {

	fun findByFakeApiKey(fakeApiKey: UUID): Lock?
}