import ingisis.manager.domains.rule.model.dto.LinterRuleDTO
import ingisis.manager.domains.rule.service.LinterRuleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/rule")
@CrossOrigin("*")
class LinterRuleController(
    @Autowired private val linterRuleService: LinterRuleService,
) {
    @GetMapping("/linter")
    suspend fun getLinterRuleByUserId(principal: Principal): ResponseEntity<LinterRuleDTO> =
        ResponseEntity.ok(linterRuleService.getLinterRulesByUserId(principal.name))

    @PutMapping("/linter")
    suspend fun updateLinterRules(
        @RequestBody linterRules: LinterRuleDTO,
        principal: Principal,
    ): ResponseEntity<LinterRuleDTO> = ResponseEntity.ok(linterRuleService.updateLinterRules(linterRules, principal.name))
}
