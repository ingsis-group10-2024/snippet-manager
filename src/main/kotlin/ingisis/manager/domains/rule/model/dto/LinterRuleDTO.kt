package ingisis.manager.domains.rule.model.dto

import ingisis.manager.domains.rule.persistance.entity.LinterRule
import ingisis.manager.redis.events.rules.CaseConvention

class LinterRuleDTO {
    var userId: String? = null
    var caseConvention: CaseConvention? = null
    var printExpressionsEnabled: Boolean? = false

    constructor(
        userId: String?,
        caseConvention: CaseConvention?,
        printExpressionsEnabled: Boolean?,
    ) {
        this.caseConvention = caseConvention
        this.printExpressionsEnabled = printExpressionsEnabled
        this.userId = userId
    }

    constructor(rule: LinterRule) {
        this.caseConvention = rule.caseConvention
        this.printExpressionsEnabled = rule.printExpressionsEnabled
        this.userId = rule.userId
    }

    constructor(userId: String?) {
        this.userId = userId
        this.caseConvention = CaseConvention.CAMEL_CASE
        this.printExpressionsEnabled = false
    }
}
