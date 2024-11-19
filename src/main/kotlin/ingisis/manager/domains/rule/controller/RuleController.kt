package ingisis.manager.domains.rule.controller

import ingisis.manager.domains.rule.model.dto.UpdateUserRuleInput
import ingisis.manager.domains.rule.model.dto.UserRuleOutput
import ingisis.manager.domains.rule.model.enums.RuleType
import ingisis.manager.domains.rule.service.implementatios.RuleService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/rule")
class RuleController(
    private val ruleService: RuleService,
) {
    @PostMapping("/default")
    suspend fun createDefaultRulesForUser(
        principal: Principal,
        @RequestHeader("Authorization") token: String,
    ): ResponseEntity<Unit> {
        ruleService.createDefaultRulesForUser(principal.name)
        return ResponseEntity.ok().build()
    }

    @PutMapping()
    suspend fun updateUserRule(
        principal: Principal,
        @RequestBody updatedRules: List<UpdateUserRuleInput>,
    ): ResponseEntity<List<UserRuleOutput>> {
        val userId = principal.name
        return ResponseEntity.ok(ruleService.updateUserRules(userId, updatedRules))
    }

    @GetMapping("/all/{ruleType}")
    suspend fun getUserRules(
        @RequestHeader("Authorization") authHeader: String,
        principal: Principal,
        @PathVariable ruleType: RuleType,
    ): ResponseEntity<List<UserRuleOutput>> {
        val rules = ruleService.getRulesForUserByType(principal.name, ruleType)
        return ResponseEntity.ok(rules)
    }
}
