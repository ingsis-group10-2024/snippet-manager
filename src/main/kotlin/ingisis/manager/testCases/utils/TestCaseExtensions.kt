package ingisis.manager.testCases.utils

import ingisis.manager.testCases.model.TestCaseDTO
import ingisis.manager.testCases.persistance.entity.TestCaseEntity

fun TestCaseEntity.toDTO() =
    TestCaseDTO(
        id = this.snippetId,
        name = this.name,
        input = this.input,
        output = this.output,
    )
