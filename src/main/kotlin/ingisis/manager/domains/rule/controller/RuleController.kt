package ingisis.manager.domains.rule.controller

import ingisis.manager.domains.rule.model.dto.RuleDTO
import ingisis.manager.domains.rule.model.enums.RuleTypeEnum
import ingisis.manager.domains.rule.service.RuleService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/rules")
class RuleController (private val ruleService: RuleService){


    @PostMapping("/format")
    fun createOrUpdateFormatRules(
        @RequestBody newRules: List<RuleDTO>,
        principal: Principal,
        @RequestHeader("Authorization") token: String,
    ): ResponseEntity<List<RuleDTO>> {
        val updatedRules =
            ruleService.createOrUpdateRules(
                newRules = newRules,
                url = "http://snippet-runner:8080/runner/rules/format",
                token = token,
                ruleType = RuleTypeEnum.FORMAT,
                userId = principal.name,
            )
        return ResponseEntity.ok(updatedRules)
    }

    @PostMapping("/lint")
    fun createOrUpdateLintingRules(
        @RequestBody newRules: List<RuleDTO>,
        principal: Principal,
        @RequestHeader("Authorization") token: String,
    ): ResponseEntity<List<RuleDTO>> {
        val updatedRules =
            ruleService.createOrUpdateRules(
                newRules,
                "http://snippet-runner:8080/runner/rules/lint",
                token,
                RuleTypeEnum.LINT,
                userId = principal.name,
            )
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
    fun getFormatRules(
        @RequestHeader("Authorization") authHeader: String,
        principal: Principal,
    ): ResponseEntity<List<RuleDTO>> {
        val rules = ruleService.getFormatRules(principal.name)
        return ResponseEntity.ok(rules)
    }

    @GetMapping("/lint")
    fun getLintingRules(
        @RequestHeader("Authorization") authHeader: String,
        principal: Principal,
    ): ResponseEntity<List<RuleDTO>> {
        val rules = ruleService.getLintingRules(principal.name)
        return ResponseEntity.ok(rules)
    }
}


