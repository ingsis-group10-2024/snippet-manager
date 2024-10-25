package ingisis.manager.snippet.exception

import sca.StaticCodeAnalyzerError

class InvalidSnippetException(
    val errors: List<StaticCodeAnalyzerError>,
) : RuntimeException("Snippet is invalid.")
