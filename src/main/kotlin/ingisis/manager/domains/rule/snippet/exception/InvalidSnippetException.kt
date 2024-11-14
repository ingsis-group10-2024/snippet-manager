package ingisis.manager.domains.rule.snippet.exception

import sca.StaticCodeAnalyzerError

class InvalidSnippetException(
    val errors: List<StaticCodeAnalyzerError>,
) : RuntimeException("Snippet is invalid.")
