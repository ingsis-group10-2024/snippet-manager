package ingisis.manager.domains.rule.service.implementatios

import ingisis.manager.domains.rule.model.dto.LinterRulesDTO
import ingisis.manager.domains.rule.persistance.repository.LinterRulesRepository
import ingisis.manager.domains.rule.service.LinterRulesService
import ingisis.manager.domains.snippet.service.SnippetService
import ingisis.manager.redis.producer.LintRequestProducer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class LinterRulesService(
    @Autowired private val linterRulesRepository: LinterRulesRepository,
    @Autowired private val snippetService: SnippetService,
    @Autowired private val producer: LintRequestProducer,
) : LinterRulesService {
    override suspend fun getLinterRulesByUserId(userId: String): LinterRulesDTO {
        TODO("Not yet implemented")
    }

    override suspend fun updateLinterRules(
        linterRules: LinterRulesDTO,
        userId: String,
    ): LinterRulesDTO {
        TODO("Not yet implemented")
    }
}
