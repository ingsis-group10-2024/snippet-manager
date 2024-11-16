import ingisis.manager.domains.rule.model.dto.LinterRulesDTO
import ingisis.manager.domains.rule.model.dto.RuleDTO
import ingisis.manager.domains.rule.service.RuleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@CrossOrigin("*")
class LinterRulesController(
    @Autowired private val ruleService: RuleService,
) {
    @GetMapping("/rule/linter")
    suspend fun getLinterRulesByUserId(principal: Principal): ResponseEntity<List<RuleDTO>> =
        ResponseEntity.ok(ruleService.getLinterRulesByUserId(principal.name))

    @PutMapping("/rule/linter")
    suspend fun updateLinterRules(
        @RequestBody linterRules: LinterRulesDTO,
        principal: Principal,
    ): ResponseEntity<LinterRulesDTO> = ResponseEntity.ok(ruleService.updateLinterRules(linterRules, principal.name))
}
