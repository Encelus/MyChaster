package my.chaster

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.transaction.TestTransaction
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.transaction.Transactional

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [MyChasterApp::class])
@ActiveProfiles("integration")
@Transactional
abstract class AbstractIntegrationTest {

	@PersistenceContext
	private lateinit var entityManager: EntityManager

	@AfterEach
	fun flushEntityManager() {
		if (TestTransaction.isActive()) {
			entityManager.flush()
		}
	}

	fun COMMIT() {
		END_TRANSACTION()
		START_TRANSACTION()
	}

	fun END_TRANSACTION() {
		TestTransaction.flagForCommit()
		TestTransaction.end()
	}

	fun START_TRANSACTION() {
		TestTransaction.start()
	}
}