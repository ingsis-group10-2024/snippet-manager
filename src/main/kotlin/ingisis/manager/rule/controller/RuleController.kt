package ingisis.manager.rule.controller

import ingisis.manager.rule.model.dto.RuleDTO
import ingisis.manager.rule.model.enums.RuleTypeEnum
import ingisis.manager.rule.service.RuleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/manager/rules")
class RuleController(
    @Autowired private val ruleService: RuleService,
) {
    @PutMapping("/format")
    fun modifyFormatRule(
        @RequestBody newRules: List<RuleDTO>, // New rules to be added
        principal: Principal,
    ): ResponseEntity<List<RuleDTO>> {
        val userId = principal.name
        val updatedRules = ruleService.createOrUpdateRules(newRules, RuleTypeEnum.FORMAT, userId)
        return ResponseEntity.ok(updatedRules)
    }

    @PutMapping("/lint")
    fun modifyLintingRule(
        @RequestBody newRules: List<RuleDTO>, // New rules to be added
        principal: Principal,
    ): ResponseEntity<List<RuleDTO>> {
        val userId = principal.name
        val updatedRules = ruleService.createOrUpdateRules(newRules, RuleTypeEnum.LINT, userId)
        return ResponseEntity.ok(updatedRules)
    }

    @DeleteMapping("/{ruleId}")
    fun deleteRule(
        @PathVariable ruleId: String,
        principal: Principal,
    ): ResponseEntity<Void> {
        ruleService.deleteRule(principal.name, ruleId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/format")
    fun getFormatRules(principal: Principal): ResponseEntity<List<RuleDTO>> {
        val rules = ruleService.getRules(userId = principal.name, ruleType = RuleTypeEnum.FORMAT)
        return ResponseEntity.ok(rules)
    }

    @GetMapping("/lint")
    fun getLintingRules(principal: Principal): ResponseEntity<List<RuleDTO>> {
        val rules = ruleService.getRules(userId = principal.name, ruleType = RuleTypeEnum.LINT)
        return ResponseEntity.ok(rules)
    }
}
