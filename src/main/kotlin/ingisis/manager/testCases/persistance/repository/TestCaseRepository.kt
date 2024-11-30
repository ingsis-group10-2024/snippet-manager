package ingisis.manager.testCases.persistance.repository

import ingisis.manager.testCases.persistance.entity.TestCaseEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TestCaseRepository : JpaRepository<TestCaseEntity, String> {
    override fun findById(id: String): Optional<TestCaseEntity>
}
