package ingisis.manager.domains.rule.persistance.repository

import ingisis.manager.domains.rule.persistance.entity.Rule
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface RuleRepository : JpaRepository<Rule, String> {
    fun findByNameRule(name: String): Optional<Rule>
}
